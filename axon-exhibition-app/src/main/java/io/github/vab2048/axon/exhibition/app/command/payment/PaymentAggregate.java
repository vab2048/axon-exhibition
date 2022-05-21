package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Representation of a bank transfer (payment) - a movement of money from one account to another.
 */
@Aggregate
public class PaymentAggregate {
    private static final Logger log = LoggerFactory.getLogger(PaymentAggregate.class);

    @AggregateIdentifier
    private UUID paymentId;
    private PaymentStatus status;


    PaymentAggregate() {}

    @CommandHandler
    public PaymentAggregate(CreatePaymentCommand command) {
        apply(new PaymentCreatedEvent(command.paymentId(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                PaymentStatus.CREATED));
    }

    @EventSourcingHandler
    public void on(PaymentCreatedEvent event) {
        this.paymentId = event.paymentId();
        this.status = event.status();
    }

    @CommandHandler
    public void handle(MarkPaymentAsCompletedCommand command) {
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
    public void handle(MarkPaymentAsFailedCommand command) {
        // If the payment status is already FAILED then we log a warning (because something could be wrong and worth inspecting)
        // and just return (for idempotency).
        if(status.equals(PaymentStatus.FAILED)) {
            log.warn("Received MarkPaymentAsCompletedCommand ({}) for payment which is currently already in the FAILED status. Will ignore the command.", command);
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
    public void on(PaymentCompletedEvent event) {
        this.status = PaymentStatus.COMPLETED;
    }

    @EventSourcingHandler
    public void on(PaymentFailedEvent event) {
        this.status = PaymentStatus.FAILED;
    }

}
