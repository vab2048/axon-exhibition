package io.github.vab2048.axon.exhibition.app.config.testcontainers;

import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class AppDBContainer extends PostgreSQLContainer<AppDBContainer> {
    private static final Logger log = LoggerFactory.getLogger(AppDBContainer.class);

    // Container version definition.
    public static final String VERSION = "SNAPSHOT";
    public static final String CONTAINER_NAME = "vab2048/axon-exhibition-db";
    public static final String CONTAINER_IMAGE = CONTAINER_NAME + ":" + VERSION;

    // Credentials.
    public static final String POSTGRES_USER = "postgres";
    public static final String POSTGRES_PASSWORD = "password";
    public static final String POSTGRES_DB_NAME = "axon_exhibition";

    // DROP DB with FORCE (i.e. terminate all open connections before dropping).
    public static final String DROP_DB_WITH_FORCE_SQL = "DROP DATABASE " + POSTGRES_DB_NAME + " WITH (FORCE);";
    public static final String DROP_DB_SQL = "DROP DATABASE " + POSTGRES_DB_NAME + ";";
    public static final String CREATE_DB_SQL = "CREATE DATABASE " + POSTGRES_DB_NAME + ";";

    // Ports (from perspective of container) to expose. The host will expose the container's port on a random
    // free port (by design) to avoid port collisions. This means a mapping will need to take place at runtime.
    public static final int POSTGRES_PORT = 5432;

    // Network related fields
    public static final String POSTGRES_CONTAINER_NETWORK_ALIAS = "axon_exhibition_postgres_db";

    public AppDBContainer(Network network) {
        super(DockerImageName.parse(CONTAINER_IMAGE).asCompatibleSubstituteFor("postgres"));
        setNetwork(network);
        log.debug("""
                                        
                        ------------------------------------------------------------
                        Creating new testcontainer: {}
                        ------------------------------------------------------------""",
                CONTAINER_IMAGE);
    }

    /**
     * Our custom configuration for the container that will be called when the container is started.
     */
    @Override
    protected void configure() {
        // Set container fields.
        withExposedPorts(POSTGRES_PORT);
        withDatabaseName(POSTGRES_DB_NAME);
        withUsername(POSTGRES_USER);
        withPassword(POSTGRES_PASSWORD);

        // Set fields for the container's environment (overriding the defaults of 'test').
        addEnv("POSTGRES_DB", POSTGRES_DB_NAME);
        addEnv("POSTGRES_USER", POSTGRES_USER);
        addEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD);

        // Connect to the network requested with the given alias.
        withNetwork(getNetwork());
        withNetworkAliases(POSTGRES_CONTAINER_NETWORK_ALIAS);
    }


    public void registerContainer(DynamicPropertyRegistry registry) {
        log.info("Dynamically registering PostgresDb for application context: {}", getJdbcUrl());
        registry.add("spring.datasource.url", this::getJdbcUrl);
        registry.add("spring.datasource.username", this::getUsername);
        registry.add("spring.datasource.password", this::getPassword);
    }

    /**
     * {@return JDBC URL for other containers in the same docker network to use to connect to this container.}
     */
    public String getNetworkJdbcUrl() {
        return "jdbc:postgresql://%s:%s/%s".formatted(POSTGRES_CONTAINER_NETWORK_ALIAS, POSTGRES_PORT, POSTGRES_DB_NAME);
    }

    /**
     * {@return JDBC URL for connecting to the running container from the local host.}
     */
    public String getLocalhostJdbcUrl(String dbName) {
        var localhostMappedPort = getMappedPort(POSTGRES_PORT);
        return "jdbc:postgresql://localhost:%s/%s".formatted(localhostMappedPort, dbName);
    }


    // ---------------------------------------------------------------------------------------------
    // DB State
    // ---------------------------------------------------------------------------------------------


    /**
     * Reset the state of the DB so that the schema is fresh.
     *
     * We do this by:
     * - dropping all schemas in the DB (and as a result all the tables and other objects within them).
     * - reading in the `init.sql` file which is in the container into a String.
     * - running the init.sql string to recreate the schema.
     *
     * An alternative method would be to drop the DB completely and recreate it. We do not
     * do this to avoid the problems this would cause with the DB connection pool of
     * our application context.
     */
    public void resetDBState() {
        var localhostJdbcUrl = getLocalhostJdbcUrl(POSTGRES_DB_NAME);
        try(Connection connection = DriverManager.getConnection(localhostJdbcUrl, POSTGRES_USER, POSTGRES_PASSWORD)) {
            // Drop the schemas...
            var statement = connection.createStatement();
            statement.execute("DROP SCHEMA axon CASCADE;");
            statement.execute("DROP SCHEMA command_side CASCADE;");
            statement.execute("DROP SCHEMA query_side CASCADE;");

            // Recreate the DB schema by running the init.sql file contents.
            var initSQL = getInitSQL();
            statement.execute(initSQL);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Retrieve the init.sql file in the container as a String.
     */
    String getInitSQL() throws IOException {
        // Retrieve an input stream of the init.sql file.
        String initSQLPathOnContainer = "/docker-entrypoint-initdb.d/init.sql";
        CopyArchiveFromContainerCmd copyArchiveFromContainerCmd = dockerClient.copyArchiveFromContainerCmd(getContainerId(), initSQLPathOnContainer);
        InputStream initSQLInputStream = copyArchiveFromContainerCmd.exec();

        // Unfortunately it is not explicitly documented but the `CopyArchiveFromContainerCmd`
        // returns the resource as a tar file (`archive` is in the name) and not the file itself.
        // And so we need to 'unTar' the stream into a String. If we did not do this the String
        // we would read in would have some erroneous content in the first few bytes.
        // See: https://github.com/docker-java/docker-java/issues/991
        TarArchiveInputStream tarStream = new TarArchiveInputStream(initSQLInputStream);
        String sql;
        TarArchiveEntry tarEntry = tarStream.getNextTarEntry();
        if(tarEntry.isFile()) {
            sql = new String(tarStream.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalStateException("TarArchiveInputStream does not contain a file");
        }
        tarStream.close();
        return sql;
    }

}
