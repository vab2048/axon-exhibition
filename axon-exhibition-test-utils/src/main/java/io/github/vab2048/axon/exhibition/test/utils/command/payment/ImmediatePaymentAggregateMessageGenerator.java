package io.github.vab2048.axon.exhibition.test.utils.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenient utility class for tests to use to generate commands/events for an immediate payment aggregate.
 */
public class ImmediatePaymentAggregateMessageGenerator {
    public ImmediatePaymentAggregateMessageGenerator() {}

    /* ***********************************************************************
     *                            Scenario Records
     * ***********************************************************************
     * The scenario records hold all the relevant data (command(s)/event(s)) which
     * are expected for a particular scenario in the lifecycle of the aggregate.
     * Each scenario always begins from creation and contains the
     * data for each of the steps for that particular scenario.
     * ***********************************************************************/
    public record ImmediatePaymentCreation(
            CreateImmediatePaymentCommand cmd,
            ImmediatePaymentCreatedEvent evt,
            PaymentSettlementTriggeredEvent evt2) {}

    public record CompletedImmediatePayment(
            ImmediatePaymentCreation immediatePaymentCreation,
            MarkPaymentAsCompletedCommand cmd,
            PaymentCompletedEvent evt) {}

    public record FailedImmediatePayment(
            ImmediatePaymentCreation immediatePaymentCreation,
            MarkPaymentAsFailedCommand cmd,
            PaymentFailedEvent evt) {}


    /* ***********************************************************************
     *                            Scenario Methods
     * ***********************************************************************
     * The scenario methods contain the logic to create the simulated commands
     * and events for a particular scenario.
     * ***********************************************************************/
    public ImmediatePaymentCreation immediatePaymentCreation(Instant paymentInitiationTime) {
        var paymentId = UUID.randomUUID();
        var sourceAccountId = UUID.randomUUID();
        var destinationAccountId = UUID.randomUUID();
        var amount = ThreadLocalRandom.current().nextInt();
        var cmd = new CreateImmediatePaymentCommand(paymentId, sourceAccountId, destinationAccountId, amount);
        var evt = new ImmediatePaymentCreatedEvent(paymentId, sourceAccountId, destinationAccountId, amount, PaymentStatus.CREATED, paymentInitiationTime);
        var evt2 = new PaymentSettlementTriggeredEvent(paymentId, sourceAccountId, destinationAccountId, amount, paymentInitiationTime);
        return new ImmediatePaymentCreation(cmd, evt, evt2);
    }

    public CompletedImmediatePayment completedPayment(Instant paymentInitiationTime) {
        var paymentCreation = immediatePaymentCreation(paymentInitiationTime);
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsCompletedCmd = new MarkPaymentAsCompletedCommand(paymentId);
        var paymentCompletedEvt = new PaymentCompletedEvent(paymentId);
        return new CompletedImmediatePayment(paymentCreation, markPaymentAsCompletedCmd, paymentCompletedEvt);
    }

    public FailedImmediatePayment failedPayment(Instant paymentInitiationTime) {
        var paymentCreation = immediatePaymentCreation(paymentInitiationTime);
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsFailedCmd = new MarkPaymentAsFailedCommand(paymentId);
        var paymentFailedEvt = new PaymentFailedEvent(paymentId);
        return new FailedImmediatePayment(paymentCreation, markPaymentAsFailedCmd, paymentFailedEvt);
    }
}
