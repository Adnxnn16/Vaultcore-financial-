package com.vaultcore.controller;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.dto.TransferResponse;
import com.vaultcore.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        UUID userUUID = userId != null ? UUID.fromString(userId) : UUID.randomUUID();
        TransferResponse response = transferService.transfer(request, userUUID);
        return ResponseEntity.ok(response);
    }
}
