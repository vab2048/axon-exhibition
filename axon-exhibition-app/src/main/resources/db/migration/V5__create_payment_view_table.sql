CREATE TABLE IF NOT EXISTS "query_side"."payment_view"
(
    payment_id                   UUID PRIMARY KEY,
    source_account_id            UUID,
    destination_account_id       UUID,
    amount                       bigint,
    status                       TEXT,
    settlement_initiation_time   TIMESTAMP WITH TIME ZONE -- Will always be UTC.
);