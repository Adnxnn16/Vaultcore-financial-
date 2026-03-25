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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class LatencyBenchmarkTest {

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

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("latency_user");
        user.setEmail("latency@vaultcore.com");
        user.setFullName("Latency User");
        userRepository.save(user);
        userId = user.getId();

        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber("LAT_SRC_1");
        sourceAccount.setUser(user);
        sourceAccount.setBalance(new BigDecimal("1000000.00"));
        sourceAccount.setCurrency("USD");
        sourceAccount.setType("CHECKING");
        sourceAccount.setActive(true);

        Account destAccount = new Account();
        destAccount.setAccountNumber("LAT_DST_1");
        destAccount.setUser(user);
        destAccount.setBalance(new BigDecimal("0.00"));
        destAccount.setCurrency("USD");
        destAccount.setType("CHECKING");
        destAccount.setActive(true);

        accountRepository.save(sourceAccount);
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
    void testTransferLatencyIsUnder50ms() {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("LAT_SRC_1")
                .destinationAccountNumber("LAT_DST_1")
                .amount(new BigDecimal("10.00"))
                .currency("USD")
                .description("Latency Test")
                .build();

        // Warmup (JVM JIT compilation)
        IntStream.range(0, 10).forEach(i -> transferService.transfer(request, userId));

        long startTime = System.nanoTime();

        int iterations = 100;
        IntStream.range(0, iterations).forEach(i -> transferService.transfer(request, userId));

        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        double averageLatencyMs = (double) durationMs / iterations;

        System.out.printf("Average Transfer Latency: %.2f ms%n", averageLatencyMs);

        // PRD goal is 50ms, locally we might be extremely fast (e.g., 5-10ms)
        assertTrue(averageLatencyMs < 50.0, "Average latency exceeded PRD requirement of 50ms. Actual: " + averageLatencyMs);
    }
}
