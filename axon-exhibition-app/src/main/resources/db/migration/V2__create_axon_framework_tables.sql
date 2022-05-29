/*
 * Create tables and indexes required by the Axon Framework within a schema called "axon".
 * See: https://docs.axoniq.io/reference-guide/appendices/rdbms-tuning
 */

/* *****************************************************************************
 *                            DOMAIN EVENT ENTRY                               *
 * *****************************************************************************/

/*
 * Stores the application events.
 * Events are stored in the 'payload' column as JSON.
 */
CREATE TABLE IF NOT EXISTS "axon"."domainevententry"
(
    globalIndex            BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL,
    eventIdentifier        TEXT NOT NULL,
    aggregateIdentifier    TEXT NOT NULL,   -- Unique ID for the aggregate.
    sequenceNumber         BIGINT NOT NULL, -- Sequence number of the event for the specific aggregate.
    type                   TEXT NOT NULL,   -- Aggregate type.
    payloadType            TEXT NOT NULL,   -- Fully qualified type name of the actual event payload.
    payloadRevision        TEXT,
    payload                jsonb NOT NULL,  -- Serialized event payload of the given payload_type.
    timeStamp              TEXT NOT NULL,   -- Event timestamp.
    metaData               jsonb,           -- Any metadata (such as trace IDs) associated with the event.
    PRIMARY KEY (globalIndex),
    UNIQUE(aggregateIdentifier, sequenceNumber),
    UNIQUE(eventIdentifier)
);

/* *****************************************************************************
 *                          SNAPSHOT EVENT ENTRY                               *
 * *****************************************************************************/

/*
 * Stores the snapshots of the aggregates in the application.
 * The snapshot is in the 'payload' column as JSON.
 */
CREATE TABLE IF NOT EXISTS "axon"."snapshotevententry"
(
    sequenceNumber         BIGINT NOT NULL,
    aggregateIdentifier    TEXT NOT NULL,
    type                   TEXT NOT NULL,
    eventIdentifier        TEXT NOT NULL,
    metaData               jsonb,
    payload                jsonb NOT NULL,
    payloadRevision        TEXT,
    payloadType            TEXT NOT NULL,
    timeStamp              TEXT NOT NULL,
    PRIMARY KEY (aggregateIdentifier, sequenceNumber),
    UNIQUE (eventIdentifier)
);

CREATE INDEX IF NOT EXISTS snapshotevententry_aggregate_identifier_index
ON "axon"."snapshotevententry"(aggregateIdentifier);



/* *****************************************************************************
 *                                  SAGAS                                      *
 * *****************************************************************************/

/*
 * Storage of ongoing sagas in the application.
 * Unfortunately it is not possible to currently easily configure Axon to serialise sagas to JSON.
 * There is an open issue to add this to the framework: https://github.com/AxonFramework/AxonFramework/issues/2214
 */
CREATE TABLE IF NOT EXISTS "axon"."sagaentry"
(
    sagaId         TEXT NOT NULL,
    revision       TEXT,
    sagaType       TEXT,
    serializedSaga bytea,
    PRIMARY KEY (sagaId)
);

/*
 * Stores association value entries for Sagas.

 * An association value is a combination of a key and value by which a Saga can be found.
 * A single association value can lead to multiple Sagas, and a single Saga can be
 * associated with multiple Association Values.
 *
 * For example, a Saga managing Orders could have a AssociationValue with key "orderId" and the order identifier as value.
 */
CREATE TABLE IF NOT EXISTS "axon"."associationvalueentry"
(
    id               INT GENERATED BY DEFAULT AS IDENTITY (START WITH 1) NOT NULL,
    associationKey   TEXT,
    associationValue TEXT,
    sagaId           TEXT,
    sagaType         TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS associationvalueentry_index_association
    ON "axon"."associationvalueentry" (sagaType, associationKey, associationValue);

CREATE INDEX IF NOT EXISTS associationvalueentry_index_saga
    ON "axon"."associationvalueentry" (sagaId, sagaType);


/* *****************************************************************************
 *                              TOKEN ENTRY                                    *
 * *****************************************************************************/

/*
 * Contains details about the state of each "token".
 *
 * A token is used by an application event processor to keep track of the events it has processed and still
 * needs to process.
 *
 * Both the command side of the application (through Sagas) and query side of the application (through projections)
 * will use this table to persist tokens for their respective event processors.
 *
 * See https://apidocs.axoniq.io/4.4/org/axonframework/eventhandling/TrackingToken.html for the different
 * types of token (under the "All Known Implementing Classes" section).
 */
CREATE TABLE IF NOT EXISTS "axon"."tokenentry"  (
    processorName TEXT NOT NULL,
    segment       INTEGER NOT NULL,
    token         jsonb NULL,
    tokenType     TEXT NULL,
    timestamp     TEXT NULL,
    owner         TEXT NULL,
    PRIMARY KEY (processorName,segment)
);