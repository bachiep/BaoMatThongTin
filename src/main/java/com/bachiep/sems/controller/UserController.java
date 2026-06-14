package com.bachiep.sems.controller;

import com.bachiep.sems.dto.request.AssignRoleRequest;
import com.bachiep.sems.dto.request.CreateUserRequest;
import com.bachiep.sems.dto.request.UpdateUserRequest;
import com.bachiep.sems.dto.response.ApiResponse;
import com.bachiep.sems.dto.response.UserResponse;
import com.bachiep.sems.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.createUser(request, httpRequest.getRemoteAddr(), authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request, httpRequest.getRemoteAddr(), authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        userService.deleteUser(id, httpRequest.getRemoteAddr(), authentication);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserResponse> lockUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.lockUser(id, httpRequest.getRemoteAddr(), authentication));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserResponse> unlockUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.unlockUser(id, httpRequest.getRemoteAddr(), authentication));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<UserResponse> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.assignRoles(id, request, httpRequest.getRemoteAddr(), authentication));
    }
}
