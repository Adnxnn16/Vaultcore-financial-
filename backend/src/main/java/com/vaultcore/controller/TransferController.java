package com.vaultcore.controller;

import com.vaultcore.dto.TransferDetailResponse;
import com.vaultcore.dto.TransferRequest;
import com.vaultcore.dto.TransferResponse;
import com.vaultcore.entity.User;
import com.vaultcore.repository.UserRepository;
import com.vaultcore.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    public TransferController(TransferService transferService, UserRepository userRepository) {
        this.transferService = transferService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = resolveUserId(userIdHeader, jwt);
        TransferResponse response = transferService.transfer(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransferDetailResponse> getTransfer(
            @PathVariable UUID transactionId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(transferService.getTransfer(transactionId, userId));
    }

    private UUID resolveUserId(UUID userIdHeader, Jwt jwt) {
        if (userIdHeader != null) {
            return userIdHeader;
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
        User user = userRepository.findByKeycloakId(jwt.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve authenticated user"));
        return user.getId();
    }
}
