package com.cargasafe.auth.domain.services;

import com.cargasafe.auth.domain.model.entities.Role;
import com.cargasafe.auth.domain.model.queries.GetAllRolesQuery;
import com.cargasafe.auth.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
    List<Role> handle(GetAllRolesQuery query);
    Optional<Role> handle(GetRoleByNameQuery query);
}
