package io.github.vab2048.axon.exhibition.test.utils.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenient utility class for tests to use to generate commands/events for a scheduled payment aggregate.
 */
public class ScheduledPaymentAggregateMessageGenerator {
    public ScheduledPaymentAggregateMessageGenerator() {}

    /* ***********************************************************************
     *                            Scenario Records
     * ***********************************************************************
     * The scenario records hold all the relevant data (command(s)/event(s)) which
     * are expected for a particular scenario in the lifecycle of the aggregate.
     * Each scenario always begins from creation and contains the
     * data for each of the steps for that particular scenario.
     * ***********************************************************************/
    public record ScheduledPaymentCreation(CreateScheduledPaymentCommand cmd, ScheduledPaymentCreatedEvent evt) {}
    public record CancelledScheduledPayment(
            ScheduledPaymentCreation creation,
            CancelScheduledPaymentCommand cmd,
            ScheduledPaymentCancelledEvent evt) {}
    public record ScheduledPaymentTriggered(
            ScheduledPaymentCreation creation,
            PaymentSettlementTriggeredEvent evt,
            TriggerScheduledPaymentDeadlinePayload deadlinePayload) {}


    public record CompletedScheduledPayment(
            ScheduledPaymentTriggered triggered,
            MarkPaymentAsCompletedCommand cmd,
            PaymentCompletedEvent evt) {}
    public record FailedScheduledPayment(
            ScheduledPaymentTriggered triggered,
            MarkPaymentAsFailedCommand cmd,
            PaymentFailedEvent evt) {}


    /* ***********************************************************************
     *                            Scenario Methods
     * ***********************************************************************
     * The scenario methods contain the logic to create the simulated commands
     * and events for a particular scenario.
     * ***********************************************************************/
    public ScheduledPaymentCreation scheduledPaymentCreation(Instant paymentInitiationTime) {
        var paymentId = UUID.randomUUID();
        var sourceAccountId = UUID.randomUUID();
        var destinationAccountId = UUID.randomUUID();
        var amount = ThreadLocalRandom.current().nextInt();
        var cmd = new CreateScheduledPaymentCommand(paymentId, sourceAccountId, destinationAccountId, amount, paymentInitiationTime);
        // Deadline ID is set to null because we do not know it in advance.
        var evt = new ScheduledPaymentCreatedEvent(paymentId, sourceAccountId, destinationAccountId, amount, PaymentStatus.CREATED, paymentInitiationTime, null);

        return new ScheduledPaymentCreation(cmd, evt);
    }

    public CancelledScheduledPayment cancelScheduledPaymentCommand(Instant paymentInitiationTime) {
        var scheduledPaymentCreation = scheduledPaymentCreation(paymentInitiationTime);
        var paymentId = scheduledPaymentCreation.evt().paymentId();
        var cancelScheduledPaymentCmd = new CancelScheduledPaymentCommand(paymentId);
        var scheduledPaymentCancelledEvt = new ScheduledPaymentCancelledEvent(paymentId);
        return new CancelledScheduledPayment(scheduledPaymentCreation, cancelScheduledPaymentCmd, scheduledPaymentCancelledEvt);
    }

    public ScheduledPaymentTriggered scheduledPaymentTriggered(Instant paymentInitiationTime) {
        var scheduledPaymentCreation = scheduledPaymentCreation(paymentInitiationTime);
        var paymentId = scheduledPaymentCreation.evt().paymentId();
        var sourceAccountId = scheduledPaymentCreation.evt().sourceAccountId();
        var destinationAccountId = scheduledPaymentCreation.evt().destinationAccountId();
        var amount = scheduledPaymentCreation.evt().amount();
        var evt = new PaymentSettlementTriggeredEvent(paymentId, sourceAccountId, destinationAccountId, amount, paymentInitiationTime);
        var payload = new TriggerScheduledPaymentDeadlinePayload("Hello from the other side!", paymentInitiationTime);

        return new ScheduledPaymentTriggered(scheduledPaymentCreation, evt, payload);
    }

    public CompletedScheduledPayment scheduledPaymentCompleted(Instant paymentInitiationTime) {
        var paymentCreation = scheduledPaymentTriggered(paymentInitiationTime);
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsCompletedCmd = new MarkPaymentAsCompletedCommand(paymentId);
        var paymentCompletedEvt = new PaymentCompletedEvent(paymentId);
        return new CompletedScheduledPayment(paymentCreation, markPaymentAsCompletedCmd, paymentCompletedEvt);
    }

    public FailedScheduledPayment failedPayment(Instant paymentInitiationTime) {
        var paymentCreation = scheduledPaymentTriggered(paymentInitiationTime);
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsFailedCmd = new MarkPaymentAsFailedCommand(paymentId);
        var paymentFailedEvt = new PaymentFailedEvent(paymentId);
        return new FailedScheduledPayment(paymentCreation, markPaymentAsFailedCmd, paymentFailedEvt);
    }

}
