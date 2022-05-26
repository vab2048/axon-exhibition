CREATE TABLE IF NOT EXISTS "query_side"."account_view"
(
    account_id    UUID PRIMARY KEY,
    email_address TEXT,
    balance       bigint
);