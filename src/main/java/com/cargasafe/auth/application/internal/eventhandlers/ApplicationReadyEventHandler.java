package com.cargasafe.auth.application.internal.eventhandlers;

import com.cargasafe.auth.domain.model.commands.SeedRolesCommand;
import com.cargasafe.auth.domain.services.RoleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ApplicationReadyEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);
    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        LOGGER.info("Seeding roles at {}", new Timestamp(System.currentTimeMillis()));
        roleCommandService.handle(new SeedRolesCommand());
        LOGGER.info("Roles seeding complete.");
    }
}
