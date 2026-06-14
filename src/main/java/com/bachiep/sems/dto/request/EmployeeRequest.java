package com.bachiep.sems.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeRequest {
    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 100)
    private String department;

    @NotNull
    private Long userId;
}
