package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.rabbitmq.client.Channel;
import qupath.rabbitmq.ActionFactory;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.RabbitMQConsumer;

public class RabbitMQConsumerProvider implements Provider<RabbitMQConsumer> {
    private final ActionLogger logger;
    private final Channel channel;
    private final ActionFactory actionFactory;

    @Inject
    public RabbitMQConsumerProvider(ActionLogger logger, Channel channel, ActionFactory actionFactory) {
        this.logger = logger;
        this.channel = channel;
        this.actionFactory = actionFactory;
    }

    @Override
    public RabbitMQConsumer get() {
        try {
            return new RabbitMQConsumer(channel, actionFactory, logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}