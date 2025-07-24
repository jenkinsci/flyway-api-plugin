package io.jenkins.plugins.flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class MySqlDbSmokeTest {

    public static final String TEST_IMAGE = "mysql:9.4.0";

    @Test
    public void smokeTest(JenkinsRule j) throws Exception {
        try (MySQLContainer<?> mysql = new MySQLContainer<>(TEST_IMAGE)) {
            mysql.start();
            mysql.waitingFor(Wait.forListeningPort());
            validateFlywayMigrations(mysql);
            assertTableExists(mysql);
        }
    }

    private void validateFlywayMigrations(MySQLContainer<?> mysql) {
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:io/jenkins/plugins/flyway/migrations/mysql")
                .load();
        flyway.migrate();
    }

    private void assertTableExists(MySQLContainer<?> mysql) throws Exception {
        try (Connection connection =
                        DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM test LIMIT 1");
        }
    }
}
