package io.github.vab2048.axon.exhibition.app.controller.dto;

import java.util.UUID;

public final class ControllerDTOs {
    private ControllerDTOs() { /* Non instantiable class */ }


    public record MakePaymentRequestBody(UUID sourceBankAccountId, UUID targetBankAccountId, long amount) {}

}
