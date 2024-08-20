package qupath.rabbitmq;

import com.google.inject.Guice;
import com.google.inject.Injector;
import qupath.adjustbrightness.commands.AdjustBrightnessCommand;
import qupath.lib.gui.tools.MenuTools;
import qupath.rabbitmq.commands.RabbitMqCommand;
import qupath.rabbitmq.module.RabbitMqExtensionModule;
import qupath.lib.common.Version;

import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

import javax.swing.*;

/**
 * Extension for implementing a RabbitMq consumer.
 */
public class RabbitMqExtension implements QuPathExtension {

    private final Injector injector;

    public RabbitMqExtension() {
//        JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension entered", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
        this.injector = createInjector();
//        JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension Injector ready", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
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

    @Override
    public void installExtension(QuPathGUI qupath) {

        var actionWriter = ActionTools.createAction(new RabbitMqCommand(qupath, injector), "Initialise RabbitMq");
        actionWriter.setLongText("Initialise the Rabbit MQ related functionality");
//        actionWriter.disabledProperty().bind(qupath.imageDataProperty().isNull());
        MenuTools.addMenuItems(
                qupath.getMenu("X-Zell2", true),
                actionWriter);
    }
//    public void installExtension(QuPathGUI qupath) {
////        JOptionPane.showMessageDialog(null, "QuPath installExtension entered", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
//        RabbitMQProducer rabbitMQProducer = null;
//        RabbitMQConsumer rabbitMQConsumer = null;
//
//        try {
//            rabbitMQConsumer = injector.getInstance(RabbitMQConsumer.class);
//            rabbitMQConsumer.ProcessMessages(qupath);
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension failed to initialize RabbitMQConsumer. Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
//            throw new RuntimeException(e);
//        }
//
//        try {
//            rabbitMQProducer = injector.getInstance(RabbitMQProducer.class);
//            rabbitMQProducer.SendReadinessSignal();
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, "QuPath RabbitMqExtension failed to initialize RabbitMQProducer. Exception - " + e.getMessage(), "X-Zell: Error", JOptionPane.ERROR_MESSAGE);
//            throw new RuntimeException(e);
//        }
////        JOptionPane.showMessageDialog(null, "QuPath installExtension exited", "X-Zell: Info", JOptionPane.INFORMATION_MESSAGE);
//    }

    @Override
    public String getName() {
        return "RabbitMQ extension";
    }

    @Override
    public String getDescription() {
        return "Implements a RabbitMQ consumer that facilitates Hybrid Microscope-related functionality";
    }

    @Override
    public Version getVersion() {
        return Version.parse("1.22.1");
    }

    @Override
    public Version getQuPathVersion() {
        return QuPathExtension.super.getQuPathVersion();
    }

}

