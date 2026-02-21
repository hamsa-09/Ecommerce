package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    UserAccount findByUsername(String username);
    UserAccount findByEmail(String email);
}
