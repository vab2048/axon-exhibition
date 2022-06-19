# ########################################################################################
#                         Postgres DB Docker Image Creator Script                        #
# ########################################################################################
#
# This Dockerfile creates a custom PostgreSQL docker image which is intended to be used for integration tests.
#
# When a container from this image is started:
# (i) a DB is created with the name of the value of the POSTGRES_DB environment variable.
# (ii) the SQL script placed in the /docker-entrypoint-initdb.d/ directory will
#      be executed and the DB will then have the application's latest schema in place.
#
# The SQL script we place in the /docker-entrypoint-initdb.d/ directory is a single file generated at build time
# through the concatenation of all of the flyway migration scripts (in the right order) in the
# src/main/resources/db/migration directory i.e. a concatenation of script V1__, V2__, etc.
#
# See:
# - Q&A: How to create custom docker image.
#   - https://stackoverflow.com/a/34753186/5108875
#   - https://stackoverflow.com/a/47512747/5108875 (if creation of schema on startup takes too long)
# - Official Postgres docker images and tags
#   - https://hub.docker.com/_/postgres/

# Begin from the correct version.
FROM postgres:14.3-alpine

# Will instruct the container to create a DB of the given name when first run.
ENV POSTGRES_DB axon_exhibition

# ##################################
#     Initialise the schema
# ##################################
# SQL (and sh) files in the /docker-entrypoint-initdb.d/ directory will be executed the first
# time the container is run. We want to create a single file containing all SQL that we want run
# (rather than execute multiple files). So we copy all SQL files into a temp directory with the intent
# to merge them into a single file.
COPY src/main/resources/db/migration /docker-entrypoint-initdb.d-temp/

# The execution order of the SQL files is defined as the sorted name order for the
# current locale (defaults=en_US.utf8). This is problematic because it will mean our
# scripts will be sorted wrong (10,11,12,...1,21,22,..2). So to avoid this we
# concatenate all of SQL files contained in the temp directory in the corect order
# and output it as one file in the initdb.d directory.
RUN \
    # Change directory to the temp directory we have created which contains all of our SQL scripts.
    cd /docker-entrypoint-initdb.d-temp && \
    # Concatenate each SQL file and output the file in the actual (not temp) init directory in a
    # script called init.sql.
    # ls options: -1: list each item on its own line, -v: natural sort of numbers (rather than the unix default),
    #             $PWD/* (prefix the filename with the directory (so we have an absolute reference)
    cat `ls -1v $PWD/* | grep '.sql'` > /docker-entrypoint-initdb.d/init.sql