package com.cargasafe.auth.domain.model.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Object identifier) {
        super("User not found: " + identifier);
    }
}
