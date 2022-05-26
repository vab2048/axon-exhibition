package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.*;
import io.github.vab2048.axon.exhibition.app.query.QueryResponses.GetPaymentsQueryResponse;
import io.github.vab2048.axon.exhibition.app.query.payment.PaymentView;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.CancelScheduledPaymentCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.CreateImmediatePaymentCommand;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.CreateScheduledPaymentCommand;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetPaymentQuery;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI.GetPaymentsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class PaymentsController implements Payments {
    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public PaymentsController(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @Override
    public ResponseEntity<MakePaymentResponseBody> makePayment(MakePaymentRequestBody requestBody) {
        UUID paymentId = commandGateway.sendAndWait(new CreateImmediatePaymentCommand(
                UUID.randomUUID(),
                requestBody.sourceBankAccountId(),
                requestBody.destinationBankAccountId(),
                requestBody.amount()));

        // Get the URI for the newly created REST resource...
        var locationURI = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(paymentId).toUri();

        return ResponseEntity
                .created(locationURI)
                .body(new MakePaymentResponseBody(paymentId));
    }

    @Override
    public GetPaymentsQueryResponse getPayments() {
        var query = new GetPaymentsQuery();
        try {
            return queryGateway.query(query, GetPaymentsQueryResponse.class).get();
        } catch (InterruptedException | ExecutionException e) {
            var msg = "Error in executing query: %s".formatted(query);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public PaymentView getPaymentView(UUID id) {
        var query = new GetPaymentQuery(id);
        try {
            return queryGateway.query(query, PaymentView.class).get();
        } catch (InterruptedException | ExecutionException e) {
            var msg = "Error in executing query: %s".formatted(query);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }


    @Override
    public ResponseEntity<MakeScheduledPaymentResponseBody> createdScheduledPayment(MakeScheduledPaymentRequestBody requestBody) {
        UUID paymentId = commandGateway.sendAndWait(new CreateScheduledPaymentCommand(
                UUID.randomUUID(),
                requestBody.sourceBankAccountId(),
                requestBody.destinationBankAccountId(),
                requestBody.amount(),
                requestBody.settlementInitiationTime()
        ));

        // Get the URI for the newly created REST resource...
        var locationURI = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(paymentId).toUri();

        return ResponseEntity
                .created(locationURI)
                .body(new MakeScheduledPaymentResponseBody(paymentId));
    }

    @Override
    public ResponseEntity<?> cancelScheduledPayment(UUID id) {
        commandGateway.sendAndWait(new CancelScheduledPaymentCommand(id));
        return ResponseEntity.noContent().build();
    }
}
