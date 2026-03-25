package com.vaultcore.service;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.dto.TransferResponse;
import com.vaultcore.entity.Account;
import com.vaultcore.entity.Transaction;
import com.vaultcore.entity.LedgerEntry;
import com.vaultcore.entity.User;
import com.vaultcore.exception.AccountNotFoundException;
import com.vaultcore.exception.InsufficientBalanceException;
import com.vaultcore.repository.AccountRepository;
import com.vaultcore.repository.LedgerEntryRepository;
import com.vaultcore.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransferService transferService;

    private Account sourceAccount;
    private Account destAccount;
    private TransferRequest request;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        sourceAccount = new Account();
        sourceAccount.setId(UUID.randomUUID());
        sourceAccount.setAccountNumber("SRC123");
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setCurrency("USD");
        sourceAccount.setActive(true);
        User srcUser = new User();
        srcUser.setId(userId);
        sourceAccount.setUser(srcUser);

        destAccount = new Account();
        destAccount.setId(UUID.randomUUID());
        destAccount.setAccountNumber("DEST456");
        destAccount.setBalance(new BigDecimal("500.00"));
        destAccount.setCurrency("USD");
        destAccount.setActive(true);
        User dstUser = new User();
        dstUser.setId(UUID.randomUUID());
        destAccount.setUser(dstUser);

        request = TransferRequest.builder()
                .sourceAccountNumber("SRC123")
                .destinationAccountNumber("DEST456")
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .description("Test Transfer")
                .build();
    }

    @Test
    void testTransfer_Success() {
        // Arrange
        when(accountRepository.findByAccountNumberForUpdate("DEST456"))
                .thenReturn(Optional.of(destAccount));
        when(accountRepository.findByAccountNumberForUpdate("SRC123"))
                .thenReturn(Optional.of(sourceAccount));

        // Act
        TransferResponse response = transferService.transfer(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getAmount());

        // Verify balances update
        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), destAccount.getBalance());

        // Verify repositories were called
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    void testTransfer_HighValue_Success() {
        // Arrange
        request.setAmount(new BigDecimal("15000.00"));
        sourceAccount.setBalance(new BigDecimal("20000.00"));

        when(accountRepository.findByAccountNumberForUpdate("DEST456"))
                .thenReturn(Optional.of(destAccount));
        when(accountRepository.findByAccountNumberForUpdate("SRC123"))
                .thenReturn(Optional.of(sourceAccount));

        // Act
        TransferResponse response = transferService.transfer(request, userId);

        // Assert
        assertEquals("COMPLETED", response.getStatus());
        verify(transactionRepository).save(any());
    }

    @Test
    void testTransfer_SameAccount() {
        request.setDestinationAccountNumber("SRC123");
        
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(request, userId);
        });

        assertEquals("Source and destination accounts must be different", ex.getMessage());
    }

    @Test
    void testTransfer_InsufficientFunds() {
        // Arrange
        request.setAmount(new BigDecimal("2000.00"));
        
        when(accountRepository.findByAccountNumberForUpdate("DEST456"))
                .thenReturn(Optional.of(destAccount));
        when(accountRepository.findByAccountNumberForUpdate("SRC123"))
                .thenReturn(Optional.of(sourceAccount));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.transfer(request, userId);
        });
    }
}
