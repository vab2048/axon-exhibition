package io.github.vab2048.axon.exhibition.app.query;

import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentCompletedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentCreatedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentFailedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetPaymentView;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;


@Component
public class PaymentViewProjection {

    /**
     * Used for performing inserts.
     */
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    /**
     * Used for performing updates.
     */
    private final PaymentViewRepository repository;


    public PaymentViewProjection(JdbcAggregateTemplate jdbcAggregateTemplate, PaymentViewRepository repository) {
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.repository = repository;
    }


    @EventHandler
    void on(PaymentCreatedEvent event) {
        var paymentView = new PaymentView(event.paymentId(), event.sourceAccountId(), event.destinationAccountId(), event.amount(), event.status());
        jdbcAggregateTemplate.insert(paymentView);
    }

    @EventHandler
    void on(PaymentCompletedEvent event) {
        var paymentView = repository.findById(event.paymentId()).orElseThrow();
        var updatedPaymentView = new PaymentView(paymentView.paymentId(), paymentView.sourceAccountId(),
                paymentView.destinationAccountId(), paymentView.amount(), PaymentStatus.COMPLETED);
        repository.save(updatedPaymentView);
    }

    @EventHandler
    void on(PaymentFailedEvent event) {
        var paymentView = repository.findById(event.paymentId()).orElseThrow();
        var updatedPaymentView = new PaymentView(paymentView.paymentId(), paymentView.sourceAccountId(),
                paymentView.destinationAccountId(), paymentView.amount(), PaymentStatus.FAILED);
        repository.save(updatedPaymentView);
    }


    @QueryHandler
    PaymentView getPayment(GetPaymentView query) {
        return repository.findById(query.id()).orElseThrow();
    }

}
