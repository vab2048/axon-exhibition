package io.github.vab2048.axon.exhibition.app.config.testcontainers;

/**
 * Records which are useful when dealing with test containers.
 */
public class Data {
    private Data() { /* Non instantiable class */ }

    /**
     * Tuple of a container's IP address and mapped port.
     */
    public record ContainerIpAndMappedPort(String ipAddress, int mappedPort) {}

}
