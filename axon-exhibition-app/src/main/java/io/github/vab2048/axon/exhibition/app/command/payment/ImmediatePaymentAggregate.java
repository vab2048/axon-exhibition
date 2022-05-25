package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import io.github.vab2048.axon.exhibition.message_api.common.InstantSupplier;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Supplier;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * A payment which should occur immediately.
 */
@Aggregate
public final class ImmediatePaymentAggregate extends PaymentAggregate {
    private static final Logger log = LoggerFactory.getLogger(ImmediatePaymentAggregate.class);

    @Deprecated
    ImmediatePaymentAggregate() { /* For framework use only. */ }

    @CommandHandler
    ImmediatePaymentAggregate(CreateImmediatePaymentCommand command, InstantSupplier instantSupplier) {
        log.debug("Handling: {}", command);
        // TODO: add checks for source and destination account IDs (that they are valid and source account
        //       has a large enough balance, etc)


        var settlementInitiationTimestamp = instantSupplier.get();
        apply(new ImmediatePaymentCreatedEvent(
                command.paymentId(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                PaymentStatus.CREATED,
                settlementInitiationTimestamp));

        apply(new PaymentSettlementTriggeredEvent(command.paymentId(), command.sourceAccountId(),
                command.destinationAccountId(), command.amount(), settlementInitiationTimestamp));
    }

    @EventSourcingHandler
    void on(ImmediatePaymentCreatedEvent event) {
        log.debug("Applying: {}", event);
        this.paymentId = event.paymentId();
        this.sourceAccountId = event.sourceAccountId();
        this.destinationAccountId = event.destinationAccountId();
        this.amount = event.amount();
        this.status = event.status();
    }

}
