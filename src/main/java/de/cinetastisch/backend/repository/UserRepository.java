package de.cinetastisch.backend.repository;

import de.cinetastisch.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getByEmail(String email);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
