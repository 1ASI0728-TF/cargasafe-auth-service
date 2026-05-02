package com.cargasafe.auth.application.internal.outboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Calls profile-service via HTTP to create a partial profile after sign-up.
 * Replaces the in-process ProfileContextFacade ACL call from the monolith.
 */
@Service
public class ExternalProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProfileService.class);

    private final RestClient restClient;

    public ExternalProfileService(@Value("${integrations.profile-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void createPartialProfile(Long userId, String firstName, String lastName) {
        try {
            restClient.post()
                    .uri("/api/v1/profiles/partial")
                    .body(Map.of(
                            "userId", userId,
                            "firstName", firstName,
                            "lastName", lastName
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Profile creation failure must not roll back user registration.
            // The profile-service should handle retries or compensation.
            LOGGER.error("Failed to create partial profile for userId={}: {}", userId, e.getMessage());
        }
    }
}
