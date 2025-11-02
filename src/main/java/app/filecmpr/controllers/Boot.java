package app.filecmpr.controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Boot implements Initializable {

    @FXML
    private Label bootTitle;

    @FXML
    private Label bootSubtitle;

    @FXML
    private void handleContinue(ActionEvent event) throws IOException {
        // Carga la escena del menú principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/filecmpr/fxml/MenuWindow.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = new Stage();
        stage.setTitle("FileCMPR - Menú");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/app/filecmpr/images/appIcon.png")));
        stage.show();

        // Cierra la escena de bienvenida
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Animación de entrada de los textos
        FadeTransition fadeTitle = new FadeTransition(Duration.seconds(1.2), bootTitle);
        fadeTitle.setFromValue(0.0);
        fadeTitle.setToValue(1.0);
        fadeTitle.play();

        FadeTransition fadeSub = new FadeTransition(Duration.seconds(1.6), bootSubtitle);
        fadeSub.setFromValue(0.0);
        fadeSub.setToValue(1.0);
        fadeSub.setDelay(Duration.seconds(0.5));
        fadeSub.play();
    }
}
