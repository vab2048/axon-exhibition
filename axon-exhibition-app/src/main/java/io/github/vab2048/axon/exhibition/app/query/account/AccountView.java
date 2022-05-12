package io.github.vab2048.axon.exhibition.app.query.account;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;
@Table(AccountView.TABLE_NAME)
public record AccountView(
        @Id @Column("account_id") UUID accountId,
        @Column("balance") Long balance) {

    public static final String TABLE_NAME = "query_side\".\"account_view";
}
