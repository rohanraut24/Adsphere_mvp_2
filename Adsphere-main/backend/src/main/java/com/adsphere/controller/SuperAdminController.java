package com.adsphere.controller;

import com.adsphere.dto.admin.AdminStats;
import com.adsphere.dto.admin.UserResponse;
import com.adsphere.dto.upgrade.*;
import com.adsphere.model.Role;
import com.adsphere.repository.UserRepository;
import com.adsphere.service.AdminService;
import com.adsphere.service.UpgradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UpgradeService upgradeService;
    private final AdminService adminService;
    private final UserRepository userRepository;

    // --- Dashboard ---

    @GetMapping("/stats")
    public ResponseEntity<AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // --- User management ---

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    // --- Upgrade request review ---

    @GetMapping("/upgrade-requests/pending")
    public ResponseEntity<List<UpgradeResponse>> getPending() {
        return ResponseEntity.ok(upgradeService.getPending());
    }

    @PutMapping("/upgrade-requests/{id}/review")
    public ResponseEntity<UpgradeResponse> review(@PathVariable Long id,
                                                   @Valid @RequestBody ReviewRequest request,
                                                   @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(upgradeService.review(id, admin.getUsername(), request));
    }
}
