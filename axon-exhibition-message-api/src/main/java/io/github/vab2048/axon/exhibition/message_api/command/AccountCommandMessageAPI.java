package io.github.vab2048.axon.exhibition.message_api.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

/**
 * The command API (commands/events) for the account aggregate.
 */
public class AccountCommandMessageAPI {
    private AccountCommandMessageAPI() { /* Non instantiable class */ }

    public record CreateNewAccountCommand(@TargetAggregateIdentifier UUID accountId, String emailAddress) {}
    public record NewAccountCreatedEvent(UUID accountId, String emailAddress) {}

    public record CreditAccountCommand(@TargetAggregateIdentifier UUID accountId, UUID paymentId, long amount) {}
    public record AccountCreditedEvent(UUID accountId, UUID paymentId, long amount) {}

    public record DebitAccountCommand(@TargetAggregateIdentifier UUID accountId, UUID paymentId, long amount) {}
    public record AccountDebitedEvent(UUID accountId, UUID paymentId, long amount) {}
}
