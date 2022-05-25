package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Representation of a bank transfer (payment) - a movement of money from one account to another.
 */
@Aggregate
public abstract sealed class PaymentAggregate permits ImmediatePaymentAggregate, ScheduledPaymentAggregate {
    private static final Clock clock = GenericEventMessage.clock;
    private static final Logger log = LoggerFactory.getLogger(PaymentAggregate.class);

    @AggregateIdentifier protected UUID paymentId;
    protected UUID sourceAccountId;
    protected UUID destinationAccountId;
    protected Long amount;
    protected PaymentStatus status;

    PaymentAggregate() { /* For framework use only. */ }

    @CommandHandler
    void handle(TriggerPaymentSettlementCommand command) {
        log.debug("Handling: {}", command);
        // If the status is already either COMPLETED or FAILED then we cannot do anything...
        if(status.equals(PaymentStatus.COMPLETED) || status.equals(PaymentStatus.FAILED)) {
            var msg = "Unable to transition payment to IN_PROGRESS state because the payment has already %s".formatted(status);
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        // If it is already IN_PROGRESS then we can just log a warning and return (for idempotency)...
        if(status.equals(PaymentStatus.IN_PROGRESS)) {
            log.warn("Received TriggerPaymentSettlementCommand ({}) for payment which is currently already in the " +
                    "IN_PROGRESS state. Will ignore the command.", command);
            return;
        }

        // Otherwise... we can go ahead and kick of the settlement of the payment.
        var settlementInitiationTimestamp = clock.instant();
        apply(new PaymentSettlementTriggeredEvent(paymentId, sourceAccountId, destinationAccountId, amount, settlementInitiationTimestamp));
    }

    @EventSourcingHandler
    void on(PaymentSettlementTriggeredEvent event) {
        log.debug("Applying: {}", event);
        this.status = PaymentStatus.IN_PROGRESS;
    }

    @CommandHandler
    void handle(MarkPaymentAsCompletedCommand command) {
        log.debug("Handling: {}", command);
        // If the payment status is already COMPLETED then we log a warning (because something could be wrong and worth inspecting)
        // and just return (for idempotency).
        if(status.equals(PaymentStatus.COMPLETED)) {
            log.warn("Received MarkPaymentAsCompletedCommand ({}) for payment which is currently already in the COMPLETED status. Will ignore the command.", command);
            return;
        }
        // If the status is FAILED, then we need to reject this command.
        if(status.equals(PaymentStatus.FAILED)) {
            throw new IllegalStateException("Unable to transition to COMPLETED state because the payment has already FAILED.");
        }
        // Otherwise, we can go ahead and apply the event...
        apply(new PaymentCompletedEvent(command.paymentId()));
    }

    @CommandHandler
    void handle(MarkPaymentAsFailedCommand command) {
        log.debug("Handling: {}", command);
        // If the payment status is already FAILED then we log a warning (because something could be wrong and worth inspecting)
        // and just return (for idempotency).
        if(status.equals(PaymentStatus.FAILED)) {
            log.warn("Received MarkPaymentAsFailedCommand ({}) for payment which is currently already in the FAILED status. Will ignore the command.", command);
            return;
        }
        // If the status is COMPLETED, then we need to reject this command.
        if(status.equals(PaymentStatus.COMPLETED)) {
            throw new IllegalStateException("Unable to transition to FAILED state because the payment has already COMPLETED.");
        }
        // Otherwise, we can go ahead and apply the event...
        apply(new PaymentFailedEvent(command.paymentId()));
    }

    @EventSourcingHandler
    void on(PaymentCompletedEvent event) {
        log.debug("Applying: {}", event);
        this.status = PaymentStatus.COMPLETED;
    }

    @EventSourcingHandler
    void on(PaymentFailedEvent event) {
        log.debug("Applying: {}", event);
        this.status = PaymentStatus.FAILED;
    }

}
