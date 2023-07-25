package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountCreditedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountDebitedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.NewAccountCreatedEvent;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Immediately consistent view (subscribing event process) of an account's balance.
 */
@ProcessingGroup(AccountBalanceView.PROCESSING_GROUP)
@Component
public class AccountBalanceView {
    private static final Logger log = LoggerFactory.getLogger(AccountBalanceView.class);
    public static final String PROCESSING_GROUP = "account-balance-command-side-view";
    @Autowired
    public void configure(EventProcessingConfigurer eventProcessing ) {
        eventProcessing.registerSubscribingEventProcessor(PROCESSING_GROUP);
    }

    private Map<UUID, Long> balance;

    @EventHandler
    void on(NewAccountCreatedEvent evt) {
        balance.put(evt.accountId(), 0L);
    }

    @EventHandler
    void on(AccountCreditedEvent evt) {
        Long currentBalance = balance.get(evt.accountId());
        balance.put(evt.accountId(), currentBalance + evt.amount());
    }

    @EventHandler
    void on(AccountDebitedEvent evt) {
        Long currentBalance = balance.get(evt.accountId());
        balance.put(evt.accountId(), currentBalance - evt.amount());
    }


}
