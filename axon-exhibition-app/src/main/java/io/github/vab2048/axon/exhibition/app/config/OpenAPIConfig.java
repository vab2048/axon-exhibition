package io.github.vab2048.axon.exhibition.app.config;

import com.google.common.collect.MoreCollectors;
import io.github.vab2048.axon.exhibition.app.controller.Accounts;
import io.github.vab2048.axon.exhibition.app.controller.Demonstrations;
import io.github.vab2048.axon.exhibition.app.controller.Payments;
import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class OpenAPIConfig {
    private static final Logger log = LoggerFactory.getLogger(OpenAPIConfig.class);

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Axon Exhibition")
                        .version("0.0.1")
                        .description("""
                                API for interacting with the Axon Exhibition app.
                                """)
                );
    }

    @Bean
    public OpenApiCustomiser customiseOpenAPI() {
        return (OpenAPI openAPI) -> {
            // Sort the tags so that they appear in our custom order.
            sortTags(openAPI);

            // Sort the schemas.
            sortSchemas(openAPI);

            // Add the basic responses which will be there for every request.
            addCommonResponsesToEachEndpoint(openAPI);
        };
    }

    /*
     * Add the definition of the common response(s) to every endpoint.
     */
    private void addCommonResponsesToEachEndpoint(OpenAPI openAPI) {
        // Every endpoint could return a 500 error.

        // Add the relevant type for the response to the schema
        openAPI.getComponents().getSchemas().putAll(ModelConverters.getInstance().read(ControllerDTOs.InternalServerErrorResponseBody.class));

        // Create a schema reference for the type.
        Schema<?> internalServerErrorResponseSchemaRef = new Schema<>();
        internalServerErrorResponseSchemaRef.setName("Error");
        internalServerErrorResponseSchemaRef.set$ref("#/components/schemas/InternalServerErrorResponseBody");

        // Add it as a response for every single endpoint.
        openAPI.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            ApiResponses apiResponses = operation.getResponses();
            apiResponses.addApiResponse("500", createApiResponse("Internal Server Error.", internalServerErrorResponseSchemaRef));
        }));
    }

    private ApiResponse createApiResponse(String description, Schema<?> internalServerErrorResponseSchemaRef) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(internalServerErrorResponseSchemaRef);
        return new ApiResponse().description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType));
    }

    /*
     * Sorts the schema so that each item will appear alphabetically.
     */
    private void sortSchemas(OpenAPI openAPI) {
        // There is currently no property you can set to affect the sort of order of the
        // schemas which appear on the Swagger UI. So we manually sort everything alphabetically.

        // Grab the current schemas... this is by default an unordered Map (hence the randomness of the order).
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();

        // Replace the schemas with a TreeMap version (a TreeMap will order each entry using the natural sort
        // order of the entry keys - which in our case will be alphabetical since the keys are Strings).
        openAPI.getComponents().setSchemas(new TreeMap<>(schemas));
    }


    /*
     * Customise the sort order of the endpoints as they appear on the UI.
     *
     * This is really just to have them appear in our custom order on the UI for convenience:
     * - Index 0: "Example Demonstrations"
     * - Index 1: "Accounts"
     * - Index 2: "Payments"
     */
    void sortTags(OpenAPI openAPI) {
        List<Tag> tagList = openAPI.getTags();
        Tag demonstrationsTag = retrieveTagOrThrow(tagList, Demonstrations.OPEN_API_TAG_NAME);
        Tag accountsTag = retrieveTagOrThrow(tagList, Accounts.OPEN_API_TAG_NAME);
        Tag paymentsTag = retrieveTagOrThrow(tagList, Payments.OPEN_API_TAG_NAME);
        List<Tag> orderedList = new ArrayList<>();
        orderedList.add(demonstrationsTag);
        orderedList.add(accountsTag);
        orderedList.add(paymentsTag);
        openAPI.setTags(orderedList);
    }

    private Tag retrieveTagOrThrow(List<Tag> tags, String tagName) {
        return tags.stream()
                .filter(tag -> tag.getName().equals(tagName))
                .collect(MoreCollectors.onlyElement());
    }

}
