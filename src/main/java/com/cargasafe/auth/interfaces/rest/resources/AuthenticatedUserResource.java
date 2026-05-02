package com.cargasafe.auth.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(Long id, String email, List<String> roles, String token) {}
