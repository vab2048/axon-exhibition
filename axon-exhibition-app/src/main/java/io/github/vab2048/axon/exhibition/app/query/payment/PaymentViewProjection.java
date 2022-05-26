package io.github.vab2048.axon.exhibition.app.query.payment;

import io.github.vab2048.axon.exhibition.app.query.QueryResponses.GetPaymentsQueryResponse;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentCompletedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentCreatedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.PaymentFailedEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.ScheduledPaymentCancelledEvent;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentStatus;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetPaymentQuery;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetPaymentsQuery;
import org.apache.commons.collections4.IterableUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;


@Component
public class PaymentViewProjection {
    private static final Logger log = LoggerFactory.getLogger(PaymentViewProjection.class);

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
        log.debug("Projecting: {}", event);
        var paymentView = new PaymentView(event.paymentId(), event.sourceAccountId(), event.destinationAccountId(),
                event.amount(), event.status(), event.settlementInitiationTime());
        jdbcAggregateTemplate.insert(paymentView);
    }

    @EventHandler
    void on(PaymentCompletedEvent event) {
        log.debug("Projecting: {}", event);
        var paymentView = repository.findById(event.paymentId()).orElseThrow();
        var updatedPaymentView = new PaymentView(paymentView.paymentId(), paymentView.sourceAccountId(),
                paymentView.destinationAccountId(), paymentView.amount(), PaymentStatus.COMPLETED, paymentView.settlementInitiationTime());
        repository.save(updatedPaymentView);
    }

    @EventHandler
    void on(ScheduledPaymentCancelledEvent event) {
        log.debug("Projecting: {}", event);
        var paymentView = repository.findById(event.paymentId()).orElseThrow();
        var updatedPaymentView = new PaymentView(paymentView.paymentId(), paymentView.sourceAccountId(),
                paymentView.destinationAccountId(), paymentView.amount(), PaymentStatus.CANCELLED,
                paymentView.settlementInitiationTime());
        repository.save(updatedPaymentView);
    }

    @EventHandler
    void on(PaymentFailedEvent event) {
        log.debug("Projecting: {}", event);
        var paymentView = repository.findById(event.paymentId()).orElseThrow();
        var updatedPaymentView = new PaymentView(paymentView.paymentId(), paymentView.sourceAccountId(),
                paymentView.destinationAccountId(), paymentView.amount(), PaymentStatus.FAILED, paymentView.settlementInitiationTime());
        repository.save(updatedPaymentView);
    }

    @QueryHandler
    PaymentView getPayment(GetPaymentQuery query) {
        log.debug("Handling: {}", query);
        return repository.findById(query.id()).orElseThrow();
    }

    @QueryHandler
    public GetPaymentsQueryResponse getPayments(GetPaymentsQuery query) {
        log.debug("Handling: {}", query);
        return new GetPaymentsQueryResponse(IterableUtils.toList(repository.findAll()));
    }


}
