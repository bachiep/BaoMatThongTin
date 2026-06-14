package com.bachiep.sems.service;

import com.bachiep.sems.dto.request.EmployeeRequest;
import com.bachiep.sems.dto.response.EmployeeResponse;
import com.bachiep.sems.entity.Employee;
import com.bachiep.sems.entity.User;
import com.bachiep.sems.repository.EmployeeRepository;
import com.bachiep.sems.repository.UserRepository;
import com.bachiep.sems.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(Authentication authentication) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_MANAGER")) {
            return employeeRepository.findAll().stream()
                    .map(EmployeeResponse::from)
                    .toList();
        }

        return employeeRepository.findByUserId(currentUser.getUser().getId())
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id, Authentication authentication) {
        Employee employee = findEmployee(id);
        ensureCanView(employee, authentication);
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request, String ipAddress, Authentication authentication) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (employeeRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user already has an employee profile");
        }

        Employee employee = new Employee();
        applyRequest(employee, request);
        employee.setUser(user);

        Employee savedEmployee = employeeRepository.save(employee);
        auditService.logAction(authentication.getName(), "EMPLOYEE_CREATED - " + savedEmployee.getId(), ipAddress);

        return EmployeeResponse.from(savedEmployee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request, String ipAddress, Authentication authentication) {
        Employee employee = findEmployee(id);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        employeeRepository.findByUserId(user.getId())
                .filter(existingEmployee -> !existingEmployee.getId().equals(id))
                .ifPresent(existingEmployee -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user already has another employee profile");
                });

        applyRequest(employee, request);
        employee.setUser(user);

        Employee savedEmployee = employeeRepository.save(employee);
        auditService.logAction(authentication.getName(), "EMPLOYEE_UPDATED - " + savedEmployee.getId(), ipAddress);

        return EmployeeResponse.from(savedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id, String ipAddress, Authentication authentication) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
        auditService.logAction(authentication.getName(), "EMPLOYEE_DELETED - " + id, ipAddress);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void applyRequest(Employee employee, EmployeeRequest request) {
        employee.setFullName(request.getFullName());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(request.getDepartment());
    }

    private void ensureCanView(Employee employee, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_MANAGER")) {
            return;
        }

        CustomUserDetails currentUser = getCurrentUser(authentication);
        if (!employee.getUser().getId().equals(currentUser.getUser().getId())) {
            throw new AccessDeniedException("You can only view your own employee profile");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private CustomUserDetails getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails currentUser)) {
            throw new AccessDeniedException("Invalid authenticated principal");
        }
        return currentUser;
    }
}
