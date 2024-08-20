package qupath.rabbitmq;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;
import qupath.rabbitmq.actions.IQuPathExtensionAction;
import qupath.rabbitmq.data.ActionMessage;
import qupath.lib.gui.QuPathGUI;

import javax.swing.*;

public class RabbitMQConsumer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private Channel channel;

    private ActionFactory actionFactory;
    private ActionLogger logger;

    private String processName = "RabbitMQ Consumer";

    @Inject
    public RabbitMQConsumer(Channel channel, ActionFactory actionFactory, ActionLogger logger) throws Exception {
        this.channel = channel;
        this.actionFactory = actionFactory;
        this.logger = logger;

        try {
//            factory = new ConnectionFactory();
//            factory.setHost("localhost");
//
//            connection = factory.newConnection();
//            channel = connection.createChannel();
//            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
//            channel.queuePurge(RPC_QUEUE_NAME);
//
//            channel.basicQos(1);
            logger.logInfo("RabbitMQ channel initialised", processName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMQ Extension - consumer failed to create connection: " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    public void ProcessMessages(QuPathGUI qupath) throws IOException {

//        actionFactory = new ActionFactory(qupath, logger);
        try {
            logger.logInfo("Commencing processing of RabbitMQ messages", processName);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                logger.logInfo("", "");
                logger.logInfo("RabbitMQ message received with Correlation Id: " + delivery.getProperties().getCorrelationId(), processName);

                ProcessMessageFromQueue(consumerTag, delivery, qupath);
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {}));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMQ Extension - failed to process messages: " + e.getMessage(), "X-Zell: RabbitMQ", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private void ProcessMessageFromQueue(String consumerTag, Delivery delivery, QuPathGUI qupath) throws IOException {
        String response = "";
        try {
            String jsonMessage = new String(delivery.getBody(), "UTF-8");
            logger.logInfo("Received message: " + jsonMessage + " [Consumer Tag:" + consumerTag + "]", processName);

            Gson gson = new GsonBuilder().create();
            ActionMessage actionMessage = gson.fromJson(jsonMessage, ActionMessage.class);
            logger.logInfo("Message deserialized\nActionId: " + actionMessage.ActionContext + "\nActionName: " + actionMessage.ActionType + "\nActionValue: " + actionMessage.ActionValue, processName);

            IQuPathExtensionAction action = actionFactory.createAction(actionMessage);
            action.runAction();

            logger.logInfo("Action completed", processName);
            response = "Action Completed";

        } catch (RuntimeException | UnsupportedEncodingException e) {
            logger.logError(e.toString(), processName);
            throw new RuntimeException(e);
        } finally {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            logger.logInfo("Received message acknowledged", processName);
            logger.logInfo("", "");
        }
    }

}
