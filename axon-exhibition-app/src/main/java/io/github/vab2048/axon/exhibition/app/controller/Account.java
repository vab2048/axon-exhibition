package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.query.AccountView;
import io.github.vab2048.axon.exhibition.message_api.controller.AccountControllerDTOs.CreateAccountResponseBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Tags(value = @Tag(name = "Accounts", description = "Manage our faux 'bank' accounts."))
public interface Account {

    @PostMapping("/accounts")
    public ResponseEntity<CreateAccountResponseBody> createNewAccount();

    @GetMapping("/accounts/{id}")
    AccountView getAccount(@PathVariable UUID id);

}
