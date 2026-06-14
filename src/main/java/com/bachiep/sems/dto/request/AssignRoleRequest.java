package com.bachiep.sems.dto.request;

import com.bachiep.sems.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRoleRequest {
    @NotEmpty
    private Set<RoleName> roles;
}
