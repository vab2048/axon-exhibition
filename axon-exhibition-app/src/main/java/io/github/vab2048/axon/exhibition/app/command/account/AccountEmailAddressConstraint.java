package io.github.vab2048.axon.exhibition.app.command.account;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Entity for maintaining the illustrative invariant that the email address associated with an account
 * is unique across all accounts.
 */
@Table(AccountEmailAddressConstraint.TABLE_NAME)
public record AccountEmailAddressConstraint(
        @Id @Column("account_id") UUID accountId,
        @Column("email_address") String emailAddress) {
    public static final String TABLE_NAME = "command_side\".\"account_email_address_constraint";
}
