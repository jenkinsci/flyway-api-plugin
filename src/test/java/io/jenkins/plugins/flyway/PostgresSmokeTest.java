package io.jenkins.plugins.flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class PostgresSmokeTest {

    public static final String TEST_IMAGE = "postgres:17.5";

    @Test
    public void smokeTest(JenkinsRule j) throws Exception {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(TEST_IMAGE)) {
            postgres.start();
            postgres.waitingFor(Wait.forListeningPort());
            validateFlywayMigrations(postgres);
            assertTableExists(postgres);
        }
    }

    private void validateFlywayMigrations(PostgreSQLContainer<?> postgres) {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:io/jenkins/plugins/flyway/migrations/postgresql")
                .load();
        flyway.migrate();
    }

    private void assertTableExists(PostgreSQLContainer<?> postgres) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM test LIMIT 1");
        }
    }
}
