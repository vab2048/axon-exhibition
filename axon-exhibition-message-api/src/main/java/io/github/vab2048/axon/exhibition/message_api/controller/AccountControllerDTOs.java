package io.github.vab2048.axon.exhibition.message_api.controller;

import java.util.UUID;

public final class AccountControllerDTOs {
    private AccountControllerDTOs() { /* Non instantiable class */ }

    public record CreateAccountRequestBody(String emailAddress) {}
    public record CreateAccountResponseBody(UUID id, String emailAddress) {}

}
