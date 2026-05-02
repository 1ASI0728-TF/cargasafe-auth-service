package com.cargasafe.auth.application.internal.queryservices;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.domain.model.queries.GetAllUsersQuery;
import com.cargasafe.auth.domain.model.queries.GetUserByEmailQuery;
import com.cargasafe.auth.domain.model.queries.GetUserByIdQuery;
import com.cargasafe.auth.domain.services.UserQueryService;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }
}
