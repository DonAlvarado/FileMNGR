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
        // Carga la escena del menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/filecmpr/fxml/MenuWindow.fxml"));
        Scene scene = new Scene(loader.load());

        // Cambia de escena a la de la app
        Stage stage = new Stage();
        stage.setTitle("FileCMPR - Menú");
        stage.setScene(scene);

        // Agrega el icono a la nueva escena
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/app/filecmpr/images/appIcon.png"))
        );

        stage.show();

        // Cierra la escena de bienvenidaa
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
