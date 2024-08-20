package qupath.rabbitmq.commands;

import com.google.inject.Guice;
import com.google.inject.Injector;
import qupath.lib.gui.QuPathGUI;
import qupath.rabbitmq.RabbitMQConsumer;
import qupath.rabbitmq.RabbitMQProducer;
import qupath.rabbitmq.module.RabbitMqExtensionModule;

import javax.swing.*;

public class RabbitMqCommand implements Runnable {

    private final QuPathGUI qupath;
    private final Injector injector;

    public RabbitMqCommand(QuPathGUI qupath, Injector injector) {

        this.qupath = qupath;
        this.injector = injector;
    }

    @Override
    public void run() {
        //        JOptionPane.showMessageDialog(null, "QuPath installExtension entered", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
        RabbitMQProducer rabbitMQProducer = null;
        RabbitMQConsumer rabbitMQConsumer = null;

        try {
            rabbitMQConsumer = injector.getInstance(RabbitMQConsumer.class);
            rabbitMQConsumer.ProcessMessages(qupath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension failed to initialize RabbitMQConsumer. Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        try {
            rabbitMQProducer = injector.getInstance(RabbitMQProducer.class);
            rabbitMQProducer.SendReadinessSignal();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension failed to initialize RabbitMQProducer. Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
//        JOptionPane.showMessageDialog(null, "QuPath installExtension exited", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
    }

    protected Injector createInjector() {
        try {
//            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension pre-Injector creation", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
            var inj = Guice.createInjector(new RabbitMqExtensionModule());
//            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension Injector created", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
            return inj;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension failed to create Guice injector. Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
}
