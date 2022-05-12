package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.InternalServerErrorResponseBody;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.MakePaymentRequestBody;
import io.github.vab2048.axon.exhibition.app.query.payment.PaymentView;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

import static io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.EXAMPLE_UUID_VALUE;

@Tags(value = @Tag(name = "Payments", description = "Make a payment between two bank accounts."))
public interface Payment {

    @Operation(summary = "Make a payment between two accounts.",
            operationId = "create-new-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account Created",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @PostMapping("/payments")
    UUID makePayment(@RequestBody MakePaymentRequestBody requestBody);


    @Operation(summary = "Retrieve payment details.",
            operationId = "get-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Status",
                    content = @Content(schema = @Schema(implementation = PaymentView.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("/payments/{id}")
    PaymentView getPaymentView(@PathVariable @Parameter(example = EXAMPLE_UUID_VALUE) UUID id);

}