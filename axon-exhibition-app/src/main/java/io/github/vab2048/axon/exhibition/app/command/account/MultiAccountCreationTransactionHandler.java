package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateMultipleAccountsInATransactionCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateNewAccountCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

/**
 * Component to handle the single transaction of creating multiple accounts at the same time.
 *
 * Also: to showcase how to use the SimpleCommandBus, and a command with a @RoutingKey
 * annotation (CreateMultipleAccountsInATransactionCommand).
 */
@Component
public class MultiAccountCreationTransactionHandler {
    private static final Logger log = LoggerFactory.getLogger(MultiAccountCreationTransactionHandler.class);
    private final SimpleCommandBus simpleCommandBus;

    public MultiAccountCreationTransactionHandler(SimpleCommandBus simpleCommandBus) {
        this.simpleCommandBus = simpleCommandBus;
    }

    @CommandHandler
    void handle(CreateMultipleAccountsInATransactionCommand transactionCommand) {
        log.debug("Handling: {}", transactionCommand);

        for(CreateNewAccountCommand command : transactionCommand.creationCommands()) {
            // We use the simple command bus so that the thread on which each of the commands
            // is dispatched remains constant. That way all commands will participate in the same
            // transaction and when one fails it will roll back all of them.
            simpleCommandBus.dispatch(asCommandMessage(command));
        }

    }

}
