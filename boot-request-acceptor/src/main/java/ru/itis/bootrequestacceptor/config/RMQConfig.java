package ru.itis.bootrequestacceptor.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class RMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        return connectionFactory;
    }

    @Bean
    public Connection connection() {
        try {
            return connectionFactory().newConnection("localhost");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }
}
