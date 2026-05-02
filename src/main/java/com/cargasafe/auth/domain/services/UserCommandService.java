package com.cargasafe.auth.domain.services;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.domain.model.commands.SignInCommand;
import com.cargasafe.auth.domain.model.commands.SignUpCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignInCommand command);
    Optional<User> handle(SignUpCommand command);
}
