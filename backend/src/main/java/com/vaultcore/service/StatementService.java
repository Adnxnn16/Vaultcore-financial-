package com.vaultcore.service;

import com.vaultcore.entity.Transaction;
import com.vaultcore.exception.AccountNotFoundException;
import com.vaultcore.repository.AccountRepository;
import com.vaultcore.repository.TransactionRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private static final Logger log = LoggerFactory.getLogger(StatementService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public StatementService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public byte[] generateStatement(UUID accountId, String month, UUID requesterUserId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        if (!account.getUser().getId().equals(requesterUserId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return generateStatement(accountId, month);
    }

    /**
     * Generate a PDF statement for a flexible date range.
     * If startDate or endDate are null, defaults to the current calendar month.
     */
    public byte[] generateStatementForRange(UUID accountId, UUID requesterUserId, LocalDate startDate, LocalDate endDate) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
        if (!account.getUser().getId().equals(requesterUserId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEnd   = (endDate   != null) ? endDate   : LocalDate.now();
        LocalDateTime start = effectiveStart.atStartOfDay();
        LocalDateTime end   = effectiveEnd.atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
        String period = effectiveStart + " to " + effectiveEnd;

        return buildPdf(account, transactions, period);
    }

    public byte[] generateStatement(UUID accountId, String month) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        YearMonth yearMonth = YearMonth.parse(month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
        return buildPdf(account, transactions, month);
    }

    // ── Shared PDF builder ───────────────────────────────────────────────────────

    private byte[] buildPdf(com.vaultcore.entity.Account account, List<Transaction> transactions, String period) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float y      = 750;
            float margin = 50;

            // Header
            contentStream.beginText();
            contentStream.setFont(fontBold, 20);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("VaultCore Financial");
            contentStream.endText();
            y -= 30;

            contentStream.beginText();
            contentStream.setFont(fontRegular, 12);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Account Statement — " + period);
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(fontRegular, 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Account: " + account.getAccountNumber() + " | Balance: $" + account.getBalance());
            contentStream.endText();
            y -= 30;

            // Separator line
            contentStream.moveTo(margin, y);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, y);
            contentStream.stroke();
            y -= 20;

            // Column headers
            contentStream.beginText();
            contentStream.setFont(fontBold, 9);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText(String.format("%-20s %-15s %-15s %-10s %s", "Date", "Reference", "Type", "Amount", "Status"));
            contentStream.endText();
            y -= 15;

            // Transaction rows
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Transaction tx : transactions) {
                if (y < 80) break; // page break safety

                contentStream.beginText();
                contentStream.setFont(fontRegular, 8);
                contentStream.newLineAtOffset(margin, y);
                String line = String.format("%-20s %-15s %-15s $%-9s %s",
                        tx.getCreatedAt().format(fmt),
                        tx.getReferenceNumber().substring(0, Math.min(14, tx.getReferenceNumber().length())),
                        tx.getTransactionType(),
                        tx.getAmount().toPlainString(),
                        tx.getStatus());
                contentStream.showText(line);
                contentStream.endText();
                y -= 12;
            }

            // Footer
            y = 50;
            contentStream.beginText();
            contentStream.setFont(fontRegular, 8);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Generated: " + LocalDateTime.now().format(fmt) + " | This is a system-generated statement.");
            contentStream.endText();

            contentStream.close();
            document.save(baos);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating statement: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate statement", e);
        }
    }
}
