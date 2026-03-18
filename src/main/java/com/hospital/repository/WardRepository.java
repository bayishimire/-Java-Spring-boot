package com.hospital.repository;

import com.hospital.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    java.util.Optional<com.hospital.model.Ward> findByName(String name);
}
