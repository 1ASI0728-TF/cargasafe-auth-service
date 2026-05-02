package com.cargasafe.auth.application.internal.commandservices;

import com.cargasafe.auth.domain.model.commands.SeedRolesCommand;
import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.domain.model.valueobjects.Roles;
import com.cargasafe.auth.domain.services.RoleCommandService;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(role));
            }
        });
    }
}
