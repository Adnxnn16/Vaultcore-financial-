package com.vaultcore.repository;

import com.vaultcore.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Transaction t JOIN FETCH t.sourceAccount sa JOIN FETCH sa.user JOIN FETCH t.destinationAccount da JOIN FETCH da.user WHERE t.id = :id")
    Optional<Transaction> findByIdWithParticipants(@Param("id") UUID id);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
