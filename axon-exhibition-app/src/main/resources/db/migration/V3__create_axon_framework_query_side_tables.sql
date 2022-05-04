CREATE SCHEMA IF NOT EXISTS "query_side";


/*
 * Contains details about the state of each "token".
 *
 * A token is used by an application event processor to keep track of the events it has processed and still
 * needs to process.
 *
 * N.B. both the command side of the application (through Sagas) and query side of the application (through projections)
 * will use this table to persist tokens for their respective event processors.
 *
 * See https://apidocs.axoniq.io/4.4/org/axonframework/eventhandling/TrackingToken.html for the different
 * types of token (under the "All Known Implementing Classes" section).
 */
CREATE TABLE IF NOT EXISTS "query_side"."tokenentry"  (
    processorName VARCHAR(255) NOT NULL,
    segment       INTEGER NOT NULL,
    token         jsonb NULL,
    tokenType     VARCHAR(255) NULL,
    timestamp     VARCHAR(255) NULL,
    owner         VARCHAR(255) NULL,
    PRIMARY KEY (processorName,segment)
);