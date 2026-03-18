package com.hospital.repository;

import com.hospital.model.NurseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NurseLogRepository extends JpaRepository<NurseLog, Long> {
}
