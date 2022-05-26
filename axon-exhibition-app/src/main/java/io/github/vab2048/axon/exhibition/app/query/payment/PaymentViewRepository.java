package io.github.vab2048.axon.exhibition.app.query.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * TODO: Update to ListCrudRepository when available.
 *
 * See: https://spring.io/blog/2022/02/22/announcing-listcrudrepository-friends-for-spring-data-3-0
 */
@Repository
public interface PaymentViewRepository extends CrudRepository<PaymentView, UUID> {
}
