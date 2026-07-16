package com.berruhanedar.app.gym_springboot.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("gymDatabase")
public class DatabaseConnectionHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseConnectionHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (!valid) {
                return Health.down()
                        .withDetail("database", "Connection validation failed")
                        .build();
            }

            return Health.up()
                    .withDetail("database", connection.getMetaData().getDatabaseProductName())
                    .withDetail("validationTimeoutSeconds", 2)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("database", "Connection could not be established")
                    .build();
        }
    }
}
