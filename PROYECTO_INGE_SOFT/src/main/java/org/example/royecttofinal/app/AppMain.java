package org.example.royecttofinal.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.royecttofinal.Controller.IPedidoController;
import org.example.royecttofinal.Controller.SolicitarPedidoController;

import java.io.IOException;

public class AppMain extends Application {

    private static Stage primaryStage;
    private static final IPedidoController SHARED_CONTROLLER = new SolicitarPedidoController();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("RestoSoftware — Gestión Integral de Pedidos (MVP)");
        mostrarVistaSolicitarPedido();
        primaryStage.show();
    }

    public static void mostrarVistaSolicitarPedido() {
        cargarEscena("/org/example/royecttofinal/SolicitarPedidoView.fxml");
    }

    public static void mostrarVistaCrudProductos() {
        cargarEscena("/org/example/royecttofinal/CrudProductosView.fxml");
    }

    public static void mostrarVistaProcesarPedido() {
        cargarEscena("/org/example/royecttofinal/ProcesarPedidoView.fxml");
    }

    private static void cargarEscena(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(rutaFxml));
            Scene scene = new Scene(loader.load(), 1200, 760);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando vista FXML: " + rutaFxml, e);
        }
    }

    public static IPedidoController getSharedController() {
        return SHARED_CONTROLLER;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
