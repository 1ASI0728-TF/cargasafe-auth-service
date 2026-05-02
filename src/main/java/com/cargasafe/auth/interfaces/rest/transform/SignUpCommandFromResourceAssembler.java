package com.cargasafe.auth.interfaces.rest.transform;

import com.cargasafe.auth.domain.model.commands.SignUpCommand;
import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        List<Role> roles = (resource.roles() != null && !resource.roles().isEmpty())
                ? resource.roles().stream().map(Role::toRoleFromName).toList()
                : List.of(Role.getDefaultRole());
        return new SignUpCommand(resource.email(), resource.password(), roles);
    }
}
