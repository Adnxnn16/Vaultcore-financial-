package com.vaultcore.controller;

import com.vaultcore.dto.AccountResponse;
import com.vaultcore.dto.CreateAccountRequest;
import com.vaultcore.dto.TransactionResponse;
import com.vaultcore.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(accountService.createAccount(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccountsByUser(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    /** Path-variable alias — used by some frontend calls */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserPath(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.getTransactionsForAccount(accountId, userId, pageable));
    }
}
