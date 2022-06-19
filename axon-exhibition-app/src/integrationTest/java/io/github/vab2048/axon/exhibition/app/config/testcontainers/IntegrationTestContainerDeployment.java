package io.github.vab2048.axon.exhibition.app.config.testcontainers;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IntegrationTestContainerDeployment {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTestContainerDeployment.class);

    /**
     * Flag to mark whether the containers have started or not.
     */
    private boolean started;

    /**
     * The network to deploy the containers on.
     */
    private final Network network;

    /**
     * The Axon Server container for the deployment.
     */
    private final AxonServerContainer axonServerContainer;

    /**
     * The postgres container for the deployment.
     */
    private final AppDBContainer postgresDBContainer;

    /**
     * {@return a Builder which can create a {@link IntegrationTestContainerDeployment}}
     */
    public static Builder builder() {
        return new Builder();
    }

    public IntegrationTestContainerDeployment(Builder builder) {
        builder.validate();
        this.started = false;
        this.network = builder.network;
        this.axonServerContainer = builder.axonServerContainer;
        this.postgresDBContainer = builder.appDBContainer;
    }

    public static IntegrationTestContainerDeployment newDefaultDeployment() {
        return builder().build();
    }

    public boolean isStarted() {
        return started;
    }

    public Network getNetwork() {
        return network;
    }

    public AxonServerContainer getAxonServerContainer() {
        return axonServerContainer;
    }

    public AppDBContainer getPostgresDBContainer() {
        return postgresDBContainer;
    }

    /**
     * Start the containers.
     */
    public void start() {
        // Start the DB and Axon Server. This will block until the waiting strategy for each container
        // is met i.e. they are up and running.
        Stream.of(axonServerContainer, postgresDBContainer).parallel().forEach(GenericContainer::start);
        started = true;
    }

    /**
     * Given a spring registry this method will register the deployed containers
     * with it - allowing for the application context to connect to the deployment.
     */
    public void registerDeploymentWithApplicationContext(DynamicPropertyRegistry registry) {
        if(!started) {
            throw new IllegalStateException("start() must be called before registering the deployment.");
        }

        axonServerContainer.registerContainer(registry);
        postgresDBContainer.registerContainer(registry);
    }

    /**
     * Reset the state of the DB within the deployment.
     *
     */
    public void resetDeploymentState(EventProcessingConfiguration eventProcessingConfiguration) {
        // Stop the tracking event processors for the application.
        stopTrackingEventProcessors(eventProcessingConfiguration);

        // Even though we are not using Axon Server to store events (we are putting everything in
        // the postgres DB) - we leave this here as an example and reference of how you would reset
        // Axon Server if it was the event store.
        axonServerContainer.resetEvents();

        // Reset the postgres DB.
        // This will clear all data in the DB (including data stored about the tracking event processors).
        postgresDBContainer.resetDBState();

        // Start the tracking event processors again.
        restartTrackingEventProcessors(eventProcessingConfiguration);
    }

    private void restartTrackingEventProcessors(EventProcessingConfiguration eventProcessingConfiguration) {
        Map<String, EventProcessor> eventProcessors = eventProcessingConfiguration.eventProcessors();
        eventProcessors.forEach((processorName, processor) -> {
            // If it is tracking event processor then reset the token and start it up.
            Optional<TrackingEventProcessor> trackingEventProcessor = eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
            if(trackingEventProcessor.isPresent()) {
                TrackingEventProcessor tep = trackingEventProcessor.get();
                tep.start();
                log.info("Restarted tracking event processor: {}", processorName);
            }
        });
    }

    private void stopTrackingEventProcessors(EventProcessingConfiguration eventProcessingConfiguration) {
        Map<String, EventProcessor> eventProcessors = eventProcessingConfiguration.eventProcessors();
        eventProcessors.forEach((processorName, processor) -> {
            // If it is tracking event processor then shut it down.
            Optional<TrackingEventProcessor> trackingEventProcessor = eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
            if(trackingEventProcessor.isPresent()) {
                processor.shutDown();
                log.info("Shutdown tracking event processor: {}", processorName);
            }
        });
    }


    /**
     * Builder class to instantiate a {@link IntegrationTestContainerDeployment}.
     */
    public static class Builder {
        private Network network = Network.newNetwork();
        private AxonServerContainer axonServerContainer = new AxonServerContainer(network);
        private AppDBContainer appDBContainer = new AppDBContainer(network);


        public Builder network(Network network) {
            Objects.requireNonNull(network, "Network cannot be null.");
            this.network = network;
            return this;
        }

        public Builder axonServerContainer(AxonServerContainer axonServerContainer) {
            Objects.requireNonNull(axonServerContainer, "Axon Server container cannot be null.");
            this.axonServerContainer = axonServerContainer;
            return this;
        }

        public Builder appDBContainer(AppDBContainer appDBContainer) {
            Objects.requireNonNull(appDBContainer, "App DB container cannot be null.");
            this.appDBContainer = appDBContainer;
            return this;
        }

        public void validate() {
            // Basic validation that required fields are present.
            Objects.requireNonNull(network, "The network for the deployment is a hard requirement and must be provided.");
            Objects.requireNonNull(axonServerContainer, "The Axon Server container is a hard requirement for the deployment and must be provided.");
            Objects.requireNonNull(appDBContainer, "The PostgreSQL DB container is a hard requirement for the deployment and must be provided.");

            // The network used throughout each container must be the same.
            assertThat(network).isSameAs(axonServerContainer.getNetwork());
            assertThat(network).isSameAs(appDBContainer.getNetwork());
        }

        public IntegrationTestContainerDeployment build() {
            return new IntegrationTestContainerDeployment(this);
        }

    }

}
