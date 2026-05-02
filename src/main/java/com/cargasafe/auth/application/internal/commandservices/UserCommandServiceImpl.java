package com.cargasafe.auth.application.internal.commandservices;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.domain.model.commands.SignInCommand;
import com.cargasafe.auth.domain.model.commands.SignUpCommand;
import com.cargasafe.auth.domain.model.exceptions.*;
import com.cargasafe.auth.domain.services.UserCommandService;
import com.cargasafe.auth.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final BCryptHashingService hashingService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  BCryptHashingService hashingService,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException(command.email()));
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new InvalidCredentialsException();
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyInUseException(command.email());
        try {
            var roles = command.roles().stream()
                    .map(role -> roleRepository.findByName(role.getName())
                            .orElseThrow(() -> new RoleNotFoundException(role.getName())))
                    .toList();
            var user = new User(command.email(), hashingService.encode(command.password()), roles);
            userRepository.save(user);
            return userRepository.findByEmail(command.email());
        } catch (EmailAlreadyInUseException | RoleNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserRegistrationException("User registration failed", e);
        }
    }
}
