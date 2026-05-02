package com.cargasafe.auth.application.internal.queryservices;

import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.domain.model.queries.GetAllRolesQuery;
import com.cargasafe.auth.domain.model.queries.GetRoleByNameQuery;
import com.cargasafe.auth.domain.services.RoleQueryService;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name());
    }
}
