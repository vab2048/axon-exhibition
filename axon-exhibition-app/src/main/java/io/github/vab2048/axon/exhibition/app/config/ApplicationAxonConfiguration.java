package io.github.vab2048.axon.exhibition.app.config;

import io.github.vab2048.axon.exhibition.app.command.account.AccountEmailAddressConstraintProjection;
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.TokenSchema;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.modelling.saga.repository.jdbc.PostgresSagaSqlSchema;
import org.axonframework.modelling.saga.repository.jdbc.SagaSchema;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * C.F. axon-spring-boot-autoconfigure: org.axonframework.springboot.autoconfig.JdbcAutoConfiguration
 */
@Configuration
public class ApplicationAxonConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ApplicationAxonConfiguration.class);
    private static final LoggingInterceptor<Message<?>> LOGGING_INTERCEPTOR = new LoggingInterceptor<>();

    /* *************************************************************************************
     * Persistence Related...
     * *************************************************************************************/

    /* Table names for the app's command side */
    private static final String AXON_DB_SCHEMA = "axon";
    private static final String DB_DOMAIN_EVENTS_TABLE_NAME = AXON_DB_SCHEMA + "." + "domainevententry";
    private static final String DB_SNAPSHOTS_TABLE_NAME = AXON_DB_SCHEMA + "." + "snapshotevententry";
    private static final String DB_SAGA_ENTRY_TABLE_NAME = AXON_DB_SCHEMA + "." + "sagaentry";
    private static final String DB_SAGA_ASSOC_VALUE_ENTRY_TABLE_NAME = AXON_DB_SCHEMA + "." + "associationvalueentry";
    private static final String DB_TOKEN_ENTRY_TABLE = AXON_DB_SCHEMA + "."+ "tokenentry";

    @Bean
    public EventStorageEngine eventStorageEngine(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AxonConfiguration axonConfiguration,
            ConnectionProvider connectionProvider,
            TransactionManager transactionManager) {
        EventSchema schema = EventSchema.builder()
                .eventTable(DB_DOMAIN_EVENTS_TABLE_NAME)
                .snapshotTable(DB_SNAPSHOTS_TABLE_NAME)
                .build();

        return JdbcEventStorageEngine.builder()
                // We are using JSON, so we set the data type for serialized event message payloads
                // to be a PGobject (jsonb). In the Postgres DB it will be the 'jsonb' type for the column.
                .dataType(PGobject.class)
                .connectionProvider(connectionProvider)                       // Mandatory
                .eventSerializer(axonConfiguration.eventSerializer())         // Mandatory
                .schema(schema)
                .snapshotFilter(axonConfiguration.snapshotFilter())
                .snapshotSerializer(axonConfiguration.serializer())           // Mandatory
                .transactionManager(transactionManager)                       // Mandatory
                .upcasterChain(axonConfiguration.upcasterChain())
                .build();
    }

    // The Event store `EmbeddedEventStore` delegates actual storage and retrieval of events to our
    // `EventStorageEngine` that we have defined as a bean.
    @Bean
    public EmbeddedEventStore eventStore(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AxonConfiguration configuration,
            EventStorageEngine storageEngine) {
        return EmbeddedEventStore.builder()
                .storageEngine(storageEngine)
                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }

    @Bean
    public JdbcTokenStore tokenStore(ConnectionProvider connectionProvider, Serializer serializer) {
        TokenSchema schema = TokenSchema.builder().setTokenTable(DB_TOKEN_ENTRY_TABLE).build();
        return JdbcTokenStore.builder()
                // contentType defines the type which the tracking token should be serialized to.
                // We are using a PGobject to represent the jsonb type which is actually used in the DB.
                .contentType(PGobject.class)
                .connectionProvider(connectionProvider)
                .serializer(serializer)
                .schema(schema)
                .build();
    }


    @Bean
    public JdbcSagaStore sagaStore(ConnectionProvider connectionProvider, Serializer serializer) {
        SagaSchema schema = new SagaSchema(DB_SAGA_ENTRY_TABLE_NAME, DB_SAGA_ASSOC_VALUE_ENTRY_TABLE_NAME);
        return JdbcSagaStore.builder()
                .sqlSchema(new PostgresSagaSqlSchema(schema))
                .connectionProvider(connectionProvider)
                .serializer(serializer)
                .build();
    }

    /* *************************************************************************************
     * Logging...
     * *************************************************************************************/

//    @Autowired
//    public void configureLoggingInterceptor(
//            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") Configurer configurer) {
//        // Registers the LoggingInterceptor on all infrastructure once they've been initialized by the Configurer:
//        configurer.onInitialize(config -> {
//            config.onStart(Phase.INSTRUCTION_COMPONENTS + 1, () -> {
//                config.commandBus().registerHandlerInterceptor(LOGGING_INTERCEPTOR);
//                config.commandBus().registerDispatchInterceptor(LOGGING_INTERCEPTOR);
//                config.eventBus().registerDispatchInterceptor(LOGGING_INTERCEPTOR);
//                config.queryBus().registerHandlerInterceptor(LOGGING_INTERCEPTOR);
//                config.queryBus().registerDispatchInterceptor(LOGGING_INTERCEPTOR);
//                config.queryUpdateEmitter().registerDispatchInterceptor(LOGGING_INTERCEPTOR);
//            });
//        });
//    }

    /* *************************************************************************************
     * Deadlines...
     * *************************************************************************************/

    @Bean
    public SimpleDeadlineManager simpleDeadlineManager(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AxonConfiguration configuration,
            TransactionManager transactionManager) {
        return SimpleDeadlineManager.builder()
                .scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
                .transactionManager(transactionManager)
                .build();
    }

    /* *************************************************************************************
     * Processing Groups...
     * *************************************************************************************/

    @Autowired
    public void configureEventProcessingGroups(EventProcessingConfigurer processingConfigurer) {
        // Specific configuration for the AccountEmailAddressConstraintProjection processing group...
        // - As a subscribing event processor
        // - Which propagates errors.
        processingConfigurer.registerSubscribingEventProcessor(AccountEmailAddressConstraintProjection.PROCESSING_GROUP_NAME);
        processingConfigurer.registerListenerInvocationErrorHandler(AccountEmailAddressConstraintProjection.PROCESSING_GROUP_NAME,
                conf -> PropagatingErrorHandler.instance());
    }

    /* *************************************************************************************
     * Snapshotting...
     * *************************************************************************************/
    public static final String ACCOUNT_AGGREGATE_SNAPSHOT_TRIGGER_DEFINITION_BEAN_NAME = "accountAggregateSnapshotTrigger";
    public static final int ACCOUNT_AGGREGATE_EVENT_COUNT_SNAPSHOT_TRIGGER = 3;
    @Bean(ACCOUNT_AGGREGATE_SNAPSHOT_TRIGGER_DEFINITION_BEAN_NAME)
    public SnapshotTriggerDefinition accountAggregateSnapshotTrigger(Snapshotter snapshotter) {
        // After 3 events take a snapshot (this number is low just so we can see trigger a snapshot easily).
        return new EventCountSnapshotTriggerDefinition(snapshotter, ACCOUNT_AGGREGATE_EVENT_COUNT_SNAPSHOT_TRIGGER);
    }

}
