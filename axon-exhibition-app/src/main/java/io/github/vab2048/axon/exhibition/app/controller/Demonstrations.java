package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.InternalServerErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.GetMapping;

@Tags(value = @Tag(name = "Example Demonstrations", description = """
        Hit an endpoint to demonstrate a specific feature of the Axon Framework."""))
public interface Demonstrations {
    String COMMON_DESCRIPTION_SET_BASED_VALIDATION = """
        # Set based validation scenario brief:
        
        We have a constraint in our model that every account must be associated to a unique email address.
        In other words the email address must be unique across the set of all accounts (hence
        set based validation).\n
        
        This endpoint issues a number of commands (randomly decided to be between 2 and 5) to create new accounts.
        All commands will have a unique email address except the last one.
        The handling of the last command will trigger a violation of the uniqueness constraint.
        """;



    @Operation(summary = "Illustrate set based validation at a consistency boundary of an aggregate.",
            operationId = "set-based-validation-1",
            description = COMMON_DESCRIPTION_SET_BASED_VALIDATION + """
                    # Consistency:
                    This specific endpoint will maintain consistency at the level of the aggregate.
                    That means that the first few commands will succeed in creating their respective account aggregates,
                    but the last one will fail.\n
                    
                    This is in contrast to the other set based validation endpoint in which NO aggregates
                    are created at all (even though only the last command fails).
                    
                    Although you will receive an error as a response - check the DB and you will see the aggregates
                    for which the commands did not fail have had their creation events persisted.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("demo/set-based-validation/consistency-at-aggregate-threshold")
    void setBasedValidation1();

    @Operation(summary = "Illustrate set based validation at a consistency boundary involving multiple aggregates.",
            operationId = "set-based-validation-2",
            description = COMMON_DESCRIPTION_SET_BASED_VALIDATION + """
                    # Consistency:
                    This specific endpoint will maintain consistency at the level of the entire transaction.
                    That means that the first few commands will initially succeed in creating their respective
                    account aggregates, but the last one will fail. As a result the ENTIRE transaction will
                    be rolled back and NO account aggregates will be created at all.\n
                    
                    This is in contrast to the other set based validation endpoint in which the commands which succeed
                    result in the aggregates being created and only the last command which fails does not create
                    its respective aggregate.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("demo/set-based-validation/consistency-at-multiple-aggregate-threshold")
    void setBasedValidation2();

    @Operation(summary = """
            Issue a number of commands which will trigger the snapshotting of an account aggregate.""",
            operationId = "trigger-account-snapshot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = InternalServerErrorResponseBody.class)))
    })
    @GetMapping("/demo/trigger-account-snapshot")
    void triggerAccountSnapshot();

}
