package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountDebitedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreditAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.DebitAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.MarkPaymentAsCompletedCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentSettlementTriggeredEvent;
import io.github.vab2048.axon.exhibition.test.utils.command.account.AccountAggregateMessageGenerator;
import io.github.vab2048.axon.exhibition.test.utils.command.payment.ImmediatePaymentAggregateMessageGenerator;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.github.vab2048.axon.exhibition.app.command.payment.PaymentSettlementSaga.PAYMENT_ID_ASSOCIATION_PROPERTY;

public class PaymentSettlementSagaTest {
    private static final ImmediatePaymentAggregateMessageGenerator immediatePaymentAggregateMessageGenerator = new ImmediatePaymentAggregateMessageGenerator();
    private static final AccountAggregateMessageGenerator accountAggregateMessageGenerator = new AccountAggregateMessageGenerator();

    private SagaTestFixture<PaymentSettlementSaga> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(PaymentSettlementSaga.class);
    }


    @Test
    void paymentSettlementTriggeredEvent_ResultsInSagaAndDebitAccountCommand() {
        var paymentInitiationTimestamp = Instant.now();
        var paymentCreationMessages = immediatePaymentAggregateMessageGenerator.immediatePaymentCreation(paymentInitiationTimestamp);
        var paymentSettlementTriggeredEvent = paymentCreationMessages.evt2();
        var paymentId = paymentSettlementTriggeredEvent.paymentId();
        var sourceAccountId = paymentSettlementTriggeredEvent.sourceAccountId();
        var amount = paymentSettlementTriggeredEvent.amount();
        var expectedCommand = new DebitAccountCommand(sourceAccountId, paymentId, amount);

        fixture.givenNoPriorActivity()
                .whenAggregate(paymentId.toString())
                .publishes(paymentSettlementTriggeredEvent)
                .expectActiveSagas(1)
                .expectDispatchedCommands(expectedCommand)
                .expectAssociationWith(PAYMENT_ID_ASSOCIATION_PROPERTY, paymentId);
    }


    @Test
    void accountDebitedEvent_ResultsInSagaDispatchingCreditAccountCommand() {
        var paymentInitiationTimestamp = Instant.now();
        var paymentCreationMessages = immediatePaymentAggregateMessageGenerator.immediatePaymentCreation(paymentInitiationTimestamp);
        var paymentSettlementTriggeredEvent = paymentCreationMessages.evt2();
        var paymentId = paymentSettlementTriggeredEvent.paymentId();
        var paymentAmount = paymentSettlementTriggeredEvent.amount();
        var debitAccountId = paymentSettlementTriggeredEvent.sourceAccountId();
        var creditAccountId = paymentSettlementTriggeredEvent.destinationAccountId();
        var accountDebitedEvent = new AccountDebitedEvent(debitAccountId, paymentId, paymentAmount);
        var expectedCommand = new CreditAccountCommand(creditAccountId, paymentId, paymentAmount);

        fixture.givenAggregate(paymentId.toString())
                .published(paymentSettlementTriggeredEvent)
                .whenAggregate(debitAccountId.toString())
                .publishes(accountDebitedEvent)
                .expectActiveSagas(1)
                .expectDispatchedCommands(expectedCommand);
    }

    @Test
    void accountCreditedEvent_ResultsInMarkPaymentAsCompleteCommandAndEndsSaga() {
        var paymentInitiationTimestamp = Instant.now();
        var paymentCreationMessages = immediatePaymentAggregateMessageGenerator.immediatePaymentCreation(paymentInitiationTimestamp);
        var paymentSettlementTriggeredEvent = paymentCreationMessages.evt2();
        var paymentId = paymentSettlementTriggeredEvent.paymentId();
        var paymentAmount = paymentSettlementTriggeredEvent.amount();
        var debitAccountId = paymentSettlementTriggeredEvent.sourceAccountId();
        var creditAccountId = paymentSettlementTriggeredEvent.destinationAccountId();
        var accountCreditedEvent = new AccountCommandMessageAPI.AccountCreditedEvent(creditAccountId, paymentId, paymentAmount);
        var expectedCommand = new MarkPaymentAsCompletedCommand(paymentId);

        fixture.givenAggregate(paymentId.toString())
                .published(paymentSettlementTriggeredEvent)
                .whenAggregate(debitAccountId.toString())
                .publishes(accountCreditedEvent)
                .expectActiveSagas(0)
                .expectDispatchedCommands(expectedCommand);
    }



}
