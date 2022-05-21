package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.test.utils.command.payment.PaymentAggregateMessageGenerator;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentAggregateTest {
    private static final PaymentAggregateMessageGenerator messageGenerator = new PaymentAggregateMessageGenerator();

    private AggregateTestFixture<PaymentAggregate> fixture;

    @BeforeEach
    void setUp() {
        // Reset the fixture before each test.
        fixture = new AggregateTestFixture<>(PaymentAggregate.class);
    }

    @Test
    void createPaymentScenario_WithValidFields_Succeeds() {
        var paymentCreation = messageGenerator.paymentCreation();
        var creationCmd = paymentCreation.cmd();
        var createdEvt = paymentCreation.evt();
        fixture.givenNoPriorActivity()
                .when(creationCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(createdEvt);
    }

    @Test
    void completedPaymentScenario_WithValidFields_Succeeds() {
        var completedPayment = messageGenerator.completedPayment();
        var creationEvt = completedPayment.paymentCreation().evt();
        var markPaymentAsCompletedCmd = completedPayment.cmd();
        var paymentCompletedEvt = completedPayment.evt();
        fixture.given(creationEvt)
                .when(markPaymentAsCompletedCmd)
                .expectEvents(paymentCompletedEvt);
    }

    @Test
    void failedPaymentScenario_WithValidFields_Succeeds() {
        var failedPayment = messageGenerator.failedPayment();
        var creationEvt = failedPayment.paymentCreation().evt();
        var markPaymentAsFailedCmd = failedPayment.cmd();
        var paymentFailedEvt = failedPayment.evt();
        fixture.given(creationEvt)
                .when(markPaymentAsFailedCmd)
                .expectEvents(paymentFailedEvt);
    }

    @Test
    void completedPaymentCmd_WhenPaymentIsAlreadyCompleted_TBC() {
        throw new IllegalStateException("not yet implemented");
    }

    @Test
    void completedPaymentCmd_WhenPaymentHasAlreadyFailed_TBC() {
        throw new IllegalStateException("not yet implemented");
    }



}
