package com.cargasafe.auth.domain.services;

import com.cargasafe.auth.domain.model.commands.LogoutAllDevicesCommand;
import com.cargasafe.auth.domain.model.commands.RefreshAccessTokenCommand;
import com.cargasafe.auth.domain.model.commands.RegisterRefreshTokenCommand;
import com.cargasafe.auth.domain.model.valueobjects.TokenPair;

public interface SessionCommandService {
    void handle(RegisterRefreshTokenCommand command);
    TokenPair handle(RefreshAccessTokenCommand command);
    void handle(LogoutAllDevicesCommand command);
}
