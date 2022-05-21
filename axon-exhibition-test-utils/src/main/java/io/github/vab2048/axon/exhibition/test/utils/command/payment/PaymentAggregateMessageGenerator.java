package io.github.vab2048.axon.exhibition.test.utils.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.*;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenient utility class for tests to use to generate commands/events for the payment aggregate.
 */
public class PaymentAggregateMessageGenerator {
    public PaymentAggregateMessageGenerator() {}

    /* ***********************************************************************
     *                            Scenario Records
     * ***********************************************************************
     * The scenario records hold all the relevant data (command(s)/event(s)) which
     * are expected for a particular scenario in the lifecycle of the aggregate.
     * Each scenario always begins from creation and contains the
     * data for each of the steps for that particular scenario.
     * ***********************************************************************/
    public record PaymentCreation(
            CreatePaymentCommand cmd,
            PaymentCreatedEvent evt) {}

    public record CompletedPayment(
            PaymentCreation paymentCreation,
            MarkPaymentAsCompletedCommand cmd,
            PaymentCompletedEvent evt) {}

    public record FailedPayment(
            PaymentCreation paymentCreation,
            MarkPaymentAsFailedCommand cmd,
            PaymentFailedEvent evt) {}


    /* ***********************************************************************
     *                            Scenario Methods
     * ***********************************************************************
     * The scenario methods contain the logic to create the simulated commands
     * and events for a particular scenario.
     * ***********************************************************************/
    public PaymentCreation paymentCreation() {
        var paymentId = UUID.randomUUID();
        var sourceAccountId = UUID.randomUUID();
        var destinationAccountId = UUID.randomUUID();
        var amount = ThreadLocalRandom.current().nextInt();
        var cmd = new CreatePaymentCommand(paymentId, sourceAccountId, destinationAccountId, amount);
        var evt = new PaymentCreatedEvent(paymentId, sourceAccountId, destinationAccountId, amount, PaymentStatus.CREATED);
        return new PaymentCreation(cmd, evt);
    }

    public CompletedPayment completedPayment() {
        var paymentCreation = paymentCreation();
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsCompletedCmd = new MarkPaymentAsCompletedCommand(paymentId);
        var paymentCompletedEvt = new PaymentCompletedEvent(paymentId);
        return new CompletedPayment(paymentCreation, markPaymentAsCompletedCmd, paymentCompletedEvt);
    }

    public FailedPayment failedPayment() {
        var paymentCreation = paymentCreation();
        var paymentId = paymentCreation.evt().paymentId();
        var markPaymentAsFailedCmd = new MarkPaymentAsFailedCommand(paymentId);
        var paymentFailedEvt = new PaymentFailedEvent(paymentId);
        return new FailedPayment(paymentCreation, markPaymentAsFailedCmd, paymentFailedEvt);
    }
}
