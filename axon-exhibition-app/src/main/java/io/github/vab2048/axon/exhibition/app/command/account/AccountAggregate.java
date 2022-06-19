package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.app.config.ApplicationAxonConfiguration;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.github.vab2048.axon.exhibition.app.config.ApplicationAxonConfiguration.ACCOUNT_AGGREGATE_SNAPSHOT_TRIGGER_DEFINITION_BEAN_NAME;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * A bank account aggregate.
 *
 * This is just a bare-bones implementation for the purpose of a demo.
 * It does not do anything special to deal with negative balances.
 */
@Aggregate(snapshotTriggerDefinition = ACCOUNT_AGGREGATE_SNAPSHOT_TRIGGER_DEFINITION_BEAN_NAME)
public class AccountAggregate {
    private static final Logger log = LoggerFactory.getLogger(AccountAggregate.class);

    /* *****************************************************
     * Fields
     * *****************************************************/

    // Since we are using Jackson as a serializer we unfortunately need getters and setters for each field
    // otherwise state will not be persisted when we take a snapshot.

    /**
     * Identifier for the account.
     */
    @AggregateIdentifier
    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    /**
     * Balance of the account in pennies.
     */
    private long balance;

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    // -------------------------------------------------------------------------------

    @Deprecated
    AccountAggregate() { /* For framework use only. */ }

    @CommandHandler
    AccountAggregate(CreateNewAccountCommand cmd) {
        log.debug("Handling: {}", cmd);
        var openingBalance = 0L; // Starts at 0.
        apply(new NewAccountCreatedEvent(cmd.accountId(), cmd.emailAddress(), openingBalance));
    }

    @EventSourcingHandler
    void on(NewAccountCreatedEvent evt) {
        log.debug("Applying: {}", evt);
        this.accountId = evt.accountId();
        this.balance = 0L;
    }

    // -------------------------------------------------------------------------------

    @CommandHandler
    void handle(CreditAccountCommand cmd) {
        log.debug("Handling: {}", cmd);
        apply(new AccountCreditedEvent(cmd.accountId(), cmd.paymentId(), cmd.amount()));
    }

    @EventSourcingHandler
    void on(AccountCreditedEvent evt) {
        log.debug("Applying: {}", evt);
        balance = balance + evt.amount();
    }

    @CommandHandler
    void handle(DebitAccountCommand cmd) {
        log.debug("Handling: {}", cmd);
        apply(new AccountDebitedEvent(cmd.accountId(), cmd.paymentId(), cmd.amount()));
    }

    @EventSourcingHandler
    void on(AccountDebitedEvent evt) {
        log.debug("Applying: {}", evt);
        balance = balance - evt.amount();
    }
}
