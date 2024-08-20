package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionProvider implements Provider<Connection> {
    private final ConnectionFactory connectionFactory;

    @Inject
    public ConnectionProvider(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Connection get() {
        try {
            return connectionFactory.newConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ connection", e);
        }
    }
}