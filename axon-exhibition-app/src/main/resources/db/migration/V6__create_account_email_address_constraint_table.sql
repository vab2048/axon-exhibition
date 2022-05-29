CREATE TABLE IF NOT EXISTS "command_side"."account_email_address_constraint" (
    account_id     UUID PRIMARY KEY,
    email_address  TEXT UNIQUE      -- The email address must be unique among all entries.
);