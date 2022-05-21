package io.github.vab2048.axon.exhibition.test.utils.command.account;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.UUID;

/**
 * Convenient utility class for tests to use to generate commands/events for the account aggregate.
 */
public class AccountAggregateMessageGenerator {
    public AccountAggregateMessageGenerator() {}

    /* ***********************************************************************
     *                            Scenario Records
     * ***********************************************************************
     * The scenario records hold all the relevant data (command(s)/event(s)) which
     * are expected for a particular scenario in the lifecycle of the aggregate.
     * Each scenario always begins from creation and contains the
     * data for each of the steps for that particular scenario.
     * ***********************************************************************/
    public record AccountCreation(
            CreateNewAccountCommand cmd,
            NewAccountCreatedEvent evt) {}
    public record AccountCredited(
            AccountCreation accountCreation,
            CreditAccountCommand cmd,
            AccountCreditedEvent evt) {}
    public record AccountDebited(
            AccountCreation accountCreation,
            DebitAccountCommand cmd,
            AccountDebitedEvent evt) {}

    /* ***********************************************************************
     *                            Scenario Methods
     * ***********************************************************************
     * The scenario methods contain the logic to create the simulated commands
     * and events for a particular scenario.
     * ***********************************************************************/
    public AccountCreation accountCreation() {
        var accountId = UUID.randomUUID();
        var emailAddress = RandomStringUtils.randomAlphabetic(8) + "@" + RandomStringUtils.randomAlphabetic(5) + ".com";
        var cmd = new CreateNewAccountCommand(accountId, emailAddress);
        var evt = new NewAccountCreatedEvent(accountId, emailAddress);
        return new AccountCreation(cmd, evt);
    }

    public AccountCredited accountCredited() {
        var creation = accountCreation();
        var accountId = creation.evt().accountId();
        var paymentId = UUID.randomUUID();
        var creditAmount = RandomUtils.nextLong();
        var cmd = new CreditAccountCommand(accountId, paymentId, creditAmount);
        var evt = new AccountCreditedEvent(accountId, paymentId, creditAmount);
        return new AccountCredited(creation, cmd, evt);
    }

    public AccountDebited accountDebited() {
        var creation = accountCreation();
        var accountId = creation.evt().accountId();
        var paymentId = UUID.randomUUID();
        var debitAmount = RandomUtils.nextLong();
        var cmd = new DebitAccountCommand(accountId, paymentId, debitAmount);
        var evt = new AccountDebitedEvent(accountId, paymentId, debitAmount);
        return new AccountDebited(creation, cmd, evt);
    }

}
