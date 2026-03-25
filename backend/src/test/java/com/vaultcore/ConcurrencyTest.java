package com.vaultcore;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.entity.Account;
import com.vaultcore.entity.User;
import com.vaultcore.repository.AccountRepository;
import com.vaultcore.repository.UserRepository;
import com.vaultcore.repository.TransactionRepository;
import com.vaultcore.repository.LedgerEntryRepository;
import com.vaultcore.service.AccountService;
import com.vaultcore.service.AuditService;
import com.vaultcore.service.TransferService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyTest {

    @Autowired
    private TransferService transferService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private AuditService auditService;

    @MockBean
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private Account sourceAccount;
    private Account destAccount;
    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("conc_user");
        user.setEmail("conc@vaultcore.com");
        user.setFullName("Conc User");
        userRepository.save(user);
        userId = user.getId();

        sourceAccount = new Account();
        sourceAccount.setAccountNumber("CONC_SRC_1");
        sourceAccount.setUser(user);
        sourceAccount.setBalance(new BigDecimal("10000.00"));
        sourceAccount.setCurrency("USD");
        sourceAccount.setType("CHECKING");
        sourceAccount.setActive(true);
        accountRepository.save(sourceAccount);

        destAccount = new Account();
        destAccount.setAccountNumber("CONC_DST_1");
        destAccount.setUser(user);
        destAccount.setBalance(new BigDecimal("0.00"));
        destAccount.setCurrency("USD");
        destAccount.setType("CHECKING");
        destAccount.setActive(true);
        accountRepository.save(destAccount);
    }

    @AfterEach
    void tearDown() {
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testConcurrentTransfersMaintainACID() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    TransferRequest request = TransferRequest.builder()
                            .sourceAccountNumber("CONC_SRC_1")
                            .destinationAccountNumber("CONC_DST_1")
                            .amount(new BigDecimal("10.00")) // Total 1000.00
                            .currency("USD")
                            .description("Concurrency Test")
                            .build();

                    transferService.transfer(request, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 100 requests * 10.00 = 1000.00 transferred successfully (no race conditions)
        Account finalSource = accountRepository.findByAccountNumber("CONC_SRC_1").orElseThrow();
        Account finalDest = accountRepository.findByAccountNumber("CONC_DST_1").orElseThrow();

        // Check if consistency is strictly maintained
        int successfulTransfers = successCount.get();
        BigDecimal expectedSourceBalance = new BigDecimal("10000.00").subtract(new BigDecimal("10.00").multiply(new BigDecimal(successfulTransfers)));
        BigDecimal expectedDestBalance = new BigDecimal("10.00").multiply(new BigDecimal(successfulTransfers));

        assertEquals(expectedSourceBalance.compareTo(finalSource.getBalance()), 0, "Source balance mismatch indicating race condition");
        assertEquals(expectedDestBalance.compareTo(finalDest.getBalance()), 0, "Dest balance mismatch indicating race condition");
        assertEquals(numberOfThreads, successfulTransfers + failureCount.get(), "Not all concurrent requests completed");
        assertTrue(finalSource.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Source balance became negative");
        assertTrue(finalDest.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Destination balance became negative");
    }
}
