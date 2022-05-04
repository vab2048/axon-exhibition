The first SQL script we have begins with V2 instead of V1 (which is left blank).
This is because the first entry in the flyway_schema_history will be overwritten
if we ever run a flyway "baseline".

To avoid having the name of our V1 script being overwritten with "baseline" in the schema history
table (if we ever run a baseline) we start at V2.