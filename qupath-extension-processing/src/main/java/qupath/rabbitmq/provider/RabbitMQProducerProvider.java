package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.RabbitMQProducer;

public class RabbitMQProducerProvider implements Provider<RabbitMQProducer> {
    private final ActionLogger logger;

    @Inject
    public RabbitMQProducerProvider(ActionLogger logger) {
        this.logger = logger;
    }

    @Override
    public RabbitMQProducer get() {
        try {
            return new RabbitMQProducer(logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}