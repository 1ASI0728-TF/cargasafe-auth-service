package com.cargasafe.auth.domain.services;

import com.cargasafe.auth.domain.model.aggregates.User;
import com.cargasafe.auth.domain.model.queries.GetAllUsersQuery;
import com.cargasafe.auth.domain.model.queries.GetUserByEmailQuery;
import com.cargasafe.auth.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    List<User> handle(GetAllUsersQuery query);
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
}
