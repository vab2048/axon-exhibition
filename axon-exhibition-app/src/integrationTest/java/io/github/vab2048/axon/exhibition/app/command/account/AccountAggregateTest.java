package io.github.vab2048.axon.exhibition.app.command.account;

import io.github.vab2048.axon.exhibition.app.config.testcontainers.IntegrationTestContainerDeployment;
import io.github.vab2048.axon.exhibition.message_api.command.AccountCommandMessageAPI.CreateNewAccountCommand;
import io.github.vab2048.axon.exhibition.test.utils.command.account.AccountAggregateMessageGenerator;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.SQLException;
import java.util.UUID;

import static io.github.vab2048.axon.exhibition.app.command.account.AccountEmailAddressConstraintProjection.accountCreationDuplicateKeyExceptionMessage;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class AccountAggregateTest {
    // ------------------------------- Static ------------------------------------
    private static final Logger log = LoggerFactory.getLogger(AccountAggregateTest.class);
    private static final AccountAggregateMessageGenerator messageGenerator = new AccountAggregateMessageGenerator();

    /**
     * The deployment of containers for the tests in the projection.
     */
    private static final IntegrationTestContainerDeployment deployment = IntegrationTestContainerDeployment.newDefaultDeployment();

    /**
     * Initialise the relevant properties for the application context to be
     * able to connect to the deployment containers.
     *
     * @param registry Spring property registry we will add our properties to.
     */
    @DynamicPropertySource
    public static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        log.info("registerDynamicProperties called in {}. ", AccountAggregateTest.class);
        deployment.start();
        deployment.registerDeploymentWithApplicationContext(registry);
    }

    // ------------------------------- Fields ------------------------------------

    @Autowired
    private EventProcessingConfiguration eventProcessingConfiguration;

    @Autowired
    private CommandGateway commandGateway;

    // ------------------------------- Test Methods ------------------------------------

    @BeforeEach
    void setUp() {
        // Reset the state of the DB (command and query side).
        deployment.resetDeploymentState(eventProcessingConfiguration);
    }

    @Test
    void createNewAccountCommand_WithDuplicateEmailAddress_Fails() throws SQLException {
        /* Given: We have an account which already exists. */
        var creation = messageGenerator.accountCreation();
        var creationCmd = creation.cmd();
        commandGateway.sendAndWait(creationCmd);

        /* When: We issue a command with the same email address. */
        var creationCmdWithSameEmail = new CreateNewAccountCommand(UUID.randomUUID(), creationCmd.emailAddress());
        assertThatThrownBy(() -> {
            commandGateway.sendAndWait(creationCmdWithSameEmail);
        })
                /* Then: A CommandExecutionException is thrown. */
                .isInstanceOf(CommandExecutionException.class)
                .hasMessageContaining(accountCreationDuplicateKeyExceptionMessage(creationCmdWithSameEmail.emailAddress()));
    }


    // TODO: successful creation, credit, debit, etc.


}
