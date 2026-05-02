package com.cargasafe.auth.interfaces.rest.transform;

import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
