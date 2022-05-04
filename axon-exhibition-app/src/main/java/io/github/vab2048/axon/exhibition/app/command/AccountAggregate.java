package io.github.vab2048.axon.exhibition.app.command;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * A bank account aggregate.
 *
 * This is just a bare-bones implementation for the purpose of a demo.
 * It does not do anything special to deal with negative balances.
 */
@Aggregate
public class AccountAggregate {

    @AggregateIdentifier
    private UUID accountId;

    /**
     * Balance of the account in pennies.
     */
    private long balance;

    AccountAggregate() {}

    @CommandHandler
    AccountAggregate(CreateNewAccountCommand cmd) {
        apply(new NewAccountCreatedEvent(cmd.accountId()));
    }

    @EventSourcingHandler
    void on(NewAccountCreatedEvent evt) {
        this.accountId = evt.accountId();
        this.balance = 0L;
    }

    @CommandHandler
    void handle(CreditAccountCommand cmd) {
        apply(new AccountCreditedEvent(cmd.accountId(), cmd.paymentId(), cmd.amount()));
    }

    @EventSourcingHandler
    void on(AccountCreditedEvent evt) {
        balance = balance + evt.amount();
    }

    @CommandHandler
    void handle(DebitAccountCommand cmd) {
        apply(new AccountDebitedEvent(cmd.accountId(), cmd.paymentId(), cmd.amount()));
    }

    @EventSourcingHandler
    void on(AccountDebitedEvent evt) {
        balance = balance - evt.amount();
    }
}
