package com.cargasafe.auth.domain.services;

import com.cargasafe.auth.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
