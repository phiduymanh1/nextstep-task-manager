package org.example.nextstepbackend.repository;

import java.util.Optional;
import org.example.nextstepbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
