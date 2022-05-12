package io.github.vab2048.axon.exhibition.app.controller.dto;

import java.time.Instant;
import java.util.UUID;

public final class ControllerDTOs {
    private ControllerDTOs() { /* Non instantiable class */ }
    public static final String EXAMPLE_UUID_VALUE = "123e4567-e89b-12d3-a456-426614174000";

    public record MakePaymentRequestBody(UUID sourceBankAccountId, UUID targetBankAccountId, long amount) {}

    public record InternalServerErrorResponseBody(
            Instant errorTimestamp,
            String message,
            String description) {}
}
