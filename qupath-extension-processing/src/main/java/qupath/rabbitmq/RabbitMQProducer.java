package qupath.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducer {
    private static final String QUEUE_NAME = "signal_queue";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private ActionLogger logger;

    private String processName = "RabbitMQ Producer";

    public RabbitMQProducer(ActionLogger logger) throws IOException, TimeoutException {
        this.logger = logger;

        try {
            factory = new ConnectionFactory();
            factory.setHost("localhost");

            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queuePurge(QUEUE_NAME);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMQ Extension - producer failed to create connection: " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    public void SendReadinessSignal() throws IOException, TimeoutException {
        try {
            String message = "QuPath Ready";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (RuntimeException | UnsupportedEncodingException e) {
            logger.logError(e.toString(), processName);
            throw new RuntimeException(e);
        } finally {
            channel.close();
            connection.close();
            logger.logInfo("Channel closed", processName);
            logger.logInfo("", "");
        }
    }
}
