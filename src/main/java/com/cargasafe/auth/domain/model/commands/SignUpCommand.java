package com.cargasafe.auth.domain.model.commands;

import com.cargasafe.auth.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(String email, String password, List<Role> roles) {}
