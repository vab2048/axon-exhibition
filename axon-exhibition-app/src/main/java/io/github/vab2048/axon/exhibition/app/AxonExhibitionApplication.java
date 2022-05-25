package io.github.vab2048.axon.exhibition.app;

import io.github.vab2048.axon.exhibition.message_api.common.InstantSupplier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AxonExhibitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonExhibitionApplication.class, args);
    }

    @Bean
    InstantSupplier instantSupplier() {
        return InstantSupplier.DEFAULT_SUPPLIER;
    }
}
