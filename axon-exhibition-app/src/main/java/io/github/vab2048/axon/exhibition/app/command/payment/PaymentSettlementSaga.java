package io.github.vab2048.axon.exhibition.app.command.payment;

import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountCreditedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.AccountDebitedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreditAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.DebitAccountCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.MarkPaymentAsCompletedCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentSettlementTriggeredEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Process manager for a payment settlement i.e. a transfer of money from one account to another.
 */
@Saga
public class PaymentSettlementSaga {
    private static final Logger log = LoggerFactory.getLogger(PaymentSettlementSaga.class);
    public static final String PAYMENT_ID_ASSOCIATION_PROPERTY = "paymentId";

    private transient CommandGateway commandGateway;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private long amount;

    @Deprecated
    public PaymentSettlementSaga() {
        /* For framework use only. */
    }

    // Since we are using Jackson as a serializer we unfortunately need getters and setters for each field
    // otherwise state will not be persisted.
    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(UUID destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = PAYMENT_ID_ASSOCIATION_PROPERTY)
    public void on(PaymentSettlementTriggeredEvent event) {
        this.sourceAccountId = event.sourceAccountId();
        this.destinationAccountId = event.destinationAccountId();
        this.amount = event.amount();
        var cmd = new DebitAccountCommand(sourceAccountId, event.paymentId(), amount);
        commandGateway.sendAndWait(cmd);
    }

    @SagaEventHandler(associationProperty = PAYMENT_ID_ASSOCIATION_PROPERTY)
    public void on(AccountDebitedEvent event) {
        // On successfully debiting one side of the transfer we need to then credit the other.
        var cmd = new CreditAccountCommand(destinationAccountId, event.paymentId(), event.amount());
        commandGateway.sendAndWait(cmd);
    }

    @SagaEventHandler(associationProperty = PAYMENT_ID_ASSOCIATION_PROPERTY)
    @EndSaga
    public void on(AccountCreditedEvent event) {
        // When the account has been credited then mark the transfer aggregate as being complete.
        commandGateway.sendAndWait(new MarkPaymentAsCompletedCommand(event.paymentId()));
    }
}
