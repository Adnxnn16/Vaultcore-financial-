package com.vaultcore.service;

import com.vaultcore.dto.LedgerEntryResponse;
import com.vaultcore.entity.LedgerEntry;
import com.vaultcore.repository.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> listAll(Pageable pageable) {
        return ledgerEntryRepository.findAllPaged(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> findByTransactionId(UUID transactionId) {
        return ledgerEntryRepository.findByTransactionIdOrderByEntryTypeAsc(transactionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LedgerEntryResponse toResponse(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId().toString(),
                e.getAccount().getId().toString(),
                e.getTransaction().getId().toString(),
                e.getEntryType(),
                e.getAmount(),
                e.getBalanceAfter(),
                e.getCreatedAt()
        );
    }
}
