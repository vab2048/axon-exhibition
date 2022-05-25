package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.InternalServerErrorResponseBody;
import io.github.vab2048.axon.exhibition.app.query.account.AccountView;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.CreateAccountRequestBody;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.CreateAccountResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

import static io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.EXAMPLE_UUID_VALUE;

@Tags(value = @Tag(name = "Accounts", description = "Manage our faux 'bank' accounts."))
public interface Accounts {

    @Operation(summary = "Create a new account.",
            operationId = "create-new-account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account Created",
            content = @Content(schema = @Schema(implementation = CreateAccountResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @PostMapping("/accounts")
    ResponseEntity<CreateAccountResponseBody> createNewAccount(CreateAccountRequestBody requestBody);

    @Operation(summary = "Retrieve account details.",
            operationId = "get-account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account View",
                    content = @Content(schema = @Schema(implementation = AccountView.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("/accounts/{id}")
    AccountView getAccount(@PathVariable @Parameter(example = EXAMPLE_UUID_VALUE) UUID id);

}
