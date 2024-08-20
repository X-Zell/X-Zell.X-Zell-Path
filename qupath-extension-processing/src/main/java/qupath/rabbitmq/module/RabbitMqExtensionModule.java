package qupath.rabbitmq.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import qupath.rabbitmq.ActionFactory;
import qupath.rabbitmq.ActionLogger;
import qupath.rabbitmq.RabbitMQConsumer;
import qupath.rabbitmq.RabbitMQProducer;
import qupath.rabbitmq.provider.*;
import qupath.lib.gui.QuPathGUI;

public class RabbitMqExtensionModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(ConnectionFactory.class).toInstance(createConnectionFactory());
        bind(Connection.class).toProvider(ConnectionProvider.class);
        bind(Channel.class).toProvider(ChannelProvider.class);
        bind(ActionLogger.class).toInstance(new ActionLogger("1.0")); // Example version
        bind(ActionFactory.class).toProvider(ActionFactoryProvider.class);
        bind(RabbitMQConsumer.class).toProvider(RabbitMQConsumerProvider.class);
        bind(RabbitMQProducer.class).toProvider(RabbitMQProducerProvider.class);
        bind(QuPathGUI.class).toProvider(QuPathGUIProvider.class);
        bind(AdjustBrightnessesActionProvider.class).in(Singleton.class);
    }

    private ConnectionFactory createConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        return factory;
    }
}
