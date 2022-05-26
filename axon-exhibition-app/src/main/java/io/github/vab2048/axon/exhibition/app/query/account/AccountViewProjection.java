package io.github.vab2048.axon.exhibition.app.query.account;

import io.github.vab2048.axon.exhibition.app.query.QueryResponses;
import io.github.vab2048.axon.exhibition.app.query.QueryResponses.GetAccountsQueryResponse;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountCreditedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountDebitedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.NewAccountCreatedEvent;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetAccountQuery;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetAccountsQuery;
import org.apache.commons.collections4.IterableUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountViewProjection {
    private static final Logger log = LoggerFactory.getLogger(AccountViewProjection.class);

    /**
     * Used for performing inserts.
     */
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    /**
     * Used for performing updates.
     */
    private final AccountViewRepository repository;

    public AccountViewProjection(JdbcAggregateTemplate jdbcAggregateTemplate, AccountViewRepository repository) {
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.repository = repository;
    }

    @EventHandler
    void on(NewAccountCreatedEvent evt) {
        log.debug("Projecting: {}", evt);
        var accountView = new AccountView(evt.accountId(), evt.emailAddress(), evt.openingBalance());
        jdbcAggregateTemplate.insert(accountView);
    }

    @EventHandler
    void on(AccountCreditedEvent evt) {
        log.debug("Projecting: {}", evt);
        var accountView = repository.findById(evt.accountId()).orElseThrow();
        var updatedAccountView = new AccountView(accountView.accountId(),
                accountView.emailAddress(), accountView.balance() + evt.amount());
        repository.save(updatedAccountView);
    }

    @EventHandler
    void on(AccountDebitedEvent evt) {
        log.debug("Projecting: {}", evt);
        var accountView = repository.findById(evt.accountId()).orElseThrow();
        var updatedAccountView = new AccountView(accountView.accountId(),
                accountView.emailAddress(), accountView.balance() - evt.amount());
        repository.save(updatedAccountView);
    }

    @QueryHandler
    public AccountView getAccount(GetAccountQuery query) {
        log.debug("Handling: {}", query);
        return repository.findById(query.id()).orElseThrow();
    }

    @QueryHandler
    public GetAccountsQueryResponse getAccounts(GetAccountsQuery query) {
        log.debug("Handling: {}", query);
        return new GetAccountsQueryResponse(IterableUtils.toList(repository.findAll()));
    }


}
