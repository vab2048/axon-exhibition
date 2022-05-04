package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.MakePaymentRequestBody;
import io.github.vab2048.axon.exhibition.app.query.PaymentView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tags(value = @Tag(name = "Payments", description = "Make a payment between two bank accounts."))
public interface Payment {

    @PostMapping("/payments")
    UUID makePayment(@RequestBody MakePaymentRequestBody requestBody);


    @GetMapping("/payments/{id}")
    PaymentView getPaymentView(@PathVariable UUID id);

}