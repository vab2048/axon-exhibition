package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.MarkPaymentAsCompletedCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.ScheduledPaymentCreatedEvent;
import io.github.vab2048.axon.exhibition.message_api.common.InstantSupplier;
import io.github.vab2048.axon.exhibition.test.utils.command.payment.ImmediatePaymentAggregateMessageGenerator;
import io.github.vab2048.axon.exhibition.test.utils.command.payment.ScheduledPaymentAggregateMessageGenerator;
import org.axonframework.deadline.DeadlineMessage;
import org.axonframework.deadline.GenericDeadlineMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.Message;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduledPaymentAggregateTest {
    private static final ScheduledPaymentAggregateMessageGenerator messageGenerator = new ScheduledPaymentAggregateMessageGenerator();

    private AggregateTestFixture<ScheduledPaymentAggregate> fixture;

    @BeforeEach
    void setUp() {
        // Reset the fixture before each test.
        fixture = new AggregateTestFixture<>(ScheduledPaymentAggregate.class);
    }

    private void setUpFixture(Instant paymentInitiationTimestamp) {
        // Set-up the timestamp which will be used by the injected InstantSupplier.
        var instantSupplier = mock(InstantSupplier.class);
        when(instantSupplier.get()).thenReturn(paymentInitiationTimestamp);
        fixture.registerInjectableResource(instantSupplier);
    }

    @Test
    void createScheduledPayment_WithValidFields_Succeeds() {
        var currentInstant = Instant.now();
        setUpFixture(currentInstant);
        var paymentInitiationTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES);  // Setup to schedule more than 1  minute into the future.

        var paymentCreation = messageGenerator.scheduledPaymentCreation(paymentInitiationTimestamp);
        var creationCmd = paymentCreation.cmd();
        var expectedCreatedEvt = paymentCreation.evt();

        fixture.givenNoPriorActivity()
                .when(creationCmd)
                .expectSuccessfulHandlerExecution()
                .expectScheduledDeadlineWithName(paymentInitiationTimestamp, ScheduledPaymentAggregate.TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME)
                .expectEventsMatching(Matchers.matches((List<EventMessage<?>> eventMessages) -> {
                    // Unfortunately we cannot set the deadlineID in advance, and so we cannot just easily compare
                    // event payloads like we would normally. And so in this case we need to compare each
                    // individual field of the event payload.
                    List<?> eventPayloads = eventMessages.stream().map(Message::getPayload).toList();

                    // Assertion 1: There should only be one event in the list.
                    assertThat(eventPayloads.size()).isEqualTo(1);

                    // Assertion 2: That event should be a ScheduledPaymentCreatedEvent.
                    assertThat(eventPayloads.get(0)).isOfAnyClassIn(ScheduledPaymentCreatedEvent.class);

                    // Assertion 3: All the event's fields (except the deadlineId) should match
                    //              perfectly with what is expected.
                    var actualCreatedEvent = (ScheduledPaymentCreatedEvent) eventPayloads.get(0);
                    assertThat(actualCreatedEvent.paymentId()).isEqualTo(expectedCreatedEvt.paymentId());
                    assertThat(actualCreatedEvent.sourceAccountId()).isEqualTo(expectedCreatedEvt.sourceAccountId());
                    assertThat(actualCreatedEvent.destinationAccountId()).isEqualTo(expectedCreatedEvt.destinationAccountId());
                    assertThat(actualCreatedEvent.amount()).isEqualTo(expectedCreatedEvt.amount());
                    assertThat(actualCreatedEvent.settlementInitiationTime()).isEqualTo(expectedCreatedEvt.settlementInitiationTime());

                    // Assertion 4: The deadline ID should be set to a value (we don't care what it is).
                    assertThat(actualCreatedEvent.deadlineId()).isNotNull();

                    // If we reach here then everything matched OK.
                    return true;
                }));
    }

    @Test
    void createScheduledPayment_WithTriggerLessThanAMinuteAway_Fails() {
        var paymentInitiationTimestamp = Instant.now();
        setUpFixture(paymentInitiationTimestamp);

        var paymentCreation = messageGenerator.scheduledPaymentCreation(paymentInitiationTimestamp);
        var creationCmd = paymentCreation.cmd();

        fixture.givenNoPriorActivity()
                .when(creationCmd)
                .expectException(IllegalStateException.class);
    }

    @Test @SuppressWarnings("unchecked")
    void createdScheduledPayment_AfterThresholdBreached_TriggersPaymentDeadline() {
        var currentTime = Instant.now();
        setUpFixture(currentTime);
        var paymentInitiationTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES);

        var paymentTriggered = messageGenerator.scheduledPaymentTriggered(paymentInitiationTimestamp);
        var creationCmd = paymentTriggered.creation().cmd();
        var deadlinePayload = paymentTriggered.deadlinePayload();

        fixture.givenNoPriorActivity()
                .andGivenCurrentTime(currentTime)
                .andGivenCommands(creationCmd)
                .whenThenTimeAdvancesTo(paymentInitiationTimestamp)
                .expectDeadlinesMetMatching(Matchers.matches((List<? extends DeadlineMessage<?>> deadlines) -> {
                    // Assertion 1: There should be one deadline.
                    assertThat(deadlines.size()).isEqualTo(1);
                    DeadlineMessage<?> deadlineMessage = deadlines.get(0);

                    // Assert 2: The deadline is of the name we expect.
                    assertThat(deadlineMessage.getDeadlineName()).isEqualTo(ScheduledPaymentAggregate.TRIGGER_SCHEDULED_PAYMENT_DEADLINE_NAME);

                    // Assert 3: The deadline payload should match our expectation.
                    assertThat(deadlineMessage.getPayload()).isEqualTo(deadlinePayload);

                    // If we reach here then everything matched OK.
                    return true;
                }));
    }

    @Test
    void cancelScheduledPayment_Succeeds() {
        var currentTime = Instant.now();
        setUpFixture(currentTime);
        var paymentInitiationTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES);

        var cancelledScheduledPayment = messageGenerator.cancelScheduledPaymentCommand(paymentInitiationTimestamp);
        var creationCmd = cancelledScheduledPayment.creation().cmd();
        var cancelCmd = cancelledScheduledPayment.cmd();
        var cancelledEvt = cancelledScheduledPayment.evt();

        fixture.givenNoPriorActivity()
                .andGivenCurrentTime(currentTime)
                .andGivenCommands(creationCmd)
                .when(cancelCmd)
                .expectNoScheduledDeadlines();
    }


    @Test
    void completedScheduledPayment_WithValidFields_Succeeds() {
        var completedPayment = messageGenerator.scheduledPaymentCompleted(Instant.now());
        var creationEvt = completedPayment.triggered().creation().evt();
        var triggeredEvt = completedPayment.triggered().evt();
        var markPaymentAsCompletedCmd = completedPayment.cmd();
        var paymentCompletedEvt = completedPayment.evt();
        fixture.given(creationEvt, triggeredEvt)
                .when(markPaymentAsCompletedCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(paymentCompletedEvt);
    }

    @Test
    void failedScheduledPayment_WithValidFields_Succeeds() {
        var failedPayment = messageGenerator.failedPayment(Instant.now());
        var creationEvt = failedPayment.triggered().creation().evt();
        var triggeredEvt = failedPayment.triggered().evt();
        var markPaymentAsFailedCmd = failedPayment.cmd();
        var paymentFailedEvt = failedPayment.evt();
        fixture.given(creationEvt, triggeredEvt)
                .when(markPaymentAsFailedCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(paymentFailedEvt);
    }

    @Test
    void completedPaymentCmd_WhenPaymentIsAlreadyCompleted_HasNoEventsApplied() {
        var completedPayment = messageGenerator.scheduledPaymentCompleted(Instant.now());
        var creationEvt = completedPayment.triggered().creation().evt();
        var triggeredEvt = completedPayment.triggered().evt();
        var completionCmd = completedPayment.cmd();
        var paymentCompletedEvt = completedPayment.evt();
        fixture.given(creationEvt, triggeredEvt, paymentCompletedEvt)
                // If the payment has already been marked as COMPLETED, when we issue the command to do it
                // again then it should just do nothing (no events) for idempotency.
                .when(completionCmd)
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void completedPaymentCmd_WhenPaymentHasAlreadyFailed_ThrowsException() {
        var failedPayment = messageGenerator.failedPayment(Instant.now());
        var creationEvt = failedPayment.triggered().creation().evt();
        var triggeredEvt = failedPayment.triggered().evt();
        var failedEvt = failedPayment.evt();
        var completionCmd = new MarkPaymentAsCompletedCommand(creationEvt.paymentId());
        fixture.given(creationEvt, triggeredEvt, failedEvt)
                .when(completionCmd)
                .expectException(IllegalStateException.class);
    }



}
