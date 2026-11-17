package org.example.royecttofinal.ViewController;

import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.royecttofinal.Controller.IPedidoController;
import org.example.royecttofinal.Model.*;
import org.example.royecttofinal.app.AppMain;

import java.util.*;

public class SolicitarPedidoViewController {

    @FXML private Label lblInfoPedido;
    @FXML private Label lblPermisosRol;
    @FXML private ComboBox<RolActor> cmbRolActor;
    @FXML private Button btnCrudProductos;
    @FXML private Button btnProcesarPedidos;

    // Paso 1: Buscar o Crear Cliente y Cobertura
    @FXML private TextField txtBuscarCliente;
    @FXML private ComboBox<Cliente> cmbClientesDemo;
    @FXML private TextField txtDocumentoCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtTelefonoCliente;
    @FXML private TextField txtCalle;
    @FXML private TextField txtBarrio;
    @FXML private Label lblEstadoCobertura;

    // Paso 2 y 3: Menú, Modificadores y Alérgenos
    @FXML private VBox panelSeleccionProductos;
    @FXML private TextField txtBuscarPlato;
    @FXML private CheckBox chkMostrarAgotados;
    @FXML private ListView<Producto> listProductos;
    @FXML private Button btnSimularKDS;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private FlowPane paneModificadores;
    @FXML private FlowPane paneAlergenos;
    @FXML private ComboBox<RestriccionAlimentaria> cmbRestriccion;
    @FXML private TextField txtObservaciones;
    @FXML private Label lblErrorSeleccion;

    // Paso 4 y 5: Campos Automáticos, Carrito, Liquidación, Pago y Recibo PDF
    @FXML private Label lblFechaInicioAuto;
    @FXML private Label lblFechaCompromisoAuto;
    @FXML private ListView<DetallePedido> listCarrito;
    @FXML private TextField txtCodigoCupon;
    @FXML private Label lblErrorCupon;
    @FXML private Label lblSubtotal;
    @FXML private Label lblFlete;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private Button btnConfirmarPedido;
    @FXML private Button btnAbrirReciboPdf;

    private IPedidoController controller;
    private FilteredList<Cliente> filtroClientesUI;
    private FilteredList<Producto> filtroProductosUI;
    private final Map<CheckBox, Modificador> mapModificadoresUI = new LinkedHashMap<>();
    private final Map<CheckBox, Alergeno> mapAlergenosUI = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        this.controller = AppMain.getSharedController();

        cmbRolActor.getItems().setAll(RolActor.values());
        cmbRolActor.getSelectionModel().select(RolActor.CAJERO);
        actualizarPermisosPorRolUI();

        // Configurar búsqueda y selección de clientes registrados
        filtroClientesUI = new FilteredList<>(controller.getClientesRegistrados(), c -> true);
        cmbClientesDemo.setItems(filtroClientesUI);
        txtBuscarCliente.textProperty().addListener((obs, o, texto) -> {
            String q = texto == null ? "" : texto.trim().toLowerCase();
            filtroClientesUI.setPredicate(c -> q.isEmpty()
                    || c.getDocumento().toLowerCase().contains(q)
                    || c.getNombre().toLowerCase().contains(q)
                    || c.getTelefono().toLowerCase().contains(q)
                    || c.getDireccion().getBarrio().toLowerCase().contains(q));
            if (!filtroClientesUI.isEmpty()) {
                cmbClientesDemo.getSelectionModel().selectFirst();
            }
        });

        cmbClientesDemo.setOnAction(e -> {
            Cliente sel = cmbClientesDemo.getValue();
            if (sel != null) {
                cargarDatosClienteEnFormulario(sel);
            }
        });

        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));

        cmbRestriccion.getItems().setAll(RestriccionAlimentaria.values());
        cmbRestriccion.getSelectionModel().select(RestriccionAlimentaria.NINGUNA);

        for (Alergeno alergeno : Alergeno.values()) {
            if (alergeno != Alergeno.NINGUNO) {
                CheckBox chk = new CheckBox(alergeno.toString());
                mapAlergenosUI.put(chk, alergeno);
                paneAlergenos.getChildren().add(chk);
            }
        }

        cmbMetodoPago.getItems().setAll(
                "Pago en Línea (Tarjeta Crédito/Débito)",
                "Transferencia Rápida",
                "Pago Contra Entrega"
        );
        cmbMetodoPago.getSelectionModel().selectFirst();

        listProductos.setPlaceholder(new Label("No hay platos disponibles que coincidan con el filtro."));
        listCarrito.setPlaceholder(new Label("El carrito está vacío. Seleccione platos del menú izquierdo."));

        listCarrito.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DetallePedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String mods = item.getModificadores().isEmpty() ? "" : " + " + item.getModificadores().size() + " adic.";
                    String aler = item.getAlergenos().isEmpty() ? "" : " | Alérgenos: " + item.getAlergenos();
                    setText(String.format("%dx %s%s — $%.2f%s",
                            item.getCantidad(), item.getProducto().getNombre(), mods, item.getSubtotal(), aler));
                }
            }
        });

        if (!filtroClientesUI.isEmpty()) {
            cmbClientesDemo.getSelectionModel().selectFirst();
            cargarDatosClienteEnFormulario(cmbClientesDemo.getValue());
        }

        cargarCatalogoYModificadoresUI();
        enlazarDatosReactivosPedido();
        restaurarEstadoPedidoEnCurso();
    }

    private void cargarDatosClienteEnFormulario(Cliente c) {
        if (c == null) return;
        txtDocumentoCliente.setText(c.getDocumento());
        txtNombreCliente.setText(c.getNombre());
        txtTelefonoCliente.setText(c.getTelefono());
        txtCalle.setText(c.getDireccion().getCalle());
        txtBarrio.setText(c.getDireccion().getBarrio());
    }

    @FXML
    private void onCambiarRolClick() {
        actualizarPermisosPorRolUI();
        lblErrorCupon.setText("");
    }

    private void actualizarPermisosPorRolUI() {
        RolActor rol = cmbRolActor.getValue();
        if (rol == null) return;
        lblPermisosRol.setText("Permisos Activos (" + rol.getEtiqueta() + "): " + rol.getDescripcionPermisos());
        btnCrudProductos.setDisable(!rol.isPuedeGestionarCrud());
        if (btnProcesarPedidos != null) btnProcesarPedidos.setDisable(!rol.isPuedeGestionarCrud());
        btnSimularKDS.setDisable(!rol.isPuedeSimularKds());
    }

    @FXML
    private void onLimpiarClienteClick() {
        cmbClientesDemo.getSelectionModel().clearSelection();
        txtBuscarCliente.clear();
        txtDocumentoCliente.clear();
        txtNombreCliente.clear();
        txtTelefonoCliente.clear();
        txtCalle.clear();
        txtBarrio.clear();
        lblEstadoCobertura.setText("Ingrese los datos del nuevo cliente y presione '+ Guardar Nuevo Cliente'.");
        lblEstadoCobertura.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
        txtDocumentoCliente.requestFocus();
    }

    @FXML
    private void onCrearClienteClick() {
        try {
            Cliente nuevo = controller.crearYRegistrarCliente(
                    txtDocumentoCliente.getText(),
                    txtNombreCliente.getText(),
                    txtTelefonoCliente.getText(),
                    txtCalle.getText(),
                    txtBarrio.getText()
            );
            txtBuscarCliente.clear();
            cmbClientesDemo.getSelectionModel().select(nuevo);

            boolean cubierto = controller.validarCobertura(
                    nuevo.getDocumento(),
                    nuevo.getNombre(),
                    nuevo.getTelefono(),
                    nuevo.getDireccion().getCalle(),
                    nuevo.getDireccion().getBarrio(),
                    cmbRolActor.getValue()
            );

            if (cubierto) {
                lblEstadoCobertura.setText("Cliente '" + nuevo.getNombre() + "' (" + nuevo.getIdCliente()
                        + ") creado y guardado. Cobertura aprobada — Flete: $"
                        + String.format("%.2f", controller.getPedidoActual().getTarifaFlete()));
                lblEstadoCobertura.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                lblFechaCompromisoAuto.setText(controller.getPedidoActual().getFechaCompromisoFormateada());
                panelSeleccionProductos.setDisable(false);
            } else {
                lblEstadoCobertura.setText("Cliente '" + nuevo.getNombre() + "' guardado, pero su barrio ('"
                        + nuevo.getDireccion().getBarrio() + "') está fuera de cobertura (RN-02).");
                lblEstadoCobertura.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                lblFechaCompromisoAuto.setText("Sin cobertura geográfica");
                panelSeleccionProductos.setDisable(true);
            }
        } catch (IllegalArgumentException ex) {
            lblEstadoCobertura.setText("Error al crear cliente: " + ex.getMessage());
            lblEstadoCobertura.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        }
    }

    private void cargarCatalogoYModificadoresUI() {
        actualizarFuenteCatalogo();
        txtBuscarPlato.textProperty().addListener((obs, o, texto) -> aplicarFiltroTextoCatalogo(texto));

        paneModificadores.getChildren().clear();
        mapModificadoresUI.clear();
        for (Modificador mod : controller.getModificadoresDisponibles()) {
            CheckBox chk = new CheckBox(mod.toString());
            mapModificadoresUI.put(chk, mod);
            paneModificadores.getChildren().add(chk);
        }
    }

    private void actualizarFuenteCatalogo() {
        if (chkMostrarAgotados.isSelected()) {
            filtroProductosUI = new FilteredList<>(controller.getCatalogoMenu().getTodosLosProductos(), p -> true);
        } else {
            filtroProductosUI = new FilteredList<>(controller.getCatalogoMenu().getProductosDisponibles(), p -> true);
        }
        aplicarFiltroTextoCatalogo(txtBuscarPlato.getText());
        listProductos.setItems(filtroProductosUI);
    }

    private void aplicarFiltroTextoCatalogo(String texto) {
        if (filtroProductosUI == null) return;
        String q = texto == null ? "" : texto.trim().toLowerCase();
        filtroProductosUI.setPredicate(p -> q.isEmpty()
                || p.getNombre().toLowerCase().contains(q)
                || p.getCategoria().toLowerCase().contains(q)
                || p.getIdProducto().toLowerCase().contains(q));
    }

    private void enlazarDatosReactivosPedido() {
        Pedido pedido = controller.getPedidoActual();
        listCarrito.setItems(pedido.getItems());

        lblFechaInicioAuto.setText(pedido.getFechaCreacionFormateada());
        lblFechaCompromisoAuto.setText(pedido.getFechaCompromisoFormateada());

        lblInfoPedido.textProperty().bind(Bindings.createStringBinding(
                () -> "Orden: " + pedido.getIdPedido() + " | Estado: " + pedido.getEstado().getEtiqueta(),
                pedido.estadoProperty()
        ));

        lblSubtotal.textProperty().bind(Bindings.format("$%.2f", pedido.subtotalItemsProperty()));
        lblFlete.textProperty().bind(Bindings.format("$%.2f", pedido.tarifaFleteProperty()));
        lblDescuento.textProperty().bind(Bindings.format("-$%.2f", pedido.descuentoProperty()));
        lblTotal.textProperty().bind(Bindings.format("$%.2f", pedido.totalFinalProperty()));
    }

    private void restaurarEstadoPedidoEnCurso() {
        Pedido pedido = controller.getPedidoActual();
        if (pedido != null && pedido.getEstado() != EstadoPedido.PENDIENTE_COBERTURA && pedido.getCliente() != null) {
            cargarDatosClienteEnFormulario(pedido.getCliente());
            lblEstadoCobertura.setText("Cobertura aprobada. Flete asignado: $"
                    + String.format("%.2f", pedido.getTarifaFlete()));
            lblEstadoCobertura.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
            lblFechaCompromisoAuto.setText(pedido.getFechaCompromisoFormateada());
            panelSeleccionProductos.setDisable(pedido.getEstado() == EstadoPedido.ENVIADO_A_COCINA_KDS);
        }
        if (controller.getUltimoReciboPdf() != null && controller.getUltimoReciboPdf().exists()) {
            btnAbrirReciboPdf.setDisable(false);
        }
        if (pedido != null && pedido.getEstado() == EstadoPedido.ENVIADO_A_COCINA_KDS) {
            btnConfirmarPedido.setDisable(true);
        }
    }

    @FXML
    private void onValidarCoberturaClick() {
        boolean cubierto = controller.validarCobertura(
                txtDocumentoCliente.getText(),
                txtNombreCliente.getText(),
                txtTelefonoCliente.getText(),
                txtCalle.getText(),
                txtBarrio.getText(),
                cmbRolActor.getValue()
        );

        if (cubierto) {
            lblEstadoCobertura.setText("Cobertura aprobada. Flete asignado: $"
                    + String.format("%.2f", controller.getPedidoActual().getTarifaFlete()));
            lblEstadoCobertura.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
            lblFechaCompromisoAuto.setText(controller.getPedidoActual().getFechaCompromisoFormateada());
            panelSeleccionProductos.setDisable(false);
        } else {
            lblEstadoCobertura.setText("Error RN-02: Dirección fuera de zona de cobertura o datos incompletos.");
            lblEstadoCobertura.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblFechaCompromisoAuto.setText("Sin cobertura geográfica");
            panelSeleccionProductos.setDisable(true);
        }
    }

    @FXML
    private void onToggleMostrarAgotados() {
        actualizarFuenteCatalogo();
    }

    @FXML
    private void onSimularToggleKDS() {
        RolActor rol = cmbRolActor.getValue();
        if (rol != null && !rol.isPuedeSimularKds()) {
            lblErrorSeleccion.setText("Error RN-07: El rol '" + rol.getEtiqueta() + "' no tiene permisos para modificar disponibilidad KDS.");
            return;
        }
        Producto sel = listProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblErrorSeleccion.setText("Seleccione un plato de la lista para alternar su disponibilidad en KDS.");
            return;
        }
        lblErrorSeleccion.setText("");
        if (!sel.getIngredientesBase().isEmpty()) {
            Insumo insumo = sel.getIngredientesBase().get(0);
            insumo.setDisponibleKDS(!insumo.isDisponibleKDS());
        } else {
            sel.setHabilitadoEnCocina(!sel.isDisponibleKDS());
            controller.getCatalogoMenu().refrescarDisponibilidad();
        }
        listProductos.refresh();
    }

    @FXML
    private void onAgregarProductoClick() {
        lblErrorSeleccion.setText("");
        Producto seleccionado = listProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblErrorSeleccion.setText("Seleccione un plato del menú antes de agregar.");
            return;
        }

        List<Modificador> modsSeleccionados = new ArrayList<>();
        for (Map.Entry<CheckBox, Modificador> entry : mapModificadoresUI.entrySet()) {
            if (entry.getKey().isSelected()) {
                modsSeleccionados.add(entry.getValue());
            }
        }

        List<Alergeno> alergenosSeleccionados = new ArrayList<>();
        for (Map.Entry<CheckBox, Alergeno> entry : mapAlergenosUI.entrySet()) {
            if (entry.getKey().isSelected()) {
                alergenosSeleccionados.add(entry.getValue());
            }
        }

        try {
            controller.agregarProductoAlPedido(
                    seleccionado,
                    spnCantidad.getValue(),
                    modsSeleccionados,
                    alergenosSeleccionados,
                    cmbRestriccion.getValue(),
                    txtObservaciones.getText()
            );
            limpiarFormularioProducto();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            lblErrorSeleccion.setText(ex.getMessage());
        }
    }

    @FXML
    private void onQuitarItemClick() {
        DetallePedido sel = listCarrito.getSelectionModel().getSelectedItem();
        if (sel != null) {
            controller.eliminarDetalleDelPedido(sel);
        }
    }

    @FXML
    private void onAplicarCuponClick() {
        lblErrorCupon.setText("");
        try {
            boolean aplicado = controller.aplicarCuponPromocional(txtCodigoCupon.getText(), cmbRolActor.getValue());
            if (aplicado) {
                lblErrorCupon.setText("Cupón aplicado correctamente a la liquidación.");
                lblErrorCupon.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
            } else {
                lblErrorCupon.setText("Error RN-03: Código inválido, vencido o subtotal insuficiente (PROMO10: $20 | VIP20: $35).");
                lblErrorCupon.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            }
        } catch (IllegalStateException ex) {
            lblErrorCupon.setText("Error " + ex.getMessage());
            lblErrorCupon.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onConfirmarPedidoClick() {
        if (controller.getPedidoActual().getItems().isEmpty()) {
            lblErrorCupon.setText("Agregue al menos un plato al carrito antes de confirmar.");
            lblErrorCupon.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            return;
        }

        String opcionPago = cmbMetodoPago.getValue();
        MetodoPago metodo = new PagoTarjeta();
        if (opcionPago.contains("Transferencia")) {
            metodo = new PagoTransferencia();
        } else if (opcionPago.contains("Contra Entrega")) {
            metodo = new PagoEfectivoContraEntrega();
        }

        try {
            MetodoPago.ResultadoPago res = controller.confirmarPedidoYEmitirComanda(metodo, cmbRolActor.getValue());
            if (res.aprobado) {
                Pedido p = controller.getPedidoActual();
                StringBuilder resumen = new StringBuilder();
                resumen.append(res.mensaje).append("\n\n");
                resumen.append("• Orden: ").append(p.getIdPedido()).append("\n");
                resumen.append("• Cliente: ").append(p.getCliente().getNombre()).append(" (").append(p.getCliente().getDocumento()).append(")\n");
                resumen.append("• Rol Auditoría: ").append(p.getOperadorAuditor().getEtiqueta()).append("\n");
                resumen.append("• Total Liquidado: $").append(String.format("%.2f", p.getTotalFinal())).append("\n");

                if (p.getFacturaElectronica() != null) {
                    resumen.append("• Factura Electrónica (RN-04): ").append(p.getFacturaElectronica().getNumeroFactura()).append("\n");
                } else {
                    resumen.append("• Factura Electrónica: Pendiente de recaudo contra entrega\n");
                }

                if (p.getComandaDigital() != null) {
                    resumen.append("• Comanda KDS (RN-06): ").append(p.getComandaDigital().getIdComanda()).append("\n");
                }

                if (controller.getUltimoReciboPdf() != null) {
                    resumen.append("• Recibo PDF generado en: ").append(controller.getUltimoReciboPdf().getPath());
                    btnAbrirReciboPdf.setDisable(false);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Comprobante de Pedido y Recibo PDF");
                alert.setHeaderText("¡Transacción Confirmada y Recibo PDF Generado!");
                alert.setContentText(resumen.toString());
                alert.showAndWait();

                controller.abrirUltimoReciboPdf();

                btnConfirmarPedido.setDisable(true);
                panelSeleccionProductos.setDisable(true);
            }
        } catch (IllegalStateException ex) {
            lblErrorCupon.setText("Error " + ex.getMessage());
            lblErrorCupon.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onAbrirReciboPdfClick() {
        controller.abrirUltimoReciboPdf();
    }

    @FXML
    private void onNuevoPedidoClick() {
        controller.iniciarNuevoPedido();
        enlazarDatosReactivosPedido();
        btnConfirmarPedido.setDisable(false);
        btnAbrirReciboPdf.setDisable(true);
        panelSeleccionProductos.setDisable(true);
        txtCodigoCupon.clear();
        lblErrorCupon.setText("");
        lblErrorSeleccion.setText("");
        lblEstadoCobertura.setText("Seleccione un cliente o cree uno nuevo y valide su cobertura.");
        lblEstadoCobertura.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
    }

    @FXML
    private void onIrACrudProductosClick() {
        RolActor rol = cmbRolActor.getValue();
        if (rol != null && !rol.isPuedeGestionarCrud()) {
            lblEstadoCobertura.setText("Error RN-07: El rol '" + rol.getEtiqueta() + "' no tiene acceso al CRUD de Productos.");
            lblEstadoCobertura.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            return;
        }
        AppMain.mostrarVistaCrudProductos();
    }

    @FXML
    private void onIrAProcesarPedidosClick() {
        RolActor rol = cmbRolActor.getValue();
        if (rol != null && !rol.isPuedeGestionarCrud()) {
            lblEstadoCobertura.setText("Error RN-07: El rol '" + rol.getEtiqueta() + "' no tiene acceso a Operaciones y Logística.");
            lblEstadoCobertura.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            return;
        }
        AppMain.mostrarVistaProcesarPedido();
    }

    private void limpiarFormularioProducto() {
        spnCantidad.getValueFactory().setValue(1);
        mapModificadoresUI.keySet().forEach(chk -> chk.setSelected(false));
        mapAlergenosUI.keySet().forEach(chk -> chk.setSelected(false));
        cmbRestriccion.getSelectionModel().select(RestriccionAlimentaria.NINGUNA);
        txtObservaciones.clear();
        lblErrorSeleccion.setText("");
    }
}
