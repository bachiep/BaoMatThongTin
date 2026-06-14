package com.bachiep.sems.dto.response;

import com.bachiep.sems.entity.Role;
import com.bachiep.sems.entity.User;
import com.bachiep.sems.enums.RoleName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean locked;
    private int failedAttempts;
    private LocalDateTime lockTime;
    private LocalDateTime createdAt;
    private Long employeeId;
    private Set<RoleName> roles;

    public static UserResponse from(User user) {
        Long employeeId = user.getEmployee() == null ? null : user.getEmployee().getId();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .locked(user.isLocked())
                .failedAttempts(user.getFailedAttempts())
                .lockTime(user.getLockTime())
                .createdAt(user.getCreatedAt())
                .employeeId(employeeId)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
