package com.vaultcore.controller;

import com.vaultcore.service.StatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    /**
     * PRD §8 — Generate PDF statement for a given account and optional date range.
     * GET /api/v1/statements/{accountId}/pdf?startDate=&endDate=
     *
     * @param accountId  Internal account UUID
     * @param userId     Internal user UUID (from gateway header)
     * @param startDate  Optional start date (ISO, e.g. 2026-01-01)
     * @param endDate    Optional end date (ISO, e.g. 2026-03-31)
     */
    @GetMapping("/{accountId}/pdf")
    public ResponseEntity<byte[]> getStatementPdf(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfBytes = statementService.generateStatementForRange(accountId, userId, startDate, endDate);

        String filename = "statement-" + accountId
                + (startDate != null ? "-from-" + startDate : "")
                + (endDate   != null ? "-to-"   + endDate   : "")
                + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Legacy month-based endpoint for backward compatibility.
     * GET /api/v1/statements/{month}?accountId=
     */
    @GetMapping("/{month}")
    public ResponseEntity<byte[]> getStatementByMonth(
            @PathVariable String month,
            @RequestParam UUID accountId,
            @RequestHeader("X-User-Id") UUID userId) {
        byte[] pdfBytes = statementService.generateStatement(accountId, month, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + month + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping
    public ResponseEntity<String[]> listStatements() {
        return ResponseEntity.ok(new String[]{});
    }
}
