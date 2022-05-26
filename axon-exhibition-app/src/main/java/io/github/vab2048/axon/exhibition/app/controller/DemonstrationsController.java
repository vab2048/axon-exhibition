package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateMultipleAccountsInATransactionCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateNewAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreditAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.DebitAccountCommand;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@RestController
public class DemonstrationsController implements Demonstrations {
    private static final Logger log = LoggerFactory.getLogger(DemonstrationsController.class);

    private final CommandGateway commandGateway;

    public DemonstrationsController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    /* *******************************************************************************
     * Set Based Validation
     * *******************************************************************************
     * We have a constraint: email address must be unique across the set of all account aggregates.
     *
     * Here we explore two scenarios:
     * (i)  Performing set based validation at the threshold of the aggregate.
     * (ii) Performing set based validation over the threshold of multiple aggregates.
     *
     * Both scenarios follow the same initial logic:
     *     Create N commands (between two and five):
     *       Command 1.  Create account with a unique email address.
     *       Command ... Create account with a unique email address.
     *       Command N.  Create account with same email address as the previous command.
     * Command N will violate the constraint and cause a rollback.
     *
     * For scenario (i) the rollback will only be for the final command - all aggregates before it will be created fine.
     * For scenario (ii) the rollback will be over ALL aggregates and NOT A SINGLE ONE will be created.
     */


    /**
     * All valid aggregates will be created - the account aggregate containing the
     * conflicting email address will not be created.
     */
    @Override
    public void setBasedValidation1() {
        int numUniqueCommandsToIssue = RandomUtils.nextInt(2, 5);
        var createNewAccountCommands = nextSetBasedValidationCommands(numUniqueCommandsToIssue);

        for(CreateNewAccountCommand command : createNewAccountCommands) {
            // We use the simple command bus so that the thread on which each of the commands
            // is dispatched remains constant. That way all commands will participate in the same
            // transaction and when one fails it will roll back all of them.
            commandGateway.sendAndWait(command);
        }
    }

    /**
     * The rollback will be over ALL aggregates and none of them will be created.
     */
    @Override
    public void setBasedValidation2() {
        int numUniqueCommandsToIssue = RandomUtils.nextInt(2, 5);
        var createNewAccountCommands = nextSetBasedValidationCommands(numUniqueCommandsToIssue);

        // Create the transaction command
        var transactionCommand = new CreateMultipleAccountsInATransactionCommand(createNewAccountCommands);

        // Issue the command (which will fail).
        commandGateway.sendAndWait(transactionCommand);
    }

    private List<CreateNewAccountCommand> nextUniqueNCreateNewAccountCommands(int n) {
        var commands = new ArrayList<CreateNewAccountCommand>();
        for(int i = 0; i < n; i++) {
            commands.add(nextCreateNewAccountCommand());
        }
        return commands;
    }

    /**
     * Will create a list of CreateNewAccountCommand objects where the first `numUniqueCommandsToIssue`
     * contain unique email addresses and so will not break the constraint.
     *
     * Then will append a command to the end with the same email address of the previous command - this
     * command will break the constraint.
     */
    private List<CreateNewAccountCommand> nextSetBasedValidationCommands(int numUniqueCommandsToIssue) {
        var commands = nextUniqueNCreateNewAccountCommands(numUniqueCommandsToIssue);
        // The last item in the list needs to use an existing email address.
        var lastUniqueCommand = commands.get(commands.size() - 1);
        var commandWhichBreaksConstraint = new CreateNewAccountCommand(UUID.randomUUID(), lastUniqueCommand.emailAddress());
        commands.add(commandWhichBreaksConstraint);
        return commands;
    }

    /*
     * Return a CreateNewAccountCommand with a random email address.
     */
    private CreateNewAccountCommand nextCreateNewAccountCommand() {
        var accountId = UUID.randomUUID();
        var accountEmailAddress = randomAlphanumeric(8) + "@" + randomAlphanumeric( 5) + "." + RandomStringUtils.randomAlphanumeric(2);
        return new CreateNewAccountCommand(accountId, accountEmailAddress);
    }

    private CreateNewAccountCommand nextCreateNewAccountCommand(UUID accountId) {
        var accountEmailAddress = randomAlphanumeric(8) + "@" + randomAlphanumeric( 5) + "." + RandomStringUtils.randomAlphanumeric(2);
        return new CreateNewAccountCommand(accountId, accountEmailAddress);
    }

    /**
     * This endpoint is for illustrating the snapshotting of an aggregate.
     * In our case the 'account' aggregate will be snapshotted based on the
     * configuration we have defined (after 3 events).
     *
     * So we will issue 10 commands to see the snapshotting occur three times.
     * To view the snapshotting - inspect the DB.
     */
    @Override
    public void triggerAccountSnapshot() {
        var accountId = UUID.randomUUID();

        // The credit/debit account commands requires a payment ID. We are not making payments (to make things
        // simpler) for this scenario, so we will just use a one time generated paymentID for all commands.
        var paymentId = UUID.randomUUID();

        // Create 10 commands:
        var command01 = nextCreateNewAccountCommand(accountId);
        var command02 = nextCreditOrDebitCommand(accountId, paymentId);
        var command03 = nextCreditOrDebitCommand(accountId, paymentId);
        var command04 = nextCreditOrDebitCommand(accountId, paymentId);
        var command05 = nextCreditOrDebitCommand(accountId, paymentId);
        var command06 = nextCreditOrDebitCommand(accountId, paymentId);
        var command07 = nextCreditOrDebitCommand(accountId, paymentId);
        var command08 = nextCreditOrDebitCommand(accountId, paymentId);
        var command09 = nextCreditOrDebitCommand(accountId, paymentId);
        var command10 = nextCreditOrDebitCommand(accountId, paymentId);

        // Issue the commands:
        log.debug("Issuing command 1/10: {}", command01);
        commandGateway.sendAndWait(command01);
        log.debug("Issuing command 2/10: {}", command02);
        commandGateway.sendAndWait(command02);
        log.debug("Issuing command 3/10: {}", command03);
        commandGateway.sendAndWait(command03);
        log.debug("Issuing command 4/10: {}", command04);
        commandGateway.sendAndWait(command04);
        log.debug("Issuing command 5/10: {}", command05);
        commandGateway.sendAndWait(command05);
        log.debug("Issuing command 6/10: {}", command06);
        commandGateway.sendAndWait(command06);
        log.debug("Issuing command 7/10: {}", command07);
        commandGateway.sendAndWait(command07);
        log.debug("Issuing command 8/10: {}", command08);
        commandGateway.sendAndWait(command08);
        log.debug("Issuing command 9/10: {}", command09);
        commandGateway.sendAndWait(command09);
        log.debug("Issuing command 10/10: {}", command10);
        commandGateway.sendAndWait(command10);

        // Now inspect the DB...
    }
    private Object nextCreditOrDebitCommand(UUID accountId, UUID paymentId) {
        // Return either a credit or debit account command randomly...
        var amount = 10L; // Just use this for an easy amount.
        return RandomUtils.nextBoolean() ? new DebitAccountCommand(accountId, paymentId, amount)
                : new CreditAccountCommand(accountId, paymentId, amount);
    }

}
