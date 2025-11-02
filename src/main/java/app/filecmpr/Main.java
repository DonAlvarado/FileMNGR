package app.filecmpr;

import app.filecmpr.controllers.Configure;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import app.filecmpr.utils.clearData;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/BootWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("FileCMPR");
        stage.setScene(scene);

        // Carga el icono de la app por que desde las escenas no se puede.
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/app/filecmpr/images/appIcon.png"))
        );
        stage.show();
    }

    public static void main(String[] args) {
        clearData.cleanTmpDir(); // Al iniciar la app limpia el directorio temporal.
        launch();
    }
}
