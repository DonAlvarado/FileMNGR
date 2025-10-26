package app.filecmpr.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.*;
import java.util.Properties;
import java.util.Objects;
import java.util.Optional;
import java.util.Arrays;

// imports omitidos

public class Configure {
    @FXML private RadioButton radioArchivo;
    @FXML private RadioButton radioCarpeta;
    @FXML private ToggleGroup tipoGroup;

    @FXML private ChoiceBox<String> operationChoice;
    @FXML private ChoiceBox<String> compressionChoice;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button btnUpload, btnProcess;
    @FXML private ListView<String> fileList;

    private final Properties props = new Properties();
    private static final String CONFIG_PATH = "config.properties";

    // Estado real seleccionado
    private File selectedFile = null;      // si el modo es Archivo
    private File selectedFolder = null;    // si el modo es Carpeta

    @FXML
    public void initialize() {
        loadConfig();

        operationChoice.getItems().setAll(
                "Comprimir", "Comprimir y Encriptar", "Solo Encriptar",
                "Descomprimir", "Descomprimir y Desencriptar"
        );
        compressionChoice.getItems().setAll("LZ77", "Huffman", "LZ + Huffman");

        operationChoice.setValue(props.getProperty("operation", "Comprimir"));
        compressionChoice.setValue(props.getProperty("algorithm", "LZ77"));
        passwordField.setText(props.getProperty("password", ""));

        // Restaurar tipo Archivo/Carpeta
        if ("carpeta".equalsIgnoreCase(props.getProperty("pathType", "archivo"))) {
            radioCarpeta.setSelected(true);
        } else {
            radioArchivo.setSelected(true);
        }

        // Listener para actualizar estado visible
        tipoGroup.selectedToggleProperty().addListener((obs, a, b) -> {
            selectedFile = null;
            selectedFolder = null;
            fileList.getItems().clear();
            saveConfig();
            updateStatus();
        });

        operationChoice.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{saveConfig(); updateStatus();});
        compressionChoice.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> saveConfig());
        passwordField.textProperty().addListener((o,a,b)-> saveConfig());

        updateUI(operationChoice.getValue());
        updateStatus();
    }

    private void updateUI(String mode) {
        boolean compression = mode != null && mode.contains("Comprimir");
        boolean encryption  = mode != null && mode.contains("Encriptar");
        compressionChoice.setDisable(!compression);
        passwordField.setDisable(!encryption);
    }

    private void updateStatus() {
        String mode = operationChoice.getValue();
        String tipo = radioCarpeta.isSelected() ? "Carpeta" : "Archivo";
        statusLabel.setText(
                (mode == null ? "Modo: Ninguno" : "Modo: " + mode) + " | Tipo: " + tipo
        );
    }

    private void saveConfig() {
        props.setProperty("operation", operationChoice.getValue());
        props.setProperty("algorithm", compressionChoice.getValue());
        props.setProperty("password", passwordField.getText());
        props.setProperty("pathType", radioCarpeta.isSelected() ? "carpeta" : "archivo");
        try (FileOutputStream out = new FileOutputStream(CONFIG_PATH)) {
            props.store(out, "Configuración");
        } catch (IOException ignored) {}
    }

    private void loadConfig() {
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
        } catch (IOException ignored) {}
    }

    @FXML
    private void handleUpload(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        fileList.getItems().clear();
        selectedFile = null;
        selectedFolder = null;

        if (radioCarpeta.isSelected()) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Seleccionar carpeta");
            File folder = dirChooser.showDialog(stage);
            if (folder != null) {
                selectedFolder = folder; // GUARDA LA RUTA REAL
                // Muestra solo nombres bonitos
                String[] names = Optional.ofNullable(folder.listFiles((d,n)->n.toLowerCase().endsWith(".txt")))
                        .orElse(new File[0])
                        .length == 0
                        ? new String[]{"(Carpeta sin .txt)"}
                        : Arrays.stream(Objects.requireNonNull(
                                folder.listFiles((d,n)->n.toLowerCase().endsWith(".txt"))))
                        .map(File::getName).toArray(String[]::new);

                fileList.getItems().add("[Carpeta] " + folder.getName());
                fileList.getItems().addAll(names);
                statusLabel.setText("Carpeta seleccionada: " + folder.getAbsolutePath());
                props.setProperty("pathType", "carpeta");
                saveConfig();
            } else {
                statusLabel.setText("No se seleccionó carpeta.");
            }
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar archivo");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                selectedFile = f; // GUARDA LA RUTA REAL
                fileList.getItems().add(f.getName());
                statusLabel.setText("Archivo seleccionado: " + f.getAbsolutePath());
                props.setProperty("pathType", "archivo");
                saveConfig();
            } else {
                statusLabel.setText("No se seleccionó archivo.");
            }
        }
    }

    @FXML
    private void handleProcess(ActionEvent event) {
        // Ahorita SOLO manejo de carpetas/archivos. Nada de compresión aún.
        if (radioCarpeta.isSelected()) {
            if (selectedFolder == null) {
                statusLabel.setText("Selecciona una carpeta primero.");
                return;
            }
            try {
                // Solo probamos el empaquetado por ahora
                File merged = app.filecmpr.filemngr.FolderPackager.mergeFolder(selectedFolder);
                statusLabel.setText("Empaquetada en: " + merged.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error empaquetando carpeta: " + e.getMessage());
            }
        } else {
            if (selectedFile == null) {
                statusLabel.setText("Selecciona un archivo primero.");
                return;
            }
            statusLabel.setText("Archivo listo: " + selectedFile.getAbsolutePath());
        }
    }
}
