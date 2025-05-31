package com.velocirawecome.stellarcoyote;
    import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.testcontainers.containers.PostgreSQLContainer;

    @Configuration
    public class PostgresContainerConfiguration {
        @Bean
        @ServiceConnection
        public PostgreSQLContainer<?> postgresContainer() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16");
            container.start();
            return container;
        }
    }

