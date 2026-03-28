package com.vaultcore.controller;

import com.vaultcore.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{month}")
    public ResponseEntity<byte[]> getStatement(
            @PathVariable String month,
            @RequestParam UUID accountId) {
        byte[] pdfBytes = statementService.generateStatement(accountId, month);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + month + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping
    public ResponseEntity<String[]> listStatements() {
        // Placeholder list until statement index table is implemented per PRD
        return ResponseEntity.ok(new String[]{});
    }
}
