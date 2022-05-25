package io.github.vab2048.axon.exhibition.app.query.payment;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table(PaymentView.TABLE_NAME)
public record PaymentView(
        @Id @Column("payment_id") UUID paymentId,
        @Column("source_account_id") UUID sourceAccountId,
        @Column("destination_account_id") UUID destinationAccountId,
        @Column("amount") long amount,
        @Column("status") PaymentStatus status,
        @Column("settlement_initiation_time") Instant settlementInitiationTime) {

    public static final String TABLE_NAME = "query_side\".\"payment_view";
}
