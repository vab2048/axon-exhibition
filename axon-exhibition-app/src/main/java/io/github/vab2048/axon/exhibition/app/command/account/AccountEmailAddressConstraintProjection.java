package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.NewAccountCreatedEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Component;

/**
 * Subscribing event processor maintaining the account email address constraint table in the DB.
 *
 * The DB itself holds a 'UNIQUE' constraint so that an email address associated with an account remains
 * unique across all accounts.
 *
 * This constraint is not realistic but simply illustrative of a command side projection/set based validation.
 */
@Component
@ProcessingGroup(AccountEmailAddressConstraintProjection.PROCESSING_GROUP_NAME)
public class AccountEmailAddressConstraintProjection {
    private static final Logger log = LoggerFactory.getLogger(AccountEmailAddressConstraintProjection.class);
    public static final String PROCESSING_GROUP_NAME = "account-email-address-constraint";

    /*
     * Maintain the uniqueness invariant that an email address must be unique to one account.
     */
    @EventHandler
    void on(NewAccountCreatedEvent evt, JdbcAggregateTemplate jdbcAggregateTemplate) {
        var accountId = evt.accountId();
        var emailAddress = evt.emailAddress();

        // If the uniqueness constraint is violated in the DB then it will result in a DbActionExecutionException.
        jdbcAggregateTemplate.insert(new AccountEmailAddressConstraint(accountId, emailAddress));
    }

    /**
     * Message which will used in the exception thrown because of a duplicate key at the point of account creation.
     */
    public static String accountCreationDuplicateKeyExceptionMessage(String emailAddress) {
        return "Account creation rejected as email address (%s) is already in use.".formatted(emailAddress);
    }

    /*
     * Handle the situation where a DbActionExecutionException is thrown when processing a NewAccountCreatedEvent.
     */
    @ExceptionHandler
    void handleException(NewAccountCreatedEvent evt, DbActionExecutionException ex) {
        // Where the specific cause is a DuplicateKeyException then we know that we have broken the
        // uniqueness constraint, and so we rethrow a more specific exception.
        // It is assumed it is the email address constraint just for simplicity (although in reality it could
        // actually be the primary key constraint).
        DuplicateKeyException duplicateKeyException = ExceptionUtils.throwableOfType(ex, DuplicateKeyException.class);
        if(duplicateKeyException != null) {
            throw new IllegalStateException(accountCreationDuplicateKeyExceptionMessage(evt.emailAddress()), duplicateKeyException);
        }

        // Otherwise: we just throw the general exception up the stack.
        throw ex;
    }

}
