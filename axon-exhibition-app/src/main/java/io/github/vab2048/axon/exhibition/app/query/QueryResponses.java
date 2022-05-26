package io.github.vab2048.axon.exhibition.app.query;

import io.github.vab2048.axon.exhibition.app.query.account.AccountView;
import io.github.vab2048.axon.exhibition.app.query.payment.PaymentView;

import java.util.List;

/**
 * Where the response type for a query would normally be a polymorphic type e.g. {@code List<AccountView>}
 * it is much better to wrap that polymorphic type into another object and use that as the
 * query response type.
 *
 * This is so that we can avoid having to configure Jackson to deal with the polymorphic
 * types - we just return this single type which contains the list within it.
 * Trust me this is so much easier than dealing with the headache of configuring Jackson
 * (See: <a href="https://github.com/AxonFramework/AxonFramework/issues/1418">Issue 1418</a>
 *       <a href="https://github.com/AxonFramework/AxonFramework/issues/1331">Issue 1331</a>).
 */
public class QueryResponses {

    public record GetAccountsQueryResponse(List<AccountView> accounts) {}

    public record GetPaymentsQueryResponse(List<PaymentView> payments) {}
}
