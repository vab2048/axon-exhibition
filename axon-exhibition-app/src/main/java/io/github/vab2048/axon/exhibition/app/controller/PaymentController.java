package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.MakePaymentRequestBody;
import io.github.vab2048.axon.exhibition.app.query.PaymentView;
import io.github.vab2048.axon.exhibition.message_api.command.PaymentCommandMessageAPI.CreatePaymentCommand;
import io.github.vab2048.axon.exhibition.message_api.query.QueryAPI;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class PaymentController implements Payment {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public PaymentController(QueryGateway queryGateway, CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @Override
    public UUID makePayment(MakePaymentRequestBody requestBody) {
        // Return the ID of the new payment.
        return commandGateway.sendAndWait(new CreatePaymentCommand(
                UUID.randomUUID(),
                requestBody.sourceBankAccountId(),
                requestBody.targetBankAccountId(),
                requestBody.amount()));
    }

    @Override
    public PaymentView getPaymentView(UUID id) {
        var query = new QueryAPI.GetPaymentView(id);
        try {
            return queryGateway.query(query, PaymentView.class).get();
        } catch (InterruptedException | ExecutionException e) {
            var msg = "Error in executing query: %s".formatted(query);
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
