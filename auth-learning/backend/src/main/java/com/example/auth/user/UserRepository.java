package com.example.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Emails are stored lowercase (see AuthService.normalize), so a plain lookup is enough.
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
