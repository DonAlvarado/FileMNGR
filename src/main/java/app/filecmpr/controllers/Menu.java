package app.filecmpr.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class Menu {

    @FXML
    private AnchorPane mainContent;

    private void loadContent(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlFile));
            mainContent.getChildren().setAll(node);
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Boton para la escena config
    @FXML
    private void showConfig() {
        loadContent("/app/filecmpr/fxml/ConfigMenu.fxml");
    }

    // Boton para la escena de view
    @FXML
    private void showView() {
        loadContent("/app/filecmpr/fxml/ViewFiles.fxml");
    }

    // Boton para la escena de Statistics
    @FXML
    private void showStats() {
        loadContent("/app/filecmpr/fxml/StatisticsView.fxml");
    }

    // Boton para cargar el diagrama del flujo del sistema
    @FXML
    private void showDiagram() {
        loadContent("/app/filecmpr/fxml/SystemDiagramView.fxml");
    }
}
