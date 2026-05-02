package com.cargasafe.auth.interfaces.rest.transform;

import com.cargasafe.auth.domain.model.commands.SignInCommand;
import com.cargasafe.auth.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}
