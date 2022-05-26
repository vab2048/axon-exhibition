package io.github.vab2048.axon.exhibition.message_api.query;

import java.util.UUID;

/**
 * The query messages.
 */
public class QueryAPI {
    private QueryAPI() { /* Non instantiable class */ }

    public record GetAccountsQuery() {}
    public record GetAccountQuery(UUID id) {}

    public record GetPaymentsQuery() {}
    public record GetPaymentQuery(UUID id) {}

}
