package io.github.vab2048.axon.exhibition.app.config.testcontainers;

import io.github.vab2048.axon.exhibition.app.config.testcontainers.Data.ContainerIpAndMappedPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class AxonServerContainer extends GenericContainer<AxonServerContainer> {
    // Logger
    private static final Logger log = LoggerFactory.getLogger(AxonServerContainer.class);

    // Container version definition.
    public static final String DEFAULT_VERSION = "4.5.12";
    public static final String CONTAINER_NAME = "axoniq/axonserver";
    public static final String CONTAINER_IMAGE = CONTAINER_NAME + ":" + DEFAULT_VERSION;

    // Ports we want exposed on the container.
    public static final int AXON_SERVER_TOMCAT_PORT_CONTAINER = 8024;
    public static final int AXON_SERVER_GATEWAY_PORT_CONTAINER = 8124;

    // The endpoint we want checked to see whether the container has started.
    public static final String UP_ENDPOINT = "/actuator/info";
    public static final Integer UP_ENDPOINT_PORT = 8024;
    public static final Long HEALTH_CHECK_TIMOUT_SECONDS = 120L;

    // Network related fields
    public static final String AXON_SERVER_NETWORK_ALIAS = "axon-server";

    // Default HTTP client used for interacting with HTTP API
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    /**
     * Initialise the container with the given network.
     * @param network Network the container will belong to.
     */
    public AxonServerContainer(Network network) {
        super(CONTAINER_IMAGE);
        setNetwork(network);
        log.debug("""
        \s
        ------------------------------------------------------------
        Creating new testcontainer: {}
        ------------------------------------------------------------""",
                CONTAINER_IMAGE);
    }

    /**
     * Our custom configuration for the container that will be called when the container is started.
     */
    @Override
    protected void configure() {
        // Expose the given ports on the container...
        withExposedPorts(AXON_SERVER_TOMCAT_PORT_CONTAINER, AXON_SERVER_GATEWAY_PORT_CONTAINER);

        // Launch with the following environment variables set
        withEnv(Map.of(
                // Make sure dev mode is enabled, so we can drop events as needed.
                "axoniq.axonserver.devmode.enabled", "true"
        ));

        // Connect to the network requested with the given alias.
        withNetwork(getNetwork());
        withNetworkAliases(AXON_SERVER_NETWORK_ALIAS);

        // Wait for the given endpoint at the given port to return a 200 status code.
        waitingFor(Wait.forHttp(UP_ENDPOINT).forPort(UP_ENDPOINT_PORT));

        // If unable to startup within the given time then fail.
        withStartupTimeout(Duration.of(HEALTH_CHECK_TIMOUT_SECONDS, ChronoUnit.SECONDS));
    }


    /**
     * Register the relevant dynamic application properties so that when the test's application
     * context is loaded it can connect to the Axon Server container.
     */
    public void registerContainer(DynamicPropertyRegistry registry) {
        var ipAndMappedPort = getContainerIpAddressAndGatewayMappedPort();
        log.info("Dynamically registering Axon Server for application context: {}", ipAndMappedPort);
        registry.add("axon.axonserver.servers",
                () -> ipAndMappedPort.ipAddress() + ":" + ipAndMappedPort.mappedPort()
        );
    }

    public GenericContainer<?> getContainer() {
        return this;
    }

    /**
     * Return the alias of the container on the given network.
     */
    public String getNetworkAlias() {
        return AXON_SERVER_NETWORK_ALIAS;
    }

    /**
     * Grab the Axon Server container's IP and mapped port for the tomcat HTTP API.
     *
     * Will throw if the container has not started.
     *
     * For reference, the expected ports are:
     * - 8024: Axon Server Tomcat API
     * - 8124: Axon Server Gateway
     */
    public ContainerIpAndMappedPort getContainerIpAddressAndHTTPApiMappedPort() {
        return new ContainerIpAndMappedPort(getHost(), getMappedPort(8024));
    }

    /**
     * Grab the Axon Server container's IP and mapped port for the gateway.
     *
     * Will throw if the container has not started.
     *
     * For reference, the expected ports are:
     * - 8024: Axon Server Tomcat API
     * - 8124: Axon Server Gateway
     */
    public ContainerIpAndMappedPort getContainerIpAddressAndGatewayMappedPort() {
        return new ContainerIpAndMappedPort(getHost(), getMappedPort(8124));
    }



    public void resetEvents() {
        var ipAndHTTPApiMappedPort = getContainerIpAddressAndHTTPApiMappedPort();
        resetEvents(ipAndHTTPApiMappedPort.ipAddress(), ipAndHTTPApiMappedPort.mappedPort());
    }

    public void resetEvents(String serverAddress, int serverPort) {
        resetEvents("http", serverAddress, serverPort);
    }

    public void resetEvents(String protocol, String serverAddress, int serverPort) {
        String endpoint = "/v1/devmode/purge-events";
        String URL = protocol + "://" + serverAddress + ":" + serverPort + endpoint;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).DELETE().build();
        log.info("Issuing HTTP request to reset the event store (purge-events): {}", request);

        // Try to make the HTTP call.
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String s = String.format("Received non 200 response to reset event store: %s", response);

                // Change the error string if it is a 404.
                if (response.statusCode() == 404) {
                    s = String.format("Received 404 response from Axon Server for endpoint: %s. Are you sure dev " +
                            "mode is enabled?", response);
                }
                throw new IllegalStateException(s);
            }
        }
        // If it fails because of a timeout - it is probably because no AxonServer instance is running.
        catch (IOException | InterruptedException ex) {
            String s = String.format("Unable to connect to Axon Server (%s:%s). Are you sure it is running?", serverAddress, serverPort);
            throw new IllegalStateException(s, ex);
        }
    }


}

