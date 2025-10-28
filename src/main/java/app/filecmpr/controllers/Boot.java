package app.filecmpr.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.image.Image;
import java.io.IOException;

public class Boot {

    @FXML
    private void handleContinue(ActionEvent event) throws IOException {
        // Cargar el FXML del menú
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/filecmpr/fxml/MenuWindow.fxml"));
        Scene scene = new Scene(loader.load());

        // Crear nueva ventana
        Stage stage = new Stage();
        stage.setTitle("FileCMPR - Menú");
        stage.setScene(scene);

        // Agregar el ícono de la aplicación
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/app/filecmpr/images/appIcon.png"))
        );

        stage.show();

        // Cerrar la ventana actual (Boot)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
