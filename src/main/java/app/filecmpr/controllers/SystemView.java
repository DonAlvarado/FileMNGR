package app.filecmpr.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SystemView {

    @FXML
    private WebView svgViewer;

    @FXML
    private void initialize() {
        try {
            WebEngine engine = svgViewer.getEngine();
            String url = getClass().getResource("/app/filecmpr/images/DiagramaFileMNGR.svg").toExternalForm();
            engine.load(url);

            // Esperar a que cargue el documento y escalar el SVG sin tocar body
            engine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    engine.executeScript("""
                        (function() {
                            const svg = document.querySelector('svg');
                            if (svg) {
                                svg.removeAttribute('width');
                                svg.removeAttribute('height');
                                svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
                                svg.setAttribute('width', '100%');
                                svg.setAttribute('height', '100%');
                                // Forzar que no aparezcan barras
                                document.documentElement.style.overflow = 'hidden';
                            }
                        })();
                    """);
                }
            });

        } catch (Exception e) {
            System.err.println("[SystemView] Error al cargar el diagrama SVG: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
