package io.github.vab2048.axon.exhibition.app.controller.dto;

import java.time.Instant;
import java.util.UUID;

public final class ControllerDTOs {
    private ControllerDTOs() { /* Non instantiable class */ }
    public static final String EXAMPLE_UUID_VALUE = "123e4567-e89b-12d3-a456-426614174000";

    /* *************************************************************
     * Errors
     * *************************************************************/
    public record InternalServerErrorResponseBody(
            Instant errorTimestamp,
            String message,
            String description) {}

    /* *************************************************************
     * Accounts
     * *************************************************************/
    public record CreateAccountRequestBody(String emailAddress) {}
    public record CreateAccountResponseBody(UUID id, String emailAddress) {}

    /* *************************************************************
     * Payments
     * *************************************************************/
    public record MakePaymentRequestBody(UUID sourceBankAccountId, UUID destinationBankAccountId, long amount) {}
    public record MakePaymentResponseBody(UUID paymentId) {}
    public record MakeScheduledPaymentRequestBody(UUID sourceBankAccountId, UUID destinationBankAccountId, long amount, Instant settlementInitiationTime) {}
    public record MakeScheduledPaymentResponseBody(UUID paymentId) {}


}
