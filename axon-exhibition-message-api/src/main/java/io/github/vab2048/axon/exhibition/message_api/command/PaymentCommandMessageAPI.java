package io.github.vab2048.axon.exhibition.message_api.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;
import java.util.UUID;

/**
 * The command API (commands/events) for the payment aggregate.
 */
public class PaymentCommandMessageAPI {
    private PaymentCommandMessageAPI() { /* Non instantiable class */ }

    /**
     * Common interface for all events representing the creation of a payment.
     */
    public sealed interface PaymentCreatedEvent
            permits ImmediatePaymentCreatedEvent, ScheduledPaymentCreatedEvent {
        UUID paymentId();
        UUID sourceAccountId();
        UUID destinationAccountId();
        long amount();
        PaymentStatus status();
        Instant settlementInitiationTime();
    }

    public record CreateScheduledPaymentCommand(
            @TargetAggregateIdentifier UUID paymentId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount,
            Instant settlementInitiationTime) {}

    public record ScheduledPaymentCreatedEvent(
            UUID paymentId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount,
            PaymentStatus status,
            Instant settlementInitiationTime,
            String deadlineId) implements PaymentCreatedEvent {}

    public record TriggerScheduledPaymentDeadlinePayload(String message, Instant settlementInitiationTime) {}

    public record CancelScheduledPaymentCommand(@TargetAggregateIdentifier UUID paymentId) {}
    public record ScheduledPaymentCancelledEvent(UUID paymentId) {}

    public record CreateImmediatePaymentCommand(
            @TargetAggregateIdentifier UUID paymentId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount) {}

    public record ImmediatePaymentCreatedEvent(
            UUID paymentId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount,
            PaymentStatus status,
            Instant settlementInitiationTime) implements PaymentCreatedEvent {}

    public record TriggerPaymentSettlementCommand(@TargetAggregateIdentifier UUID paymentId) {}

    public record PaymentSettlementTriggeredEvent(
            UUID paymentId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            long amount,
            Instant settlementInitiationTimestamp) {}

    public record MarkPaymentAsCompletedCommand(@TargetAggregateIdentifier UUID paymentId) {}

    public record PaymentCompletedEvent(UUID paymentId) {}

    public record MarkPaymentAsFailedCommand(@TargetAggregateIdentifier UUID paymentId) {}

    public record PaymentFailedEvent(UUID paymentId) {}
}
