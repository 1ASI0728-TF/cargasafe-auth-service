package com.cargasafe.auth.domain.model.commands;

public record RegisterRefreshTokenCommand(Long userId, String refreshToken) {}
