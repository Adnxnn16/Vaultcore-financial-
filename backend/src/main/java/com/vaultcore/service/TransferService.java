package com.vaultcore.service;

import com.vaultcore.dto.TransferDetailResponse;
import com.vaultcore.dto.TransferRequest;
import com.vaultcore.dto.TransferResponse;
import com.vaultcore.entity.*;
import com.vaultcore.exception.AccountNotFoundException;
import com.vaultcore.exception.InsufficientBalanceException;
import com.vaultcore.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRITICAL: Double-entry ledger transfer engine.
 * - SERIALIZABLE isolation
 * - SELECT FOR UPDATE on accounts
 * - Atomic DEBIT + CREDIT entries
 * - Immutable ledger writes
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditService auditService;
    private final AccountService accountService;

    public TransferService(AccountRepository accountRepository, 
                          TransactionRepository transactionRepository, 
                          LedgerEntryRepository ledgerEntryRepository, 
                          AuditService auditService,
                          AccountService accountService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.auditService = auditService;
        this.accountService = accountService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransferResponse transfer(TransferRequest request, UUID userId) {
        resolveAccountIdentifiers(request);
        if (request.getSourceAccountNumber() == null || request.getSourceAccountNumber().isBlank()) {
            throw new IllegalArgumentException("Source account number or fromAccountId is required");
        }
        if (request.getDestinationAccountNumber() == null || request.getDestinationAccountNumber().isBlank()) {
            throw new IllegalArgumentException("Destination account number or toAccountId is required");
        }

        log.info("Transfer initiated: {} -> {}, amount: {}",
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount());


        // Validate source != destination
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        // SELECT FOR UPDATE — acquire row-level locks (ordered to prevent deadlock)
        String firstAcct = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getSourceAccountNumber() : request.getDestinationAccountNumber();
        String secondAcct = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getDestinationAccountNumber() : request.getSourceAccountNumber();

        Account first = accountRepository.findByAccountNumberForUpdate(firstAcct)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + firstAcct));
        Account second = accountRepository.findByAccountNumberForUpdate(secondAcct)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + secondAcct));

        Account sourceAccount = first.getAccountNumber().equals(request.getSourceAccountNumber()) ? first : second;
        Account destAccount = first.getAccountNumber().equals(request.getDestinationAccountNumber()) ? first : second;

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Source account does not belong to the authenticated user");
        }

        // Validate balance
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + sourceAccount.getBalance() + ", Required: " + request.getAmount());
        }

        // Generate reference
        String referenceNumber = generateReferenceNumber();

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .referenceNumber(referenceNumber)
                .sourceAccount(sourceAccount)
                .destinationAccount(destAccount)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status("COMPLETED")
                .description(request.getDescription())
                .transactionType("TRANSFER")
                .completedAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // === DOUBLE-ENTRY BOOKKEEPING ===

        // DEBIT entry (source account)
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(request.getAmount());
        if (newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Transfer rejected: resulting source balance would be negative");
        }
        LedgerEntry debitEntry = LedgerEntry.builder()
                .transaction(transaction)
                .account(sourceAccount)
                .entryType("DEBIT")
                .amount(request.getAmount())
                .balanceAfter(newSourceBalance)
                .build();
        ledgerEntryRepository.save(debitEntry);

        // CREDIT entry (destination account)
        BigDecimal newDestBalance = destAccount.getBalance().add(request.getAmount());
        LedgerEntry creditEntry = LedgerEntry.builder()
                .transaction(transaction)
                .account(destAccount)
                .entryType("CREDIT")
                .amount(request.getAmount())
                .balanceAfter(newDestBalance)
                .build();
        ledgerEntryRepository.save(creditEntry);

        // Update account balances
        sourceAccount.setBalance(newSourceBalance);
        destAccount.setBalance(newDestBalance);
        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);

        accountService.evictBalanceCache(sourceAccount.getId());
        accountService.evictBalanceCache(destAccount.getId());

        log.info("Transfer completed: {} (ref: {})", request.getAmount(), referenceNumber);

        return TransferResponse.builder()
                .referenceNumber(referenceNumber)
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destAccount.getAccountNumber())
                .amount(request.getAmount())
                .currency(transaction.getCurrency())
                .status("COMPLETED")
                .description(request.getDescription())
                .completedAt(transaction.getCompletedAt())
                .mfaRequired(false)
                .message("Transfer completed successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public TransferDetailResponse getTransfer(UUID transactionId, UUID requesterUserId) {
        Transaction t = transactionRepository.findByIdWithParticipants(transactionId)
                .orElseThrow(() -> new AccountNotFoundException("Transfer not found"));
        UUID srcUser = t.getSourceAccount().getUser().getId();
        UUID dstUser = t.getDestinationAccount().getUser().getId();
        if (!requesterUserId.equals(srcUser) && !requesterUserId.equals(dstUser)) {
            throw new AccountNotFoundException("Transfer not found");
        }
        List<String> ledgerIds = ledgerEntryRepository.findByTransactionIdOrderByEntryTypeAsc(transactionId).stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.toList());
        return new TransferDetailResponse(
                t.getId().toString(),
                t.getReferenceNumber(),
                t.getSourceAccount().getAccountNumber(),
                t.getDestinationAccount().getAccountNumber(),
                t.getAmount(),
                t.getCurrency(),
                t.getStatus(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getCompletedAt(),
                ledgerIds
        );
    }

    private void resolveAccountIdentifiers(TransferRequest request) {
        if (request.getFromAccountId() != null) {
            Account src = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getFromAccountId()));
            request.setSourceAccountNumber(src.getAccountNumber());
        }
        if (request.getToAccountId() != null) {
            Account dst = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + request.getToAccountId()));
            request.setDestinationAccountNumber(dst.getAccountNumber());
        }
    }

    private String generateReferenceNumber() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
