package app.filecmpr.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import app.filecmpr.utils.AppState;

public class ViewFiles {

    @FXML private TableView<FileInfo> originalFilesTable;
    @FXML private TableColumn<FileInfo, String> nameColOriginal, sizeColOriginal, typeColOriginal, dateColOriginal;

    @FXML private TableView<FileInfo> processedFilesTable;
    @FXML private TableColumn<FileInfo, String> nameColProcessed, sizeColProcessed, typeColProcessed, dateColProcessed;

    private final ObservableList<FileInfo> originalFiles = FXCollections.observableArrayList();
    private final ObservableList<FileInfo> processedFiles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns(nameColOriginal, sizeColOriginal, typeColOriginal, dateColOriginal);
        setupColumns(nameColProcessed, sizeColProcessed, typeColProcessed, dateColProcessed);
        originalFilesTable.setItems(originalFiles);
        processedFilesTable.setItems(processedFiles);

        if (AppState.lastOriginal != null && AppState.lastProcessed != null) {
            loadFiles(AppState.lastOriginal, AppState.lastProcessed);
        }
    }

    private void setupColumns(TableColumn<FileInfo, String> name,
                              TableColumn<FileInfo, String> size,
                              TableColumn<FileInfo, String> type,
                              TableColumn<FileInfo, String> date) {
        name.setCellValueFactory(d -> d.getValue().nameProperty());
        size.setCellValueFactory(d -> d.getValue().sizeProperty());
        type.setCellValueFactory(d -> d.getValue().typeProperty());
        date.setCellValueFactory(d -> d.getValue().dateProperty());
    }

    private void loadFiles(File original, File processed) {
        originalFiles.clear();
        processedFiles.clear();
        if (original.exists()) originalFiles.add(new FileInfo(original));
        if (processed.exists()) processedFiles.add(new FileInfo(processed));
    }

    public static class FileInfo {
        private final javafx.beans.property.SimpleStringProperty name, size, type, date;

        public FileInfo(File file) {
            this.name = new javafx.beans.property.SimpleStringProperty(file.getName());
            this.size = new javafx.beans.property.SimpleStringProperty(String.format("%.2f KB", file.length() / 1024.0));
            this.type = new javafx.beans.property.SimpleStringProperty(getExt(file));
            this.date = new javafx.beans.property.SimpleStringProperty(
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(file.lastModified())));
        }

        private String getExt(File f) {
            String n = f.getName();
            int dot = n.lastIndexOf('.');
            return (dot == -1) ? "N/A" : n.substring(dot + 1);
        }

        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public javafx.beans.property.StringProperty sizeProperty() { return size; }
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.StringProperty dateProperty() { return date; }
    }
}
