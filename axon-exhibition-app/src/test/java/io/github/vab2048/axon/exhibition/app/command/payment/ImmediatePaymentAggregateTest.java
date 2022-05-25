package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.MarkPaymentAsCompletedCommand;
import io.github.vab2048.axon.exhibition.message_api.common.InstantSupplier;
import io.github.vab2048.axon.exhibition.test.utils.command.payment.ImmediatePaymentAggregateMessageGenerator;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImmediatePaymentAggregateTest {
    private static final ImmediatePaymentAggregateMessageGenerator messageGenerator = new ImmediatePaymentAggregateMessageGenerator();

    private AggregateTestFixture<ImmediatePaymentAggregate> fixture;

    @BeforeEach
    void setUp() {
        // Reset the fixture before each test.
        fixture = new AggregateTestFixture<>(ImmediatePaymentAggregate.class);
    }

    private void setUpFixture(Instant paymentInitiationTimestamp) {
        // Set-up the timestamp which will be used by the injected InstantSupplier.
        var instantSupplier = mock(InstantSupplier.class);
        when(instantSupplier.get()).thenReturn(paymentInitiationTimestamp);
        fixture.registerInjectableResource(instantSupplier);
    }

    @Test
    void createImmediatePaymentScenario_WithValidFields_Succeeds() {
        var paymentInitiationTimestamp = Instant.now();
        setUpFixture(paymentInitiationTimestamp);

        var paymentCreation = messageGenerator.immediatePaymentCreation(paymentInitiationTimestamp);
        var creationCmd = paymentCreation.cmd();
        var createdEvt = paymentCreation.evt();
        var paymentSettlementTriggeredEvt= paymentCreation.evt2();

        fixture.givenNoPriorActivity()
                .when(creationCmd)

                .expectSuccessfulHandlerExecution()
                .expectEvents(createdEvt, paymentSettlementTriggeredEvt);
    }

    @Test
    void completedPaymentScenario_WithValidFields_Succeeds() {
        var completedPayment = messageGenerator.completedPayment(Instant.now());
        var creationEvt = completedPayment.immediatePaymentCreation().evt();
        var markPaymentAsCompletedCmd = completedPayment.cmd();
        var paymentCompletedEvt = completedPayment.evt();
        fixture.given(creationEvt)
                .when(markPaymentAsCompletedCmd)
                .expectEvents(paymentCompletedEvt);
    }

    @Test
    void failedPaymentScenario_WithValidFields_Succeeds() {
        var failedPayment = messageGenerator.failedPayment(Instant.now());
        var creationEvt = failedPayment.immediatePaymentCreation().evt();
        var markPaymentAsFailedCmd = failedPayment.cmd();
        var paymentFailedEvt = failedPayment.evt();
        fixture.given(creationEvt)
                .when(markPaymentAsFailedCmd)
                .expectEvents(paymentFailedEvt);
    }

    @Test
    void completedPaymentCmd_WhenPaymentIsAlreadyCompleted_HasNoEventsApplied() {
        var completedPayment = messageGenerator.completedPayment(Instant.now());
        var creationEvt = completedPayment.immediatePaymentCreation().evt();
        var completionCmd = completedPayment.cmd();
        var paymentCompletedEvt = completedPayment.evt();
        fixture.given(creationEvt, paymentCompletedEvt)
                // If the payment has already been marked as COMPLETED, when we issue the command to do it
                // again then it should just do nothing (no events) for idempotency.
                .when(completionCmd)
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void completedPaymentCmd_WhenPaymentHasAlreadyFailed_ThrowsException() {
        var failedPayment = messageGenerator.failedPayment(Instant.now());
        var creationEvt = failedPayment.immediatePaymentCreation().evt();
        var failedEvt = failedPayment.evt();
        var completionCmd = new MarkPaymentAsCompletedCommand(creationEvt.paymentId());
        fixture.given(creationEvt, failedEvt)
                .when(completionCmd)
                .expectException(IllegalStateException.class);
    }



}
