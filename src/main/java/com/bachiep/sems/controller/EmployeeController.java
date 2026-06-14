package com.bachiep.sems.controller;

import com.bachiep.sems.dto.request.EmployeeRequest;
import com.bachiep.sems.dto.response.ApiResponse;
import com.bachiep.sems.dto.response.EmployeeResponse;
import com.bachiep.sems.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<List<EmployeeResponse>> getEmployees(Authentication authentication) {
        return ResponseEntity.ok(employeeService.getEmployees(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(employeeService.getEmployee(id, authentication));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(employeeService.createEmployee(request, httpRequest.getRemoteAddr(), authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_EDIT')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, httpRequest.getRemoteAddr(), authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    public ResponseEntity<ApiResponse> deleteEmployee(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        employeeService.deleteEmployee(id, httpRequest.getRemoteAddr(), authentication);
        return ResponseEntity.ok(new ApiResponse(true, "Employee deleted successfully"));
    }
}
