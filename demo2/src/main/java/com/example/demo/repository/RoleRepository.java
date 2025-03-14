package com.example.demo.repository;

import com.example.demo.entity.Role;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface RoleRepository  extends JpaRepository<Role, Long> {

    Optional<Role> findRoleByName(String name);
}
