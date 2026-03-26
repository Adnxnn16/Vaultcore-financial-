package com.vaultcore.repository;

import com.vaultcore.entity.MfaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaTokenRepository extends JpaRepository<MfaToken, UUID> {
    Optional<MfaToken> findByTransactionReferenceAndUsedFalse(String transactionReference);
}
