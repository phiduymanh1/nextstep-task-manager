package org.example.nextstepbackend.Repositorys;

import org.example.nextstepbackend.Entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUserEmail(String email);
}
