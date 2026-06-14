package com.bachiep.sems.dto.response;

import com.bachiep.sems.entity.Role;
import com.bachiep.sems.entity.User;
import com.bachiep.sems.enums.RoleName;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class CurrentUserResponse {
    private Long id;
    private String username;
    private String email;
    private Long employeeId;
    private Set<RoleName> roles;
    private Set<String> permissions;

    public static CurrentUserResponse from(User user) {
        Long employeeId = user.getEmployee() == null ? null : user.getEmployee().getId();

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        return CurrentUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .employeeId(employeeId)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .permissions(permissions)
                .build();
    }
}
