package com.vaultcore.service;

import com.vaultcore.dto.AccountResponse;
import com.vaultcore.dto.CreateAccountRequest;
import com.vaultcore.dto.TransactionResponse;
import com.vaultcore.entity.Account;
import com.vaultcore.entity.Transaction;
import com.vaultcore.entity.User;
import com.vaultcore.exception.AccountNotFoundException;
import com.vaultcore.repository.AccountRepository;
import com.vaultcore.repository.TransactionRepository;
import com.vaultcore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        return toResponse(account);
    }

    @Cacheable(value = "balance", key = "#accountId")
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<BigDecimal> future = executor.submit(() -> getBalanceInternal(accountId));
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Balance lookup interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Balance lookup failed", cause);
        }
    }

    private BigDecimal getBalanceInternal(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        return account.getBalance();
    }

    @CacheEvict(value = "balance", key = "#accountId")
    public void evictBalanceCache(UUID accountId) {
        log.debug("Balance cache evicted for account: {}", accountId);
    }

    @Transactional
    public Account createWelcomeAccount(User user) {
        log.info("Creating welcome account for user: {}", user.getUsername());
        Account account = Account.builder()
                .accountNumber("VC-" + (100000 + (int)(Math.random() * 900000)))
                .user(user)
                .type("CHECKING")
                .balance(new BigDecimal("1000.0000"))
                .currency("USD")
                .active(true)
                .build();
        return accountRepository.save(account);
    }

    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("User not found: " + userId));
        String currency = request.getCurrency().trim().toUpperCase();
        Account account = Account.builder()
                .accountNumber("VC-" + (100000 + (int) (Math.random() * 900000)))
                .user(user)
                .type("CHECKING")
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .nickname(request.getNickname())
                .active(true)
                .build();
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsForAccount(UUID accountId, UUID userId, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        if (!account.getUser().getId().equals(userId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return transactionRepository.findByAccountId(accountId, pageable).map(this::toTransactionResponse);
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId().toString())
                .referenceNumber(t.getReferenceNumber())
                .sourceAccountNumber(t.getSourceAccount().getAccountNumber())
                .destinationAccountNumber(t.getDestinationAccount().getAccountNumber())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .transactionType(t.getTransactionType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId() != null ? account.getId().toString() : null)
                .accountNumber(account.getAccountNumber())
                .accountType(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .active(account.getActive())
                .createdAt(account.getCreatedAt())
                .ownerName(account.getUser() != null ? account.getUser().getFullName() : null)
                .nickname(account.getNickname())
                .build();
    }
}
