package app.filecmpr.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewFiles {

    @FXML private TableView<FileInfo> fileTable;
    @FXML private TableColumn<FileInfo, String> nameColumn;
    @FXML private TableColumn<FileInfo, String> sizeColumn;
    @FXML private TableColumn<FileInfo, String> typeColumn;
    @FXML private TableColumn<FileInfo, String> dateColumn;

    private final ObservableList<FileInfo> fileList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        sizeColumn.setCellValueFactory(data -> data.getValue().sizeProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());

        fileTable.setItems(fileList);

        loadFilesFromDirectory(new File("data/")); // Cambia "data/" por donde guardes tus archivos
    }

    private void loadFilesFromDirectory(File folder) {
        fileList.clear();
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) fileList.add(new FileInfo(f));
            }
        }
    }

    // Clase auxiliar interna
    public static class FileInfo {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty size;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty date;

        public FileInfo(File file) {
            this.name = new javafx.beans.property.SimpleStringProperty(file.getName());
            this.size = new javafx.beans.property.SimpleStringProperty(String.format("%.2f", file.length() / 1024.0));
            this.type = new javafx.beans.property.SimpleStringProperty(getFileExtension(file));
            this.date = new javafx.beans.property.SimpleStringProperty(
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(file.lastModified()))
            );
        }

        private String getFileExtension(File f) {
            String name = f.getName();
            int lastDot = name.lastIndexOf('.');
            return (lastDot == -1) ? "N/A" : name.substring(lastDot + 1);
        }

        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public javafx.beans.property.StringProperty sizeProperty() { return size; }
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.StringProperty dateProperty() { return date; }
    }
}
