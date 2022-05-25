package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.query.account.AccountView;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateNewAccountCommand;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.CreateAccountRequestBody;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.CreateAccountResponseBody;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetAccountView;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class AccountsController implements Accounts {
    private static final Logger log = LoggerFactory.getLogger(AccountsController.class);

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public AccountsController(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @Override
    public ResponseEntity<CreateAccountResponseBody> createNewAccount(CreateAccountRequestBody requestBody) {
        // Create the new account...
        String emailAddress = requestBody.emailAddress();
        UUID id = commandGateway.sendAndWait(new CreateNewAccountCommand(UUID.randomUUID(), emailAddress));

        // Get the URI for the newly created REST resource...
        var locationURI = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(id).toUri();

        return ResponseEntity
                .created(locationURI)
                .body(new CreateAccountResponseBody(id, emailAddress));
    }

    @Override
    public AccountView getAccount(UUID id) {
        var query = new GetAccountView(id);
        try {
            return queryGateway.query(query, AccountView.class).get();
        } catch (InterruptedException | ExecutionException e) {
            var msg = "Error in executing query: %s".formatted(query);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
