package com.vaultcore.repository;

import com.vaultcore.entity.StockPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockPositionRepository extends JpaRepository<StockPosition, UUID> {
    List<StockPosition> findByUser_Id(UUID userId);
    Optional<StockPosition> findByUser_IdAndSymbol(UUID userId, String symbol);
}
