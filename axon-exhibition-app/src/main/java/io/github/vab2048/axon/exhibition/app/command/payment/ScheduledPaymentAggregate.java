package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import io.github.vab2048.axon.exhibition.message_api.common.InstantSupplier;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public final class ScheduledPaymentAggregate extends PaymentAggregate {
    private static final Logger log = LoggerFactory.getLogger(ScheduledPaymentAggregate.class);
    public static final String EXAMPLE_DEADLINE_PAYLOAD_MESSAGE = "Hello from the other side!";
    public static final String TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME = "TRIGGER_SCHEDULED_PAYMENT_DEADLINE";

    private Instant scheduledPaymentInstant;
    private String scheduledPaymentDeadlineId;

    @Deprecated
    ScheduledPaymentAggregate() { /* For framework use only. */ }

    @CommandHandler
    ScheduledPaymentAggregate(
            CreateScheduledPaymentCommand command,
            InstantSupplier instantSupplier,
            DeadlineManager deadlineManager) {
        log.debug("Handling: {}", command);
        // TODO: add checks for source and destination account IDs (that they are valid and source account
        //       has a large enough balance, etc)

        // Can only schedule payments if they are at least a minute in the future....
        var now = instantSupplier.get();
        var scheduleInstant = command.settlementInitiationTime();
        if(scheduleInstant.toEpochMilli() - now.toEpochMilli() < 60_000) {
            throw new IllegalStateException("Can only schedule payments which are at least 1 minute in the future");
        }

        var deadlinePayload = new TriggerScheduledPaymentDeadlinePayload(EXAMPLE_DEADLINE_PAYLOAD_MESSAGE, scheduleInstant);
        var deadlineId = deadlineManager.schedule(scheduleInstant, TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME, deadlinePayload);
        var paymentStatus = PaymentStatus.CREATED;
        apply(new ScheduledPaymentCreatedEvent(command.paymentId(), command.sourceAccountId(),
                command.destinationAccountId(), command.amount(), paymentStatus, scheduleInstant, deadlineId));
    }

    @EventSourcingHandler
    void on(ScheduledPaymentCreatedEvent event) {
        log.debug("Applying: {}", event);
        this.paymentId = event.paymentId();
        this.sourceAccountId = event.sourceAccountId();
        this.destinationAccountId = event.destinationAccountId();
        this.amount = event.amount();
        this.status = event.status();
        this.scheduledPaymentInstant = event.settlementInitiationTime();
        this.scheduledPaymentDeadlineId = event.deadlineId();
    }

    @DeadlineHandler(deadlineName = TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME)
    void onScheduledPaymentDeadline(TriggerScheduledPaymentDeadlinePayload paymentDeadlinePayload) {
        log.debug("Handling: {}", paymentDeadlinePayload);
        apply(new PaymentSettlementTriggeredEvent(paymentId, sourceAccountId, destinationAccountId, amount,
                paymentDeadlinePayload.settlementInitiationTime()));
    }

    @CommandHandler
    void handle(CancelScheduledPaymentCommand command, DeadlineManager deadlineManager) {
        log.debug("Handling: {}", command);
        deadlineManager.cancelSchedule(TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME, scheduledPaymentDeadlineId);
        apply(new ScheduledPaymentCancelledEvent(paymentId));
    }

}
