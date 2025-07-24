package io.jenkins.plugins.flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class MariaDbSmokeTest {

    public static final String TEST_IMAGE = "mariadb:11.8.2";

    @Test
    public void smokeTest(JenkinsRule j) throws Exception {
        try (MariaDBContainer<?> mysql = new MariaDBContainer<>(TEST_IMAGE)) {
            mysql.start();
            mysql.waitingFor(Wait.forListeningPort());
            validateFlywayMigrations(mysql);
            assertTableExists(mysql);
        }
    }

    private void validateFlywayMigrations(MariaDBContainer<?> mysql) {
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:io/jenkins/plugins/flyway/migrations/mariadb")
                .load();
        flyway.migrate();
    }

    private void assertTableExists(MariaDBContainer<?> mysql) throws Exception {
        try (Connection connection =
                        DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM test LIMIT 1");
        }
    }
}
