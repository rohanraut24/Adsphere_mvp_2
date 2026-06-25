package com.adsphere.controller;

import com.adsphere.model.User;
import com.adsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final UserRepository userRepository;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(Map.of(
                "balance", user.getBalance(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, BigDecimal> payload) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        BigDecimal amount = payload.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }
        
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of(
                "balance", user.getBalance(),
                "message", "Deposit successful"
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, BigDecimal> payload) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        BigDecimal amount = payload.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }
        
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of(
                "balance", user.getBalance(),
                "message", "Withdrawal successful"
        ));
    }
}
