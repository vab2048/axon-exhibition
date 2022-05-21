package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.test.utils.command.account.AccountAggregateMessageGenerator;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountAggregateTest {
    private static final AccountAggregateMessageGenerator messageGenerator = new AccountAggregateMessageGenerator();

    private AggregateTestFixture<AccountAggregate> fixture;

    @BeforeEach
    void setUp() {
        // Reset the fixture before each test.
        fixture = new AggregateTestFixture<>(AccountAggregate.class);
    }

    @Test
    void createAccountScenario_WithValidFields_Succeeds() {
        var accountCreation = messageGenerator.accountCreation();
        var creationCmd = accountCreation.cmd();
        var createdEvt = accountCreation.evt();
        fixture.givenNoPriorActivity()
                .when(creationCmd)
                .expectSuccessfulHandlerExecution()
                .expectEvents(createdEvt);
    }

    @Test
    void creditAccountScenario_WithValidFields_Succeeds() {
        var accountCredited = messageGenerator.accountCredited();
        var creationEvt = accountCredited.accountCreation().evt();
        var creditAccountCmd = accountCredited.cmd();
        var accountCreditedEvt = accountCredited.evt();
        fixture.given(creationEvt)
                .when(creditAccountCmd)
                .expectEvents(accountCreditedEvt);
    }

    @Test
    void debitAccountScenario_WithValidFields_Succeeds() {
        var accountDebited = messageGenerator.accountDebited();
        var creationEvt = accountDebited.accountCreation().evt();
        var debitAccountCmd = accountDebited.cmd();
        var accountDebitedEvt = accountDebited.evt();
        fixture.given(creationEvt)
                .when(debitAccountCmd)
                .expectEvents(accountDebitedEvt);
    }
}
