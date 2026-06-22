package com.hospital.repo;

import com.hospital.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data generates the implementation at runtime. Method names are parsed
 * into queries: findByEmail -> "SELECT u FROM User u WHERE u.email = ?".
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
