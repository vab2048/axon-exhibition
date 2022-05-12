package io.github.vab2048.axon.exhibition.app.query.payment;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PaymentViewRepository extends CrudRepository<PaymentView, UUID> {
}
