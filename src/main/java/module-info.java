module app.filecmpr {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens app.filecmpr to javafx.fxml;
    exports app.filecmpr;
    exports app.filecmpr.controllers;
    opens app.filecmpr.controllers to javafx.fxml;
}