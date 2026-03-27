package com.vaultcore.service;

import com.vaultcore.entity.Transaction;
import com.vaultcore.exception.AccountNotFoundException;
import com.vaultcore.repository.AccountRepository;
import com.vaultcore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatementService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Value("${app.statement.encryption-key:VaultCoreAES128K}")
    private String encryptionKey;

    public byte[] generateStatement(UUID accountId, String month) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        YearMonth yearMonth = YearMonth.parse(month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float y = 750;
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
            contentStream.showText("Account Statement — " + month);
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

            // AES-128 encryption
            return encryptPdf(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error generating statement for account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate statement", e);
        }
    }

    private byte[] encryptPdf(byte[] data) throws Exception {
        byte[] keyBytes = encryptionKey.getBytes();
        // Ensure 16-byte key for AES-128
        byte[] key16 = new byte[16];
        System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));

        SecretKeySpec secretKey = new SecretKeySpec(key16, "AES");
        
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        
        byte[] encrypted = cipher.doFinal(data);
        
        byte[] finalResult = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, finalResult, 0, iv.length);
        System.arraycopy(encrypted, 0, finalResult, iv.length, encrypted.length);
        
        return finalResult;
    }
}
