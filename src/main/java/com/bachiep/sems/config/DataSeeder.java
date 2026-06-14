package com.bachiep.sems.config;

import com.bachiep.sems.entity.Employee;
import com.bachiep.sems.entity.Permission;
import com.bachiep.sems.entity.Role;
import com.bachiep.sems.entity.User;
import com.bachiep.sems.enums.RoleName;
import com.bachiep.sems.repository.EmployeeRepository;
import com.bachiep.sems.repository.PermissionRepository;
import com.bachiep.sems.repository.RoleRepository;
import com.bachiep.sems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String USER_CREATE = "USER_CREATE";
    private static final String USER_VIEW = "USER_VIEW";
    private static final String USER_EDIT = "USER_EDIT";
    private static final String USER_DELETE = "USER_DELETE";
    private static final String EMPLOYEE_CREATE = "EMPLOYEE_CREATE";
    private static final String EMPLOYEE_VIEW = "EMPLOYEE_VIEW";
    private static final String EMPLOYEE_EDIT = "EMPLOYEE_EDIT";
    private static final String EMPLOYEE_DELETE = "EMPLOYEE_DELETE";
    private static final String AUDIT_VIEW = "AUDIT_VIEW";
    private static final String LOGIN_HISTORY_VIEW = "LOGIN_HISTORY_VIEW";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.default-password:Password@123}")
    private String defaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Map<String, Permission> permissions = seedPermissions();
        Map<RoleName, Role> roles = seedRoles(permissions);

        seedUser("admin", "admin@sems.local", "System Administrator", roles.get(RoleName.ADMIN));
        seedUser("manager", "manager@sems.local", "Demo Manager", roles.get(RoleName.MANAGER));
        seedUser("employee", "employee@sems.local", "Demo Employee", roles.get(RoleName.EMPLOYEE));
    }

    private Map<String, Permission> seedPermissions() {
        return Arrays.stream(new String[]{
                        USER_CREATE,
                        USER_VIEW,
                        USER_EDIT,
                        USER_DELETE,
                        EMPLOYEE_CREATE,
                        EMPLOYEE_VIEW,
                        EMPLOYEE_EDIT,
                        EMPLOYEE_DELETE,
                        AUDIT_VIEW,
                        LOGIN_HISTORY_VIEW
                })
                .map(this::getOrCreatePermission)
                .collect(Collectors.toMap(Permission::getName, permission -> permission));
    }

    private Map<RoleName, Role> seedRoles(Map<String, Permission> permissions) {
        Map<RoleName, Role> roles = new EnumMap<>(RoleName.class);

        roles.put(RoleName.ADMIN, getOrCreateRole(RoleName.ADMIN));
        roles.put(RoleName.MANAGER, getOrCreateRole(RoleName.MANAGER));
        roles.put(RoleName.EMPLOYEE, getOrCreateRole(RoleName.EMPLOYEE));

        roles.get(RoleName.ADMIN).setPermissions(new HashSet<>(permissions.values()));
        roles.get(RoleName.MANAGER).setPermissions(new HashSet<>(Set.of(
                permissions.get(EMPLOYEE_CREATE),
                permissions.get(EMPLOYEE_VIEW),
                permissions.get(EMPLOYEE_EDIT)
        )));
        roles.get(RoleName.EMPLOYEE).setPermissions(new HashSet<>(Set.of(
                permissions.get(EMPLOYEE_VIEW)
        )));

        roleRepository.saveAll(roles.values());
        return roles;
    }

    private Permission getOrCreatePermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    return permissionRepository.save(permission);
                });
    }

    private Role getOrCreateRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }

    private void seedUser(String username, String email, String fullName, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setPassword(passwordEncoder.encode(defaultPassword));
                    return newUser;
                });

        user.setRoles(new HashSet<>(Set.of(role)));
        User savedUser = userRepository.save(user);

        if (!employeeRepository.existsByUserId(savedUser.getId())) {
            Employee employee = new Employee();
            employee.setFullName(fullName);
            employee.setDepartment(role.getName().name());
            employee.setUser(savedUser);
            employeeRepository.save(employee);
        }
    }
}
