package com.bachiep.sems.service;

import com.bachiep.sems.dto.request.AssignRoleRequest;
import com.bachiep.sems.dto.request.CreateUserRequest;
import com.bachiep.sems.dto.request.UpdateUserRequest;
import com.bachiep.sems.dto.response.UserResponse;
import com.bachiep.sems.entity.Role;
import com.bachiep.sems.entity.User;
import com.bachiep.sems.enums.RoleName;
import com.bachiep.sems.repository.RoleRepository;
import com.bachiep.sems.repository.UserRepository;
import com.bachiep.sems.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String ipAddress, Authentication authentication) {
        ensureUsernameAvailable(request.getUsername(), null);
        ensureEmailAvailable(request.getEmail(), null);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRoles(request.getRoles()));

        User savedUser = userRepository.save(user);
        auditService.logAction(authentication.getName(), "USER_CREATED - " + savedUser.getUsername(), ipAddress);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String ipAddress, Authentication authentication) {
        User user = findUser(id);
        ensureUsernameAvailable(request.getUsername(), id);
        ensureEmailAvailable(request.getEmail(), id);

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        auditService.logAction(authentication.getName(), "USER_UPDATED - " + savedUser.getUsername(), ipAddress);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public void deleteUser(Long id, String ipAddress, Authentication authentication) {
        User user = findUser(id);
        ensureNotSelf(user, authentication, "You cannot delete your own account");

        userRepository.delete(user);
        auditService.logAction(authentication.getName(), "USER_DELETED - " + user.getUsername(), ipAddress);
    }

    @Transactional
    public UserResponse lockUser(Long id, String ipAddress, Authentication authentication) {
        User user = findUser(id);
        ensureNotSelf(user, authentication, "You cannot lock your own account");

        user.setLocked(true);
        user.setLockTime(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        auditService.logAction(authentication.getName(), "USER_LOCKED_BY_ADMIN - " + savedUser.getUsername(), ipAddress);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse unlockUser(Long id, String ipAddress, Authentication authentication) {
        User user = findUser(id);
        user.setLocked(false);
        user.setFailedAttempts(0);
        user.setLockTime(null);

        User savedUser = userRepository.save(user);
        auditService.logAction(authentication.getName(), "USER_UNLOCKED_BY_ADMIN - " + savedUser.getUsername(), ipAddress);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse assignRoles(Long id, AssignRoleRequest request, String ipAddress, Authentication authentication) {
        User user = findUser(id);
        user.setRoles(resolveRoles(request.getRoles()));

        User savedUser = userRepository.save(user);
        auditService.logAction(authentication.getName(), "USER_ROLE_UPDATED - " + savedUser.getUsername(), ipAddress);

        return UserResponse.from(savedUser);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<RoleName> resolvedRoleNames = roleNames == null || roleNames.isEmpty()
                ? Collections.singleton(RoleName.EMPLOYEE)
                : roleNames;

        return resolvedRoleNames.stream()
                .map(this::getOrCreateRole)
                .collect(Collectors.toSet());
    }

    private Role getOrCreateRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }

    private void ensureUsernameAvailable(String username, Long currentUserId) {
        userRepository.findByUsername(username)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
                });
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
                });
    }

    private void ensureNotSelf(User targetUser, Authentication authentication, String message) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails currentUser)) {
            throw new AccessDeniedException("Invalid authenticated principal");
        }

        if (targetUser.getId().equals(currentUser.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
