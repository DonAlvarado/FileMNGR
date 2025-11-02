package app.filecmpr.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import app.filecmpr.compression.CompressionFactory;
import app.filecmpr.compression.Compressor;
import app.filecmpr.encryption.EncryptionFactory;
import app.filecmpr.encryption.Encryptor;
import app.filecmpr.filemngr.FolderPackager;
import app.filecmpr.utils.AppState;

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

    private File selectedFile = null;
    private File selectedFolder = null;

    @FXML
    public void initialize() {
        loadConfig();

        operationChoice.getItems().setAll(
                "Comprimir", "Comprimir y Encriptar", "Solo Encriptar",
                "Descomprimir", "Descomprimir y Desencriptar", "Desencriptar"
        );

        Set<String> algorithms = CompressionFactory.getAlgorithmNames();
        compressionChoice.getItems().setAll(algorithms.isEmpty() ? List.of("Vacio") : algorithms);

        operationChoice.setValue(props.getProperty("operation", "Comprimir"));
        compressionChoice.setValue(props.getProperty("algorithm",
                compressionChoice.getItems().isEmpty() ? "LZ77" : compressionChoice.getItems().get(0)));
        passwordField.setText(props.getProperty("password", ""));

        if ("carpeta".equalsIgnoreCase(props.getProperty("pathType", "archivo")))
            radioCarpeta.setSelected(true);
        else
            radioArchivo.setSelected(true);

        tipoGroup.selectedToggleProperty().addListener((obs, a, b) -> {
            selectedFile = null;
            selectedFolder = null;
            fileList.getItems().clear();
            saveConfig();
            updateStatus();
        });

        operationChoice.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            saveConfig();
            updateUI(b);
            updateStatus();
        });

        compressionChoice.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> saveConfig());
        passwordField.textProperty().addListener((o,a,b)-> saveConfig());

        updateUI(operationChoice.getValue());
        updateStatus();
    }

    private void updateUI(String mode) {
        boolean compression = mode != null && mode.contains("Comprimir");
        boolean encryption  = mode != null && mode.contains("Encriptar") || mode != null && mode.contains("Desencriptar");
        compressionChoice.setDisable(!compression);
        passwordField.setDisable(!encryption);
    }

    private void updateStatus() {
        String mode = operationChoice.getValue();
        String tipo = radioCarpeta.isSelected() ? "Carpeta" : "Archivo";
        statusLabel.setText((mode == null ? "Modo: Ninguno" : "Modo: " + mode) + " | Tipo: " + tipo);
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

        String mode = operationChoice.getValue();

        if (radioCarpeta.isSelected()) {
            if (mode != null && (mode.contains("Descomprimir") || mode.contains("Desencriptar"))) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Seleccionar archivo comprimido/encriptado");
                fc.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Archivos procesados", "*.cmp", "*.ec", "*.enc"),
                        new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
                );
                File file = fc.showOpenDialog(stage);
                if (file != null) {
                    selectedFolder = file;
                    fileList.getItems().add(file.getName());
                    statusLabel.setText("Archivo seleccionado: " + file.getAbsolutePath());
                }
            } else {
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Seleccionar carpeta");
                File folder = dirChooser.showDialog(stage);
                if (folder != null) {
                    selectedFolder = folder;
                    fileList.getItems().add("[Carpeta] " + folder.getName());
                    File[] txtFiles = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".txt"));
                    if (txtFiles != null && txtFiles.length > 0)
                        Arrays.stream(txtFiles).forEach(f -> fileList.getItems().add(f.getName()));
                    else
                        fileList.getItems().add("(Carpeta vacía o sin .txt)");
                    statusLabel.setText("Carpeta seleccionada: " + folder.getAbsolutePath());
                }
            }
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar archivo");
            if (mode != null && (mode.contains("Descomprimir") || mode.contains("Desencriptar"))) {
                fc.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Archivos procesados", "*.cmp", "*.ec", "*.enc")
                );
            } else {
                fc.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
                );
            }
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                selectedFile = f;
                fileList.getItems().add(f.getName());
                statusLabel.setText("Archivo seleccionado: " + f.getAbsolutePath());
            }
        }
    }

    @FXML
    private void handleProcess(ActionEvent event) {
        String operation = operationChoice.getValue();
        String algorithm = compressionChoice.getValue();
        String password = passwordField.getText();

        try {
            if (radioCarpeta.isSelected()) {
                if (selectedFolder == null) {
                    statusLabel.setText("Selecciona una carpeta primero.");
                    return;
                }
                processFolder(selectedFolder, operation, algorithm, password);
            } else {
                if (selectedFile == null) {
                    statusLabel.setText("Selecciona un archivo primero.");
                    return;
                }
                processFile(selectedFile, operation, algorithm, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error procesando: " + e.getMessage());
        }
    }

    private void processFile(File file, String operation, String algorithm, String password) throws Exception {
        Compressor comp = CompressionFactory.get(algorithm);
        Encryptor enc = EncryptionFactory.get("AES-256");

        byte[] input = Files.readAllBytes(file.toPath());
        byte[] result = null;
        String outName = file.getName();
        long start = System.currentTimeMillis();

        if (operation.contains("Comprimir y Encriptar")) {
            result = enc.encrypt(comp.compress(input), password);
            outName = outName.replaceFirst("\\.[^.]+$", "") + ".ec";
        } else if (operation.contains("Descomprimir y Desencriptar")) {
            result = comp.decompress(enc.decrypt(input, password));
            outName = outName.replaceFirst("\\.[^.]+$", "") + "_rec.txt";
        } else if (operation.contains("Solo Encriptar")) {
            result = enc.encrypt(input, password);
            outName = outName.replaceFirst("\\.[^.]+$", "") + ".enc";
        } else if (operation.equals("Desencriptar")) {
            result = enc.decrypt(input, password);
            outName = outName.replaceFirst("\\.[^.]+$", "") + "_dec.txt";
        } else if (operation.contains("Comprimir")) {
            result = comp.compress(input);
            outName = outName.replaceFirst("\\.[^.]+$", "") + ".cmp";
        } else if (operation.contains("Descomprimir")) {
            result = comp.decompress(input);
            outName = outName.replaceFirst("\\.[^.]+$", "") + "_dec.txt";
        }

        long time = System.currentTimeMillis() - start;

        File tmpDir = new File("app_data/tmp");
        if (!tmpDir.exists()) tmpDir.mkdirs();

        File tempOriginal = new File(tmpDir, file.getName());
        Files.copy(file.toPath(), tempOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File outFile = new File(tmpDir, outName);
        Files.write(outFile.toPath(), result);

        AppState.lastOriginal = tempOriginal;
        AppState.lastProcessed = outFile;
        AppState.lastTime = time;

        statusLabel.setText("Archivo procesado (" + operation + ") en " + time + " ms");
    }

    private void processFolder(File folder, String operation, String algorithm, String password) throws Exception {
        Compressor comp = CompressionFactory.get(algorithm);
        Encryptor enc = EncryptionFactory.get("AES-256");

        File tmpDir = new File("app_data/tmp");
        if (!tmpDir.exists()) tmpDir.mkdirs();

        long start = System.currentTimeMillis();
        byte[] data, result;
        File output;

        if (operation.contains("Comprimir y Encriptar")) {
            File merged = FolderPackager.mergeFolder(folder);
            data = Files.readAllBytes(merged.toPath());
            result = enc.encrypt(comp.compress(data), password);
            output = new File(tmpDir, folder.getName() + "_pack.ec");
            Files.write(output.toPath(), result);
        } else if (operation.contains("Descomprimir y Desencriptar")) {
            data = Files.readAllBytes(folder.toPath());
            result = comp.decompress(enc.decrypt(data, password));
            File decoded = new File(tmpDir, folder.getName().replace(".ec", "_decoded.txt"));
            Files.write(decoded.toPath(), result);
            FolderPackager.unmergeFolder(decoded);
            output = decoded;
        } else if (operation.contains("Comprimir")) {
            File merged = FolderPackager.mergeFolder(folder);
            data = Files.readAllBytes(merged.toPath());
            result = comp.compress(data);
            output = new File(tmpDir, folder.getName() + "_compressed.cmp");
            Files.write(output.toPath(), result);
        } else if (operation.contains("Descomprimir")) {
            data = Files.readAllBytes(folder.toPath());
            result = comp.decompress(data);
            File decoded = new File(tmpDir, folder.getName().replace(".cmp", "_decoded.txt"));
            Files.write(decoded.toPath(), result);
            FolderPackager.unmergeFolder(decoded);
            output = decoded;
        } else if (operation.equals("Desencriptar")) {
            data = Files.readAllBytes(folder.toPath());
            result = enc.decrypt(data, password);
            output = new File(tmpDir, folder.getName().replace(".enc", "_dec.txt"));
            Files.write(output.toPath(), result);
        } else {
            statusLabel.setText("Operación no implementada para carpetas: " + operation);
            return;
        }

        long time = System.currentTimeMillis() - start;
        AppState.lastTime = time;
        AppState.lastOriginal = folder;
        AppState.lastProcessed = output;

        statusLabel.setText("Carpeta procesada (" + operation + ") en " + time + " ms");
    }
}
