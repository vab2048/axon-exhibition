package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Representation of a bank transfer (payment) - a movement of money from one account to another.
 */
@Aggregate
public class PaymentAggregate {

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
        apply(new PaymentCompletedEvent(command.paymentId()));
    }

    @CommandHandler
    public void handle(MarkPaymentAsFailedCommand command) {
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
