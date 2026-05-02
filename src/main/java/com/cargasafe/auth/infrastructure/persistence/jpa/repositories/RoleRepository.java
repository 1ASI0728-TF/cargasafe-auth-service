package com.cargasafe.auth.infrastructure.persistence.jpa.repositories;

import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Roles name);
    boolean existsByName(Roles name);
}
