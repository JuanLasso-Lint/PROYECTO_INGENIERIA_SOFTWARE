package org.example.royecttofinal.ViewController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.royecttofinal.Controller.IPedidoController;
import org.example.royecttofinal.Model.Producto;
import org.example.royecttofinal.app.AppMain;

public class CrudProductosViewController {

    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, String> colEstadoKDS;
    @FXML private Label lblContadorProductos;

    @FXML private TextField txtIdProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtPrecioBase;
    @FXML private CheckBox chkDisponibleKDS;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensajeCrud;

    private IPedidoController controller;
    private FilteredList<Producto> listaFiltrada;
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        this.controller = AppMain.getSharedController();

        cmbCategoria.getItems().setAll("Fuertes", "Pizzas", "Carnes", "Pastas", "Entradas", "Ensaladas", "Bebidas", "Postres");
        cmbCategoria.getSelectionModel().selectFirst();

        colId.setCellValueFactory(data -> data.getValue().idProductoProperty());
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colCategoria.setCellValueFactory(data -> data.getValue().categoriaProperty());
        colPrecio.setCellValueFactory(data -> data.getValue().precioBaseProperty());
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item.doubleValue()));
            }
        });
        colEstadoKDS.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isDisponibleKDS() ? "Disponible" : "Agotado KDS"
        ));

        listaFiltrada = new FilteredList<>(controller.getCatalogoMenu().getTodosLosProductos(), p -> true);
        txtBuscarProducto.textProperty().addListener((obs, o, texto) -> {
            String filtro = texto == null ? "" : texto.trim().toLowerCase();
            listaFiltrada.setPredicate(p -> filtro.isEmpty()
                    || p.getIdProducto().toLowerCase().contains(filtro)
                    || p.getNombre().toLowerCase().contains(filtro)
                    || p.getCategoria().toLowerCase().contains(filtro));
            actualizarContador();
        });

        tblProductos.setItems(listaFiltrada);
        tblProductos.setPlaceholder(new Label("No hay productos que coincidan con el criterio de búsqueda."));

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            productoSeleccionado = sel;
            if (sel != null) {
                txtIdProducto.setText(sel.getIdProducto());
                txtIdProducto.setDisable(true);
                txtNombreProducto.setText(sel.getNombre());
                cmbCategoria.setValue(sel.getCategoria());
                txtPrecioBase.setText(String.valueOf(sel.getPrecioBase()));
                chkDisponibleKDS.setSelected(sel.isDisponibleKDS());
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            }
        });

        actualizarContador();
    }

    @FXML
    private void onCrearClick() {
        try {
            double precio = Double.parseDouble(txtPrecioBase.getText().trim().replace(",", "."));
            controller.crearProductoCatalogo(
                    txtIdProducto.getText(),
                    txtNombreProducto.getText(),
                    cmbCategoria.getValue(),
                    precio,
                    chkDisponibleKDS.isSelected()
            );
            mostrarMensaje("Producto creado y sincronizado con el menú activo.", "#16A34A");
            onLimpiarClick();
            actualizarContador();
        } catch (NumberFormatException ex) {
            mostrarMensaje("Error: Ingrese un valor numérico válido para el precio.", "#DC2626");
        } catch (IllegalArgumentException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), "#DC2626");
        }
    }

    @FXML
    private void onActualizarClick() {
        if (productoSeleccionado == null) return;
        try {
            double precio = Double.parseDouble(txtPrecioBase.getText().trim().replace(",", "."));
            controller.actualizarProductoCatalogo(
                    productoSeleccionado,
                    txtNombreProducto.getText(),
                    cmbCategoria.getValue(),
                    precio,
                    chkDisponibleKDS.isSelected()
            );
            tblProductos.refresh();
            mostrarMensaje("Producto actualizado correctamente (estado KDS sincronizado).", "#2563EB");
            onLimpiarClick();
        } catch (NumberFormatException ex) {
            mostrarMensaje("Error: Ingrese un precio numérico válido.", "#DC2626");
        } catch (IllegalArgumentException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), "#DC2626");
        }
    }

    @FXML
    private void onEliminarClick() {
        if (productoSeleccionado == null) return;
        controller.eliminarProductoCatalogo(productoSeleccionado);
        mostrarMensaje("Producto eliminado del catálogo.", "#D97706");
        onLimpiarClick();
        actualizarContador();
    }

    @FXML
    private void onLimpiarClick() {
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
        txtIdProducto.clear();
        txtIdProducto.setDisable(false);
        txtNombreProducto.clear();
        cmbCategoria.getSelectionModel().selectFirst();
        txtPrecioBase.clear();
        chkDisponibleKDS.setSelected(true);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void onVolverAPedidosClick() {
        AppMain.mostrarVistaSolicitarPedido();
    }

    private void actualizarContador() {
        lblContadorProductos.setText("Productos visibles: " + listaFiltrada.size()
                + " de " + controller.getCatalogoMenu().getTodosLosProductos().size() + " totales.");
    }

    private void mostrarMensaje(String msg, String colorHex) {
        lblMensajeCrud.setText(msg);
        lblMensajeCrud.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
    }
}