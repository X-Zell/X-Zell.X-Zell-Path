package qupath.rabbitmq.actions;

import java.io.IOException;

public interface IQuPathExtensionAction {
    void runAction() throws IOException;
}
