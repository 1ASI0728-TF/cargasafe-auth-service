package com.cargasafe.auth.domain.model.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Object name) {
        super("Role not found: " + name);
    }
}
