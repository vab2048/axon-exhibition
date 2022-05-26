package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.query.QueryResponses.GetPaymentsQueryResponse;
import io.github.vab2048.axon.exhibition.app.query.payment.PaymentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.*;

@Tags(value = @Tag(name = "Payments", description = "Make a payment between two bank accounts."))
public interface Payments {

    @Operation(summary = "Make a payment between two accounts.",
            operationId = "make-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment Created",
                    content = @Content(schema = @Schema(implementation = MakePaymentResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @PostMapping("/payments")
    ResponseEntity<MakePaymentResponseBody> makePayment(@RequestBody MakePaymentRequestBody requestBody);

    @Operation(summary = "Get the latest state for all payment resources.",
            operationId = "get-payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments State",
                    content = @Content(schema = @Schema(implementation = GetPaymentsQueryResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("/payments/")
    GetPaymentsQueryResponse getPayments();

    @Operation(summary = "Get the latest state for a specific payment resource.",
            operationId = "get-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment State",
                    content = @Content(schema = @Schema(implementation = PaymentView.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("/payments/{id}")
    PaymentView getPaymentView(@PathVariable @Parameter(example = EXAMPLE_UUID_VALUE) UUID id);

    @Operation(summary = """
            Schedule a payment between two accounts.
            The payment must be scheduled for at least 1 minute in the future.""",
            operationId = "schedule-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment Created",
                    content = @Content(schema = @Schema(implementation = MakeScheduledPaymentResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @PostMapping("/scheduled-payments")
    ResponseEntity<MakeScheduledPaymentResponseBody> createdScheduledPayment(MakeScheduledPaymentRequestBody requestBody);

    @Operation(summary = "Cancel a scheduled payment.",
            operationId = "cancel-payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cancelled"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @DeleteMapping("/scheduled-payments/{id}")
    ResponseEntity<?> cancelScheduledPayment(@PathVariable @Parameter(example = EXAMPLE_UUID_VALUE) UUID id);

}