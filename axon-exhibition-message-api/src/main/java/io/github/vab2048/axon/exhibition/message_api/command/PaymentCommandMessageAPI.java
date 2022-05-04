package io.github.vab2048.axon.exhibition.message_api.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

/**
 * The command API (commands/events) for the payment aggregate.
 */
public class PaymentCommandMessageAPI {
    private PaymentCommandMessageAPI() { /* Non instantiable class */ }

    public record CreatePaymentCommand(@TargetAggregateIdentifier UUID paymentId,
                                       UUID sourceAccountId,
                                       UUID destinationAccountId,
                                       long amount) {}
    public record PaymentCreatedEvent(UUID paymentId,
                                      UUID sourceAccountId,
                                      UUID destinationAccountId,
                                      long amount,
                                      PaymentStatus status) {}
    public record MarkPaymentAsCompletedCommand(@TargetAggregateIdentifier UUID paymentId) {}
    public record PaymentCompletedEvent(UUID paymentId) {}
    public record MarkPaymentAsFailedCommand(@TargetAggregateIdentifier UUID paymentId) {}
    public record PaymentFailedEvent(UUID paymentId) {}
}
