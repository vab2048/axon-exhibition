package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateNewAccountCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DemonstrationsController implements Demonstrations {
    private static final Logger log = LoggerFactory.getLogger(DemonstrationsController.class);

    private final CommandGateway commandGateway;

    public DemonstrationsController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    /*
     * Our constraint is that an email address must be unique across the set of all account aggregates.
     * We will test that constraint holds by attempting to create two accounts with the same email address.
     */
    @Override
    @Transactional // <--- not needed but signals intent and gives better log output for you to see what is happening.
    public void setBasedValidation() {
        var emailAddress = "example@example.com";
        var cmd1 = new CreateNewAccountCommand(UUID.randomUUID(), emailAddress);
        var cmd2 = new CreateNewAccountCommand(UUID.randomUUID(), emailAddress);

        // If the email address already exists in the DB then the uniqueness constraint will be violated.
        log.info("Issuing command 1: {}", cmd1);
        UUID account1Id = commandGateway.sendAndWait(cmd1);
        log.info("Account 1 created with ID: {}", account1Id);
        log.info("Issuing command 2: {}", cmd2);
        commandGateway.sendAndWait(cmd2);
    }
}
