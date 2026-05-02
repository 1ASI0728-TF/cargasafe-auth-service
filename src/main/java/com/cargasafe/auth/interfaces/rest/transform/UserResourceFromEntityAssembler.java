package com.cargasafe.auth.interfaces.rest.transform;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(user.getId(), user.getEmail(), user.getSerializedRoles());
    }
}
