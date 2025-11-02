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
                "Descomprimir", "Descomprimir y Desencriptar"
        );

        // === Cargar algoritmos disponibles ===
        Set<String> algorithms = CompressionFactory.getAlgorithmNames();
        compressionChoice.getItems().setAll(algorithms.isEmpty() ? List.of("Vacio") : algorithms);

        // === Restaurar valores guardados ===
        operationChoice.setValue(props.getProperty("operation", "Comprimir"));
        compressionChoice.setValue(props.getProperty("algorithm",
                compressionChoice.getItems().isEmpty() ? "LZ77" : compressionChoice.getItems().get(0)));
        passwordField.setText(props.getProperty("password", ""));

        if ("carpeta".equalsIgnoreCase(props.getProperty("pathType", "archivo")))
            radioCarpeta.setSelected(true);
        else
            radioArchivo.setSelected(true);

        // === Listeners ===
        tipoGroup.selectedToggleProperty().addListener((obs, a, b) -> {
            selectedFile = null;
            selectedFolder = null;
            fileList.getItems().clear();
            saveConfig();
            updateStatus();
        });

        operationChoice.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            saveConfig();
            updateUI(b); // ← Asegura actualización inmediata del UI
            updateStatus();
        });

        compressionChoice.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> saveConfig());
        passwordField.textProperty().addListener((o,a,b)-> saveConfig());

        // === Inicializa correctamente el UI ===
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

        // === Caso: Carpeta seleccionada ===
        if (radioCarpeta.isSelected()) {
            // Si estamos en modo descomprimir, pedimos el archivo .cmp (la "carpeta comprimida")
            if (mode != null && mode.contains("Descomprimir")) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Seleccionar carpeta comprimida (.cmp)");
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Archivos comprimidos", "*.cmp")
                );
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
                );

                File cmpFile = fc.showOpenDialog(stage);
                if (cmpFile != null) {
                    selectedFolder = cmpFile; // tratamos el archivo .cmp como una "carpeta comprimida"
                    fileList.getItems().add(cmpFile.getName());
                    statusLabel.setText("Carpeta comprimida seleccionada: " + cmpFile.getAbsolutePath());
                } else {
                    statusLabel.setText("No se seleccionó ningún archivo comprimido.");
                }
            }
            // Si estamos en modo comprimir, pedimos una carpeta normal
            else {
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Seleccionar carpeta");
                File folder = dirChooser.showDialog(stage);
                if (folder != null) {
                    selectedFolder = folder;
                    fileList.getItems().add("[Carpeta] " + folder.getName());

                    File[] txtFiles = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".txt"));
                    if (txtFiles != null && txtFiles.length > 0) {
                        Arrays.stream(txtFiles).forEach(f -> fileList.getItems().add(f.getName()));
                    } else {
                        fileList.getItems().add("(Carpeta vacía o sin .txt)");
                    }

                    statusLabel.setText("Carpeta seleccionada: " + folder.getAbsolutePath());
                } else {
                    statusLabel.setText("No se seleccionó ninguna carpeta.");
                }
            }
        }

        // === Caso: Archivo individual ===
        else {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar archivo");

            // Cambiamos los filtros según el modo actual
            if (mode != null && mode.contains("Descomprimir")) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos comprimidos", "*.cmp"));
            } else {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            }
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));

            File f = fc.showOpenDialog(stage);
            if (f != null) {
                selectedFile = f;
                fileList.getItems().add(f.getName());
                statusLabel.setText("Archivo seleccionado: " + f.getAbsolutePath());
            } else {
                statusLabel.setText("No se seleccionó ningún archivo.");
            }
        }
    }

    @FXML
    private void handleProcess(ActionEvent event) {
        String operation = operationChoice.getValue();
        String algorithm = compressionChoice.getValue();

        try {
            if (radioCarpeta.isSelected()) {
                if (selectedFolder == null) {
                    statusLabel.setText("Selecciona una carpeta primero.");
                    return;
                }
                processFolder(selectedFolder, operation, algorithm);
            } else {
                if (selectedFile == null) {
                    statusLabel.setText("Selecciona un archivo primero.");
                    return;
                }
                processFile(selectedFile, operation, algorithm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error procesando: " + e.getMessage());
        }
    }

    private void processFile(File file, String operation, String algorithm) throws IOException {
        Compressor comp = CompressionFactory.get(algorithm);
        if (comp == null) {
            statusLabel.setText("Algoritmo no soportado: " + algorithm);
            return;
        }

        byte[] inputData = Files.readAllBytes(file.toPath());
        byte[] outputData;
        String outputExt = operation.contains("Descomprimir") ? "_dec.txt" : ".cmp";
        long start = System.currentTimeMillis();

        if (operation.contains("Comprimir")) {
            outputData = comp.compress(inputData);
        } else if (operation.contains("Descomprimir")) {
            outputData = comp.decompress(inputData);
        } else {
            statusLabel.setText("Operación no implementada: " + operation);
            return;
        }

        long time = System.currentTimeMillis() - start;

        File tmpDir = new File("app_data/tmp");
        if (!tmpDir.exists()) tmpDir.mkdirs();

        File tempOriginal = new File(tmpDir, file.getName());
        Files.copy(file.toPath(), tempOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File outFile = new File(tmpDir, file.getName().replaceFirst("\\.[^.]+$", "") + outputExt);
        Files.write(outFile.toPath(), outputData);

        AppState.lastOriginal = tempOriginal;
        AppState.lastProcessed = outFile;
        AppState.lastTime = time;

        statusLabel.setText("Archivo procesado con " + comp.getName() + " en " + time + " ms");
    }

    private void processFolder(File folder, String operation, String algorithm) throws IOException {
        Compressor comp = CompressionFactory.get(algorithm);
        if (comp == null) {
            statusLabel.setText("Algoritmo no soportado: " + algorithm);
            return;
        }

        File tmpDir = new File("app_data/tmp");
        if (!tmpDir.exists()) tmpDir.mkdirs();

        long start = System.currentTimeMillis();
        File result;

        if (operation.contains("Comprimir")) {
            File mergedFile = FolderPackager.mergeFolder(folder);
            byte[] inputData = Files.readAllBytes(mergedFile.toPath());
            byte[] compressed = comp.compress(inputData);
            File cmpFile = new File(tmpDir, folder.getName() + "_compressed.cmp");
            Files.write(cmpFile.toPath(), compressed);
            result = cmpFile;
            AppState.lastOriginal = mergedFile;
            AppState.lastProcessed = cmpFile;
        } else if (operation.contains("Descomprimir")) {
            byte[] inputData = Files.readAllBytes(folder.toPath()); // carpeta = archivo .cmp aquí
            byte[] decoded = comp.decompress(inputData);
            File decodedFile = new File(tmpDir, folder.getName().replace(".cmp", "_decoded.txt"));
            Files.write(decodedFile.toPath(), decoded);
            File restoredDir = FolderPackager.unmergeFolder(decodedFile);
            result = restoredDir;
            AppState.lastOriginal = folder;
            AppState.lastProcessed = restoredDir;
        } else {
            statusLabel.setText("Operación no implementada para carpetas: " + operation);
            return;
        }

        long total = System.currentTimeMillis() - start;
        AppState.lastTime = total;
        statusLabel.setText("Carpeta procesada con " + comp.getName() + " en " + total + " ms.");
    }
}
