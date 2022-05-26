package io.github.vab2048.axon.exhibition.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.GetMapping;

@Tags(value = @Tag(name = "Example Demonstrations", description = "Hit an endpoint to demonstrate a specific feature."))
public interface Demonstrations {

    @GetMapping("demo/set-based-validation/consistency-at-aggregate-threshold")
    void setBasedValidation1();

    @GetMapping("demo/set-based-validation/consistency-at-multiple-aggregate-threshold")
    void setBasedValidation2();

    @GetMapping("/demo/trigger-account-snapshot")
    void triggerAccountSnapshot();

}
