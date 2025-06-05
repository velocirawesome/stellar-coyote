package com.velocirawesome.stellarcoyote;
    import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Because we're using testcontainers for local development and integration tests,
 * we don't want the integration test postgres to overwrite the data imported in the local development postgres.
 */
@Slf4j
@Configuration
public class PostgresContainerConfiguration {
    @Bean
    @Profile("!test")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        log.info("Starting PostgreSQL container for local development");
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16").withReuse(true);
        container.start();
        return container;
    }

    @Bean
    @Profile("test")
    @ServiceConnection
    public PostgreSQLContainer<?> testPostgresContainer() {
        log.info("Starting PostgreSQL container for integration tests");
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16");
        container.start();
        return container;
    }
}

