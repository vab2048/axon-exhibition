package io.github.vab2048.axon.exhibition.message_api.command;

import org.axonframework.commandhandling.RoutingKey;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.UUID;

/**
 * The command API (commands/events) for the account aggregate.
 */
public class AccountCommandMessageAPI {
    private AccountCommandMessageAPI() { /* Non instantiable class */ }

    public record CreateMultipleAccountsInATransactionCommand(List<CreateNewAccountCommand> creationCommands) {

        /**
         * The routing key will be used by Axon Server (within a consistent hash algorithm) to determine
         * to which node of your application (if you have multiple instances of the app running) to send
         * the command. This is for load balancing.
         *
         * Normally for commands this would be done by marking the aggregateID with @TargetAggregateIdentifier
         * (which is meta annotated with @RoutingKey) - so all commands for the same aggregate get routed to the
         * same instance (reducing potential concurrency issues).
         *
         * But we don't have an aggregate for this command: so we just return a constant number which
         * will be used by the hash algorithm.
         *
         * @return Routing key which will be used to target the application instance this command will be routed to.
         */
        @RoutingKey
        public int routingKey() {
            return 1;
        }
    }
    public record CreateNewAccountCommand(@TargetAggregateIdentifier UUID accountId, String emailAddress) {}
    public record NewAccountCreatedEvent(UUID accountId, String emailAddress, long openingBalance) {}

    public record CreditAccountCommand(@TargetAggregateIdentifier UUID accountId, UUID paymentId, long amount) {}
    public record AccountCreditedEvent(UUID accountId, UUID paymentId, long amount) {}

    public record DebitAccountCommand(@TargetAggregateIdentifier UUID accountId, UUID paymentId, long amount) {}
    public record AccountDebitedEvent(UUID accountId, UUID paymentId, long amount) {}
}
