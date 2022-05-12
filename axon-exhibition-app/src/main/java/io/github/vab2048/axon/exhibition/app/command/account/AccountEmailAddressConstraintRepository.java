package io.github.vab2048.axon.exhibition.app.command.account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountEmailAddressConstraintRepository extends CrudRepository<AccountEmailAddressConstraint, UUID> {



}
