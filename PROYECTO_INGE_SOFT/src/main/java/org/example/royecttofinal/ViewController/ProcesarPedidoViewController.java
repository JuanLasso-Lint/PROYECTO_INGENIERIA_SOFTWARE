package org.example.royecttofinal.ViewController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.royecttofinal.Controller.IPedidoController;
import org.example.royecttofinal.Model.*;
import org.example.royecttofinal.app.AppMain;

import java.util.stream.Collectors;

public class ProcesarPedidoViewController {

    @FXML private Label lblKpiTotales;
    @FXML private Label lblKpiEnProceso;
    @FXML private Label lblKpiEntregados;
    @FXML private Label lblKpiIncidencias;

    @FXML private TextField txtBuscarOrden;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Pedido> tblPedidosOperativos;
    @FXML private TableColumn<Pedido, String> colOrdenId;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colTotal;
    @FXML private TableColumn<Pedido, String> colRecaudo;
    @FXML private TableColumn<Pedido, String> colRepartidor;
    @FXML private TableColumn<Pedido, String> colEstado;

    @FXML private Label lblDetalleEncabezado;
    @FXML private Label lblDetalleDireccion;
    @FXML private Label lblDetalleItems;

    @FXML private TextField txtObsBodega;
    @FXML private Button btnIniciarPreparacion;
    @FXML private Button btnEmpacarPedido;
    @FXML private ComboBox<Repartidor> cmbRepartidor;
    @FXML private Button btnDespacharPedido;
    @FXML private CheckBox chkRecaudoConfirmado;
    @FXML private TextField txtNotaRecepcionCliente;
    @FXML private Button btnConfirmarEntrega;
    @FXML private Button btnVerGuiaPdf;

    @FXML private ComboBox<String> cmbTipoNovedad;
    @FXML private TextField txtDetalleNovedad;
    @FXML private Button btnReportarNovedad;
    @FXML private Label lblMensajeOperacion;
    @FXML private ListView<String> listBitacoraPedido;

    private IPedidoController controller;
    private FilteredList<Pedido> pedidosFiltrados;
    private Pedido pedidoSeleccionado;

    @FXML
    public void initialize() {
        this.controller = AppMain.getSharedController();

        cmbFiltroEstado.getItems().setAll(
                "Todos los Estados",
                EstadoPedido.ENVIADO_A_COCINA_KDS.getEtiqueta(),
                EstadoPedido.EN_PREPARACION.getEtiqueta(),
                EstadoPedido.EMPACADO_LISTO_DESPACHO.getEtiqueta(),
                EstadoPedido.EN_CAMINO_DESPACHADO.getEtiqueta(),
                EstadoPedido.ENTREGADO_Y_CERRADO.getEtiqueta(),
                EstadoPedido.CON_INCIDENCIA.getEtiqueta()
        );
        cmbFiltroEstado.getSelectionModel().selectFirst();

        cmbTipoNovedad.getItems().setAll(
                "A2 - Insuficiencia de Insumo",
                "A3 - Fallo Recaudo Contra Entrega",
                "Dirección Errada / Cliente Ausente",
                "Incidencia en Transporte / Ruta"
        );
        cmbTipoNovedad.getSelectionModel().selectFirst();

        cmbRepartidor.setItems(controller.getRepartidores());
        if (!controller.getRepartidores().isEmpty()) {
            cmbRepartidor.getSelectionModel().selectFirst();
        }

        colOrdenId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPedido()));
        colCliente.setCellValueFactory(d -> {
            Cliente c = d.getValue().getCliente();
            String txt = c != null ? c.getNombre() + " (" + c.getDireccion().getBarrio() + ")" : "-";
            return new SimpleStringProperty(txt);
        });
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getTotalFinal())));
        colRecaudo.setCellValueFactory(d -> {
            Pedido p = d.getValue();
            String cond = p.requiereRecaudoContraEntrega() ? "Por Cobrar (COD)" : "Pagado";
            return new SimpleStringProperty(cond);
        });
        colRepartidor.setCellValueFactory(d -> {
            Repartidor r = d.getValue().getRepartidorAsignado();
            return new SimpleStringProperty(r != null ? r.getNombre() : "Sin asignar");
        });
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().getEtiqueta()));

        pedidosFiltrados = new FilteredList<>(controller.getPedidosOperativos(), p -> true);
        txtBuscarOrden.textProperty().addListener((obs, o, n) -> aplicarFiltrosTabla());
        cmbFiltroEstado.setOnAction(e -> aplicarFiltrosTabla());

        tblPedidosOperativos.setItems(pedidosFiltrados);
        tblPedidosOperativos.setPlaceholder(new Label("No hay pedidos confirmados que coincidan con el filtro."));

        tblPedidosOperativos.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            pedidoSeleccionado = sel;
            actualizarPanelDetalleYBotones();
        });

        if (!pedidosFiltrados.isEmpty()) {
            tblPedidosOperativos.getSelectionModel().selectFirst();
        }
        actualizarKpis();
    }

    private void aplicarFiltrosTabla() {
        String q = txtBuscarOrden.getText() == null ? "" : txtBuscarOrden.getText().trim().toLowerCase();
        String estadoFiltro = cmbFiltroEstado.getValue();
        pedidosFiltrados.setPredicate(p -> {
            boolean coincideTexto = q.isEmpty()
                    || p.getIdPedido().toLowerCase().contains(q)
                    || (p.getCliente() != null && (p.getCliente().getNombre().toLowerCase().contains(q)
                    || p.getCliente().getDireccion().getBarrio().toLowerCase().contains(q)));
            boolean coincideEstado = estadoFiltro == null
                    || "Todos los Estados".equals(estadoFiltro)
                    || p.getEstado().getEtiqueta().equals(estadoFiltro);
            return coincideTexto && coincideEstado;
        });
    }

    private void actualizarPanelDetalleYBotones() {
        if (pedidoSeleccionado == null) {
            lblDetalleEncabezado.setText("Seleccione un pedido de la tabla para gestionar su preparación y entrega.");
            lblDetalleDireccion.setText("Dirección: --- | Teléfono: --- | Compromiso: ---");
            lblDetalleItems.setText("Platos: ---");
            listBitacoraPedido.setItems(FXCollections.observableArrayList());
            btnIniciarPreparacion.setDisable(true);
            btnEmpacarPedido.setDisable(true);
            btnDespacharPedido.setDisable(true);
            btnConfirmarEntrega.setDisable(true);
            btnReportarNovedad.setDisable(true);
            btnVerGuiaPdf.setDisable(true);
            return;
        }

        Pedido p = pedidoSeleccionado;
        Cliente c = p.getCliente();
        EstadoPedido est = p.getEstado();

        lblDetalleEncabezado.setText(String.format("Orden %s — Estado: %s | Total: $%.2f (%s)",
                p.getIdPedido(),
                est.getEtiqueta(),
                p.getTotalFinal(),
                p.requiereRecaudoContraEntrega() ? "PENDIENTE RECAUDO CONTRA ENTREGA" : "PAGADO"));

        lblDetalleDireccion.setText(String.format("Cliente: %s | Tel: %s | Dirección: %s | Compromiso: %s",
                c != null ? c.getNombre() : "-",
                c != null ? c.getTelefono() : "-",
                c != null ? c.getDireccion().toString() : "-",
                p.getFechaCompromisoFormateada()));

        String resumenPlatos = p.getItems().stream()
                .map(d -> d.getCantidad() + "x " + d.getProducto().getNombre()
                        + (d.getRestriccion() != RestriccionAlimentaria.NINGUNA ? " [" + d.getRestriccion() + "]" : ""))
                .collect(Collectors.joining("  •  "));
        lblDetalleItems.setText("Detalle a verificar: " + resumenPlatos);

        listBitacoraPedido.setItems(p.getHistorialEventos());
        if (!p.getHistorialEventos().isEmpty()) {
            listBitacoraPedido.scrollTo(p.getHistorialEventos().size() - 1);
        }

        // Habilitación inteligente de botones según etapa del proceso
        btnIniciarPreparacion.setDisable(est != EstadoPedido.ENVIADO_A_COCINA_KDS && est != EstadoPedido.CON_INCIDENCIA);
        btnEmpacarPedido.setDisable(est != EstadoPedido.EN_PREPARACION);
        btnDespacharPedido.setDisable(est != EstadoPedido.EMPACADO_LISTO_DESPACHO);
        btnConfirmarEntrega.setDisable(est != EstadoPedido.EN_CAMINO_DESPACHADO);
        btnReportarNovedad.setDisable(est == EstadoPedido.ENTREGADO_Y_CERRADO);
        btnVerGuiaPdf.setDisable(false);

        chkRecaudoConfirmado.setDisable(!p.requiereRecaudoContraEntrega());
        chkRecaudoConfirmado.setSelected(!p.requiereRecaudoContraEntrega());
    }

    @FXML
    private void onIniciarPreparacionClick() {
        ejecutarAccionOperativa(() -> {
            controller.iniciarPreparacionPedido(pedidoSeleccionado, txtObsBodega.getText());
            txtObsBodega.clear();
            mostrarMensaje("Paso 9 completado: Existencias verificadas y pedido en preparación.", "#2563EB");
        });
    }

    @FXML
    private void onEmpacarPedidoClick() {
        ejecutarAccionOperativa(() -> {
            controller.empacarPedidoYPrepararDocumentacion(pedidoSeleccionado, txtObsBodega.getText());
            txtObsBodega.clear();
            controller.generarYAbrirGuiaDespacho(pedidoSeleccionado);
            mostrarMensaje("Paso 10 completado: Pedido empacado y Guía de Despacho PDF generada.", "#0D9488");
        });
    }

    @FXML
    private void onDespacharPedidoClick() {
        ejecutarAccionOperativa(() -> {
            Repartidor rep = cmbRepartidor.getValue();
            controller.despacharPedidoConRepartidor(pedidoSeleccionado, rep);
            cmbRepartidor.getItems().setAll(controller.getRepartidores());
            cmbRepartidor.getSelectionModel().select(rep);
            mostrarMensaje("Pasos 11–12 completados: Pedido despachado con " + rep.getNombre() + " y cliente notificado.", "#D97706");
        });
    }

    @FXML
    private void onConfirmarEntregaClick() {
        ejecutarAccionOperativa(() -> {
            controller.confirmarEntregaPedido(
                    pedidoSeleccionado,
                    chkRecaudoConfirmado.isSelected(),
                    txtNotaRecepcionCliente.getText()
            );
            txtNotaRecepcionCliente.clear();
            mostrarMensaje("Pasos 13–15 completados: Entrega confirmada y pedido cerrado exitosamente.", "#16A34A");
        });
    }

    @FXML
    private void onReportarNovedadClick() {
        ejecutarAccionOperativa(() -> {
            controller.registrarIncidenciaPedido(
                    pedidoSeleccionado,
                    cmbTipoNovedad.getValue(),
                    txtDetalleNovedad.getText()
            );
            txtDetalleNovedad.clear();
            mostrarMensaje("Incidencia registrada en la bitácora del pedido.", "#DC2626");
        });
    }

    @FXML
    private void onAbrirGuiaPdfClick() {
        if (pedidoSeleccionado == null) return;
        try {
            controller.generarYAbrirGuiaDespacho(pedidoSeleccionado);
        } catch (Exception ex) {
            mostrarMensaje("Error al abrir guía PDF: " + ex.getMessage(), "#DC2626");
        }
    }

    private void ejecutarAccionOperativa(Runnable accion) {
        if (pedidoSeleccionado == null) {
            mostrarMensaje("Seleccione un pedido de la tabla operativa.", "#DC2626");
            return;
        }
        try {
            accion.run();
            tblPedidosOperativos.refresh();
            actualizarPanelDetalleYBotones();
            actualizarKpis();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            mostrarMensaje("Validación de Proceso: " + ex.getMessage(), "#DC2626");
        }
    }

    private void actualizarKpis() {
        var lista = controller.getPedidosOperativos();
        long total = lista.size();
        long entregados = lista.stream().filter(p -> p.getEstado() == EstadoPedido.ENTREGADO_Y_CERRADO).count();
        long incidencias = lista.stream().filter(p -> p.getEstado() == EstadoPedido.CON_INCIDENCIA).count();
        long enProceso = total - entregados - incidencias;

        lblKpiTotales.setText("Órdenes Confirmadas: " + total);
        lblKpiEnProceso.setText("En Proceso Operativo: " + enProceso);
        lblKpiEntregados.setText("Entregados y Cerrados: " + entregados);
        lblKpiIncidencias.setText("Con Incidencia: " + incidencias);
    }

    private void mostrarMensaje(String texto, String colorHex) {
        lblMensajeOperacion.setText(texto);
        lblMensajeOperacion.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
    }

    @FXML
    private void onVolverSolicitarPedidoClick() {
        AppMain.mostrarVistaSolicitarPedido();
    }

    @FXML
    private void onIrCrudProductosClick() {
        AppMain.mostrarVistaCrudProductos();
    }
}
