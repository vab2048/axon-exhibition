package io.github.vab2048.axon.exhibition.message_api.common;

import java.time.Instant;
import java.util.function.Supplier;


/**
 * A supplier of the current instant in time.
 * Useful to use rather than resorting to a static Instant.now() call within your methods
 * so that it can be mocked during testing.
 */
@FunctionalInterface
public interface InstantSupplier extends Supplier<Instant> {
    // Default supplier just returns the current instance.
    InstantSupplier DEFAULT_SUPPLIER = Instant::now;

}
