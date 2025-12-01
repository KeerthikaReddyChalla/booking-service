package com.flightapp.bookingservice.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@Configuration
public class R2dbcConfig {

    @Bean
    public io.r2dbc.spi.ConnectionFactory connectionFactory() {
        return MySqlConnectionFactory.from(
                MySqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(3306)
                        .username("root")
                        .password("root")
                        .database("bookingdb")
                        .build()
        );
    }

    @Bean(name = "r2dbcEntityTemplate")
    public R2dbcEntityTemplate template(io.r2dbc.spi.ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
