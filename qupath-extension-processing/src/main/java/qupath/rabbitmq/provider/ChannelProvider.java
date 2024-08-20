package qupath.rabbitmq.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ChannelProvider implements Provider<Channel> {

    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private final Connection connection;

    @Inject
    public ChannelProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Channel get() {
        try {
            Channel channel = connection.createChannel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);
            channel.basicQos(1);
            return channel;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ channel", e);
        }
    }
}
