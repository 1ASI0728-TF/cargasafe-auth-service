package com.cargasafe.auth.interfaces.rest.transform;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getEmail(), user.getSerializedRoles(), token);
    }
}
