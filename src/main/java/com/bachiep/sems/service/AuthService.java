package com.bachiep.sems.service;

import com.bachiep.sems.dto.request.LoginRequest;
import com.bachiep.sems.dto.request.RegisterRequest;
import com.bachiep.sems.dto.request.VerifyOtpRequest;
import com.bachiep.sems.dto.response.AuthResponse;
import com.bachiep.sems.dto.response.CurrentUserResponse;
import com.bachiep.sems.entity.*;
import com.bachiep.sems.enums.LoginStatus;
import com.bachiep.sems.enums.RoleName;
import com.bachiep.sems.exception.AccountTemporarilyLockedException;
import com.bachiep.sems.exception.AuthenticationFailedException;
import com.bachiep.sems.exception.RateLimitExceededException;
import com.bachiep.sems.repository.*;
import com.bachiep.sems.security.CustomUserDetails;
import com.bachiep.sems.security.CustomUserDetailsService;
import com.bachiep.sems.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    @Value("${app.security.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.security.otp.length:6}")
    private int otpLength;

    @Value("${app.security.lockout.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.EMPLOYEE);
                    return roleRepository.save(role);
                });

        user.setRoles(Collections.singleton(userRole));

        User savedUser = userRepository.save(user);

        Employee employee = new Employee();
        employee.setFullName(registerRequest.getFullName());
        employee.setUser(savedUser);
        employeeRepository.save(employee);

        auditService.logAction(savedUser.getUsername(), "USER_REGISTERED", null);

        return savedUser;
    }

    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest, String ipAddress) {
        if (!rateLimitService.allowLogin(loginRequest.getUsername(), ipAddress)) {
            saveLoginHistory(loginRequest.getUsername(), ipAddress, LoginStatus.FAILED);
            auditService.logAction("SYSTEM", "LOGIN_RATE_LIMITED - " + loginRequest.getUsername(), ipAddress);
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null) {
            saveLoginHistory(loginRequest.getUsername(), ipAddress, LoginStatus.FAILED);
            auditService.logAction("UNKNOWN", "LOGIN_FAILED_UNKNOWN_USER - " + loginRequest.getUsername(), ipAddress);
            throw new AuthenticationFailedException("Invalid username or password");
        }

        if (user.isLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(lockoutDurationMinutes).isBefore(LocalDateTime.now())) {
                user.setLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                auditService.logAction("SYSTEM", "USER_UNLOCKED - " + user.getUsername(), ipAddress);
            } else {
                throw new AccountTemporarilyLockedException("Account is locked. Please try again later.");
            }
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
            );
        } catch (AuthenticationException ex) {
            saveLoginHistory(loginRequest.getUsername(), ipAddress, LoginStatus.FAILED);
            auditService.logAction(user.getUsername(), "LOGIN_FAILED_BAD_PASSWORD", ipAddress);
            
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            
            if (attempts >= maxFailedAttempts) {
                user.setLocked(true);
                user.setLockTime(LocalDateTime.now());
                auditService.logAction("SYSTEM", "USER_LOCKED - " + user.getUsername(), ipAddress);
            }
            userRepository.save(user);

            throw new AuthenticationFailedException("Invalid username or password");
        }

        user.setFailedAttempts(0);
        userRepository.save(user);

        String otpCode = generateOtpCode();
        otpTokenRepository.deleteByUser(user);
        otpTokenRepository.flush();

        OtpToken otpToken = new OtpToken();
        otpToken.setToken(otpCode);
        otpToken.setUser(user);
        otpToken.setExpiryDate(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), otpCode);
        auditService.logAction(user.getUsername(), "OTP_SENT", ipAddress);

        return AuthResponse.builder()
                .username(user.getUsername())
                .message("OTP has been sent to your email")
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request, String ipAddress) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        OtpToken otpToken = otpTokenRepository.findByTokenAndUser(request.getOtp(), user)
                .orElse(null);

        if (otpToken == null) {
            saveLoginHistory(user.getUsername(), ipAddress, LoginStatus.FAILED);
            auditService.logAction(user.getUsername(), "LOGIN_FAILED_INVALID_OTP", ipAddress);
            throw new AuthenticationFailedException("Invalid OTP");
        }

        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpTokenRepository.deleteByUser(user);
            saveLoginHistory(user.getUsername(), ipAddress, LoginStatus.FAILED);
            auditService.logAction(user.getUsername(), "LOGIN_FAILED_EXPIRED_OTP", ipAddress);
            throw new AuthenticationFailedException("OTP has expired");
        }

        otpTokenRepository.deleteByUser(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        saveLoginHistory(user.getUsername(), ipAddress, LoginStatus.SUCCESS);
        auditService.logAction(user.getUsername(), "LOGIN_SUCCESS", ipAddress);

        return AuthResponse.builder()
                .accessToken(jwt)
                .username(user.getUsername())
                .message("Login verified successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails currentUser)) {
            throw new AccessDeniedException("Invalid authenticated principal");
        }

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        return CurrentUserResponse.from(user);
    }

    private String generateOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void saveLoginHistory(String username, String ipAddress, LoginStatus status) {
        LoginHistory history = new LoginHistory();
        history.setUsername(username);
        history.setIpAddress(ipAddress);
        history.setStatus(status);
        loginHistoryRepository.save(history);
    }
}
