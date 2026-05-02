package com.cargasafe.auth.interfaces.rest;

import com.cargasafe.auth.application.internal.commandservices.TokenRevocationCommandService;
import com.cargasafe.auth.application.internal.outboundservices.ExternalProfileService;
import com.cargasafe.auth.domain.model.commands.*;
import com.cargasafe.auth.domain.model.queries.GetUserByEmailQuery;
import com.cargasafe.auth.domain.services.SessionCommandService;
import com.cargasafe.auth.domain.services.UserCommandService;
import com.cargasafe.auth.domain.services.UserQueryService;
import com.cargasafe.auth.infrastructure.tokens.jwt.BearerTokenService;
import com.cargasafe.auth.interfaces.rest.resources.*;
import com.cargasafe.auth.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.cargasafe.auth.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.cargasafe.auth.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.cargasafe.auth.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;
    private final BearerTokenService tokenService;
    private final TokenRevocationCommandService tokenRevocationCommandService;
    private final UserQueryService userQueryService;
    private final ExternalProfileService externalProfileService;
    private final SessionCommandService sessionCommandService;

    public AuthenticationController(UserCommandService userCommandService,
                                    BearerTokenService tokenService,
                                    TokenRevocationCommandService tokenRevocationCommandService,
                                    UserQueryService userQueryService,
                                    ExternalProfileService externalProfileService,
                                    SessionCommandService sessionCommandService) {
        this.userCommandService = userCommandService;
        this.tokenService = tokenService;
        this.tokenRevocationCommandService = tokenRevocationCommandService;
        this.userQueryService = userQueryService;
        this.externalProfileService = externalProfileService;
        this.sessionCommandService = sessionCommandService;
    }

    @Operation(summary = "Sign in", description = "Authenticates a user and returns access + refresh tokens.")
    @PostMapping("/sign-in")
    public ResponseEntity<TokenPairResource> signIn(@RequestBody @Valid SignInResource resource) {
        var result = userCommandService.handle(SignInCommandFromResourceAssembler.toCommandFromResource(resource));
        if (result.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var user = result.get();
        String accessToken = tokenService.generateToken(user.getEmail(), user.getId(), user.getSerializedRoles());
        String refreshToken = tokenService.allocateRefreshToken(user.getEmail());
        sessionCommandService.handle(new RegisterRefreshTokenCommand(user.getId(), refreshToken));

        return ResponseEntity.ok(new TokenPairResource(accessToken, refreshToken));
    }

    @Operation(summary = "Sign up", description = "Registers a new user and creates a partial profile in the profile-service.")
    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signUp(@RequestBody @Valid SignUpResource resource) {
        var user = userCommandService.handle(SignUpCommandFromResourceAssembler.toCommandFromResource(resource));
        if (user.isEmpty()) return ResponseEntity.badRequest().build();

        externalProfileService.createPartialProfile(
                user.get().getId(),
                resource.profile().firstName(),
                resource.profile().lastName()
        );

        return new ResponseEntity<>(UserResourceFromEntityAssembler.toResourceFromEntity(user.get()), HttpStatus.CREATED);
    }

    @Operation(summary = "Verify token", description = "Validates the access token and returns authenticated user info.")
    @PostMapping("/verify-token")
    public ResponseEntity<AuthenticatedUserResource> verifyToken(HttpServletRequest request) {
        String token = tokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String email = tokenService.getUsernameFromToken(token);
        var userOpt = userQueryService.handle(new GetUserByEmailQuery(email));
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(
                AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(userOpt.get(), token)
        );
    }

    @Operation(summary = "Refresh token", description = "Issues a new token pair from a valid refresh token.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResource> refresh(@RequestBody RefreshTokenResource body) {
        if (body.refreshToken() == null || body.refreshToken().isBlank())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var pair = sessionCommandService.handle(new RefreshAccessTokenCommand(body.refreshToken()));
        return ResponseEntity.ok(new TokenPairResource(pair.accessToken(), pair.refreshToken()));
    }

    @Operation(summary = "Logout", description = "Revokes the provided refresh token.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenResource body) {
        if (body.refreshToken() == null || body.refreshToken().isBlank())
            return ResponseEntity.badRequest().build();
        tokenRevocationCommandService.handle(new RevokeTokenCommand(body.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout all devices", description = "Revokes all refresh tokens for the authenticated user.")
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        String token = tokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token) || tokenService.isRefreshToken(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String email = tokenService.getUsernameFromToken(token);
        var userOpt = userQueryService.handle(new GetUserByEmailQuery(email));
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        sessionCommandService.handle(new LogoutAllDevicesCommand(userOpt.get().getId()));
        return ResponseEntity.noContent().build();
    }
}
