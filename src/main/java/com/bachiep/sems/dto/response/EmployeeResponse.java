package com.bachiep.sems.dto.response;

import com.bachiep.sems.entity.Employee;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String department;
    private Long userId;
    private String username;
    private String email;

    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .email(employee.getUser().getEmail())
                .build();
    }
}
