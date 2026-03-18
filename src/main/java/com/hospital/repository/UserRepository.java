package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<com.hospital.model.User, Long> {
    Optional<com.hospital.model.User> findByUsername(String username);
}
