package com.hospital.repository;

import com.hospital.model.PharmacyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, Long> {
}
