package com.vaultcore.repository;

import com.vaultcore.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    List<LedgerEntry> findByTransactionIdOrderByEntryTypeAsc(UUID transactionId);

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    @EntityGraph(attributePaths = {"account", "transaction"})
    @Query("SELECT le FROM LedgerEntry le ORDER BY le.createdAt DESC")
    Page<LedgerEntry> findAllPaged(Pageable pageable);
}
