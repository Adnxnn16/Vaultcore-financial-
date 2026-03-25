package com.vaultcore.controller;

import com.vaultcore.dto.LedgerEntryResponse;
import com.vaultcore.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public ResponseEntity<Page<LedgerEntryResponse>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ledgerService.listAll(pageable));
    }

    @GetMapping("/{txnId}")
    public ResponseEntity<List<LedgerEntryResponse>> byTransaction(@PathVariable UUID txnId) {
        return ResponseEntity.ok(ledgerService.findByTransactionId(txnId));
    }
}
