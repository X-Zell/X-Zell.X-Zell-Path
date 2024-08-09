package qupath.reordered.image.viewer;


import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import qupath.lib.common.Version;
// import qupath.lib.gui.ActionTools;
// import qupath.lib.gui.ActionTools.ActionDescription;
// import qupath.lib.gui.ActionTools.ActionMenu;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

import javax.swing.JOptionPane;

/**
 * Extension as Hello World POC.
 */
public class HelloWorldExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI qupath) {

         // Get a reference to a menu, creating it if necessary
         Menu menu = qupath.getMenu("X-Zell2", true);

         // Create a new MenuItem, which shows a new script when selected
         MenuItem item = new MenuItem("Hello World");
         item.setOnAction(e -> {
         	JOptionPane.showMessageDialog(null, "Hello World", "X-Zell: Hello World", JOptionPane.INFORMATION_MESSAGE);
         });

         menu.getItems().add(item);

    }

    @Override
    public String getName() {
        return "Hello World extension";
    }

    @Override
    public String getDescription() {
        return "Acts as a Hello World POC";
    }

    @Override
    public Version getVersion() {
        return Version.parse("0.2.0");
    }

    @Override
    public Version getQuPathVersion() {
        return Version.parse("0.3.2");
    }

}

