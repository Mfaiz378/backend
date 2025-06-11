package com.example.repository;

import com.example.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
}
