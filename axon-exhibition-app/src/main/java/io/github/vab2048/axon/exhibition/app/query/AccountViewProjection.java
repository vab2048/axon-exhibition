package io.github.vab2048.axon.exhibition.app.query;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountCreditedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountDebitedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.NewAccountCreatedEvent;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetAccountView;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountViewProjection {

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
        var balance = 0L; // Starting balance at time of account creation is 0.
        var accountView = new AccountView(evt.accountId(), balance);
        jdbcAggregateTemplate.insert(accountView);
    }

    @EventHandler
    void on(AccountCreditedEvent evt) {
        var accountView = repository.findById(evt.accountId()).orElseThrow();
        var updatedAccountView = new AccountView(accountView.accountId(), accountView.balance() + evt.amount());
        repository.save(updatedAccountView);
    }

    @EventHandler
    void on(AccountDebitedEvent evt) {
        var accountView = repository.findById(evt.accountId()).orElseThrow();
        var updatedAccountView = new AccountView(accountView.accountId(), accountView.balance() - evt.amount());
        repository.save(updatedAccountView);
    }

    @QueryHandler
    public AccountView getAccount(GetAccountView query) {
        return repository.findById(query.id()).orElseThrow();
    }
}
