package org.example.royecttofinal.Model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    public static final double LIMITE_CONTRA_ENTREGA_CONSUMIDOR = 60.00;
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final StringProperty idPedido = new SimpleStringProperty();
    private final ObjectProperty<Cliente> cliente = new SimpleObjectProperty<>();
    private final ObservableList<DetallePedido> items = FXCollections.observableArrayList();
    private final ObjectProperty<EstadoPedido> estado = new SimpleObjectProperty<>(EstadoPedido.PENDIENTE_COBERTURA);

    private final DoubleProperty subtotalItems = new SimpleDoubleProperty(0.0);
    private final DoubleProperty tarifaFlete = new SimpleDoubleProperty(0.0);
    private final DoubleProperty descuento = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalFinal = new SimpleDoubleProperty(0.0);

    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompromisoEntrega;
    private ZonaCobertura zonaAplicada;
    private CuponDescuento cuponAplicado;
    private MetodoPago metodoPago;
    private FacturaElectronica facturaElectronica;
    private ComandaDigital comandaDigital;
    private RolActor operadorAuditor;
    private LocalDateTime fechaConfirmacion;

    // Atributos del Proceso 2: Procesar Pedido (Bodega, Despacho, Repartidor y Entrega)
    private final ObjectProperty<Repartidor> repartidorAsignado = new SimpleObjectProperty<>();
    private final BooleanProperty recaudoContraEntregaConfirmado = new SimpleBooleanProperty(false);
    private final StringProperty motivoIncidencia = new SimpleStringProperty("");
    private final StringProperty notasOperativas = new SimpleStringProperty("");
    private LocalDateTime fechaInicioPreparacion;
    private LocalDateTime fechaEmpaque;
    private LocalDateTime fechaDespacho;
    private LocalDateTime fechaEntregaReal;
    private final ObservableList<String> historialEventos = FXCollections.observableArrayList();

    private final List<PedidoObserver> observers = new ArrayList<>();

    public Pedido(String idPedido) {
        this.idPedido.set(idPedido);
        this.fechaCreacion = LocalDateTime.now();
        this.items.addListener((ListChangeListener<DetallePedido>) c -> recalcularLiquidacion());
        this.tarifaFlete.addListener((obs, o, n) -> recalcularLiquidacion());
        this.descuento.addListener((obs, o, n) -> recalcularLiquidacion());
        registrarEventoBitacora("Solicitud creada en estado: " + EstadoPedido.PENDIENTE_COBERTURA.getEtiqueta());
    }

    public void registrarObserver(PedidoObserver observer) {
        observers.add(observer);
    }

    private void registrarEventoBitacora(String detalle) {
        String marcaTiempo = LocalDateTime.now().format(HORA_FMT);
        historialEventos.add("[" + marcaTiempo + "] " + detalle);
    }

    private void cambiarEstado(EstadoPedido nuevoEstado) {
        EstadoPedido anterior = this.estado.get();
        this.estado.set(nuevoEstado);
        registrarEventoBitacora("Estado actualizado: " + anterior.getEtiqueta() + " -> " + nuevoEstado.getEtiqueta());
        for (PedidoObserver obs : observers) {
            obs.onCambioEstadoPedido(this, anterior, nuevoEstado);
        }
    }

    public boolean validarCobertura(ZonaCobertura zona, RolActor actor) {
        if (cliente.get() == null || !zona.cubreDireccion(cliente.get().getDireccion())) {
            return false;
        }
        this.zonaAplicada = zona;
        this.operadorAuditor = actor;
        this.tarifaFlete.set(zona.getTarifaFlete());
        this.fechaCompromisoEntrega = LocalDateTime.now().plusMinutes(zona.getTiempoEstimadoMinutos());
        cambiarEstado(EstadoPedido.EN_SELECCION);
        return true;
    }

    public void agregarItem(DetallePedido detalle) {
        if (estado.get() == EstadoPedido.PENDIENTE_COBERTURA) {
            throw new IllegalStateException("RN-02: Debe validar la cobertura geográfica antes de agregar platos.");
        }
        if (!detalle.getProducto().isDisponibleKDS()) {
            throw new IllegalStateException("RN-01: Producto bloqueado por falta de insumos en cocina KDS.");
        }
        detalle.subtotalProperty().addListener((obs, o, n) -> recalcularLiquidacion());
        items.add(detalle);
    }

    public void eliminarItem(DetallePedido detalle) {
        items.remove(detalle);
    }

    public boolean aplicarCupon(CuponDescuento cupon, RolActor actor) {
        if (cupon == null) return false;
        RolActor rolEfectivo = actor != null ? actor : RolActor.CONSUMIDOR;
        if ("VIP20".equalsIgnoreCase(cupon.getCodigo()) && !rolEfectivo.isPuedeAplicarCuponVip()) {
            throw new IllegalStateException("RN-07 (Permiso de Rol): El rol '" + rolEfectivo.getEtiqueta()
                    + "' no está autorizado para aplicar el cupón gerencial VIP20. Cambie a Cajero.");
        }
        if (cupon.esValido(subtotalItems.get())) {
            this.cuponAplicado = cupon;
            recalcularLiquidacion();
            return true;
        }
        return false;
    }

    public final void recalcularLiquidacion() {
        double sub = items.stream().mapToDouble(DetallePedido::getSubtotal).sum();
        subtotalItems.set(sub);

        if (cuponAplicado != null) {
            if (cuponAplicado.esValido(sub)) {
                descuento.set(cuponAplicado.calcularDescuento(sub));
            } else {
                cuponAplicado = null;
                descuento.set(0.0);
            }
        }
        totalFinal.set(Math.max(0.0, sub + tarifaFlete.get() - descuento.get()));
    }

    public MetodoPago.ResultadoPago procesarYConfirmarPedido(MetodoPago metodo, RolActor operador) {
        if (items.isEmpty()) {
            throw new IllegalStateException("Debe agregar al menos un plato antes de confirmar el pedido.");
        }
        RolActor rolEfectivo = operador != null ? operador : RolActor.CONSUMIDOR;

        if (metodo instanceof PagoTarjeta && !rolEfectivo.isPuedeCobrarTarjeta()) {
            throw new IllegalStateException("RN-07 (Permiso de Rol): El rol '" + rolEfectivo.getEtiqueta()
                    + "' no tiene terminal de Tarjeta habilitada. Use Transferencia, Contra Entrega o cambie a Cajero.");
        }
        if (metodo instanceof PagoEfectivoContraEntrega && rolEfectivo == RolActor.CONSUMIDOR
                && getTotalFinal() > LIMITE_CONTRA_ENTREGA_CONSUMIDOR) {
            throw new IllegalStateException("RN-07 (Límite Consumidor): Pedidos contra entrega superiores a $"
                    + String.format("%.2f", LIMITE_CONTRA_ENTREGA_CONSUMIDOR)
                    + " requieren validación de Auxiliar de Registro o Cajero.");
        }

        this.metodoPago = metodo;
        this.operadorAuditor = rolEfectivo;
        this.fechaConfirmacion = LocalDateTime.now();
        cambiarEstado(EstadoPedido.LIQUIDADO);

        MetodoPago.ResultadoPago res = metodo.procesarPago(getTotalFinal());
        if (res.aprobado) {
            if (res.esContraEntrega) {
                cambiarEstado(EstadoPedido.PENDIENTE_RECAUDO);
            } else {
                cambiarEstado(EstadoPedido.PAGO_CONFIRMADO);
                this.facturaElectronica = new FacturaElectronica(this, res.referenciaTransaccion);
            }
            this.comandaDigital = new ComandaDigital(this, this.operadorAuditor);
            cambiarEstado(EstadoPedido.ENVIADO_A_COCINA_KDS);
        }
        return res;
    }

    // =========================================================================
    // MÉTODOS DEL PROCESO 2: PROCESAR PEDIDO (BODEGA, DESPACHO Y ENTREGA)
    // =========================================================================

    public boolean requiereRecaudoContraEntrega() {
        return (metodoPago instanceof PagoEfectivoContraEntrega) && !recaudoContraEntregaConfirmado.get();
    }

    public void iniciarPreparacionBodega(String observacionBodega) {
        if (estado.get() != EstadoPedido.ENVIADO_A_COCINA_KDS && estado.get() != EstadoPedido.CON_INCIDENCIA) {
            throw new IllegalStateException("Paso 9: Solo se puede iniciar preparación de órdenes recibidas en Cocina/Bodega (KDS) o reanudadas tras novedad.");
        }
        this.fechaInicioPreparacion = LocalDateTime.now();
        if (observacionBodega != null && !observacionBodega.isBlank()) {
            this.notasOperativas.set("Bodega: " + observacionBodega.trim());
            registrarEventoBitacora("Verificación de inventario/bodega: " + observacionBodega.trim());
        } else {
            registrarEventoBitacora("Existencias verificadas al 100% por Encargado de Bodega/Cocina.");
        }
        cambiarEstado(EstadoPedido.EN_PREPARACION);
    }

    public void marcarEmpacadoYDocumentado(String observacionEmpaque) {
        if (estado.get() != EstadoPedido.EN_PREPARACION) {
            throw new IllegalStateException("Paso 10: El pedido debe estar 'En Preparación y Verificación' antes de empacarse.");
        }
        this.fechaEmpaque = LocalDateTime.now();
        String obs = (observacionEmpaque != null && !observacionEmpaque.isBlank())
                ? observacionEmpaque.trim()
                : "Paquete sellado con guía y comprobante adjunto.";
        this.notasOperativas.set(obs);
        registrarEventoBitacora("Empaque finalizado y documentación preparada: " + obs);
        cambiarEstado(EstadoPedido.EMPACADO_LISTO_DESPACHO);
    }

    public void asignarRepartidorYDespachar(Repartidor repartidor) {
        if (estado.get() != EstadoPedido.EMPACADO_LISTO_DESPACHO) {
            throw new IllegalStateException("Paso 11: El pedido debe estar empacado y listo antes de asignarse a un repartidor.");
        }
        if (repartidor == null) {
            throw new IllegalArgumentException("Seleccione un repartidor disponible para realizar el despacho.");
        }
        if (!repartidor.isDisponible()) {
            throw new IllegalStateException("El repartidor seleccionado alcanzó su cupo máximo de entregas activas.");
        }
        this.repartidorAsignado.set(repartidor);
        repartidor.asignarPedido();
        this.fechaDespacho = LocalDateTime.now();
        registrarEventoBitacora("Despacho asignado a " + repartidor.getNombre() + " (" + repartidor.getVehiculoPlaca() + "). Notificación enviada al cliente.");
        cambiarEstado(EstadoPedido.EN_CAMINO_DESPACHADO);
    }

    public void confirmarEntregaYCerrar(boolean recaudoConfirmadoEnSitio, String notaRecepcionCliente) {
        if (estado.get() != EstadoPedido.EN_CAMINO_DESPACHADO) {
            throw new IllegalStateException("Paso 13-15: Solo pueden cerrarse pedidos que estén 'Despachados / En Camino'.");
        }
        if (requiereRecaudoContraEntrega() && !recaudoConfirmadoEnSitio) {
            throw new IllegalStateException("Flujo Alterno A3: Pedido Contra Entrega ($"
                    + String.format("%.2f", getTotalFinal())
                    + "). Debe confirmar el recaudo del dinero en sitio antes de cerrar el pedido.");
        }
        if (requiereRecaudoContraEntrega() && recaudoConfirmadoEnSitio) {
            this.recaudoContraEntregaConfirmado.set(true);
            // Al recaudarse contra entrega, se habilita la facturación electrónica
            EstadoPedido estadoPrevio = this.estado.get();
            this.estado.set(EstadoPedido.PAGO_CONFIRMADO);
            this.facturaElectronica = new FacturaElectronica(this, "COD-RECAUDADO-SITIO");
            this.estado.set(estadoPrevio);
            registrarEventoBitacora("Recaudo Contra Entrega confirmado ($" + String.format("%.2f", getTotalFinal())
                    + "). Factura generada: " + this.facturaElectronica.getNumeroFactura());
        }
        this.fechaEntregaReal = LocalDateTime.now();
        if (repartidorAsignado.get() != null) {
            repartidorAsignado.get().liberarPedido();
        }
        String nota = (notaRecepcionCliente != null && !notaRecepcionCliente.isBlank())
                ? notaRecepcionCliente.trim()
                : "Cliente confirma recepción completa y a satisfacción.";
        registrarEventoBitacora("Entrega confirmada: " + nota);
        cambiarEstado(EstadoPedido.ENTREGADO_Y_CERRADO);
    }

    public void reportarNovedadOIncidencia(String tipoNovedad, String detalleIncidencia) {
        EstadoPedido actual = estado.get();
        if (actual == EstadoPedido.PENDIENTE_COBERTURA || actual == EstadoPedido.EN_SELECCION || actual == EstadoPedido.ENTREGADO_Y_CERRADO) {
            throw new IllegalStateException("No se puede reportar novedad operativa en el estado actual (" + actual.getEtiqueta() + ").");
        }
        if (detalleIncidencia == null || detalleIncidencia.isBlank()) {
            throw new IllegalArgumentException("Describa el detalle de la novedad o incidencia.");
        }
        String tipo = (tipoNovedad != null && !tipoNovedad.isBlank()) ? tipoNovedad.trim() : "Novedad Operativa";
        String textoCompleto = "[" + tipo + "] " + detalleIncidencia.trim();
        this.motivoIncidencia.set(textoCompleto);
        if (repartidorAsignado.get() != null) {
            repartidorAsignado.get().liberarPedido();
        }
        registrarEventoBitacora("NOVEDAD REGISTRADA: " + textoCompleto);
        cambiarEstado(EstadoPedido.CON_INCIDENCIA);
    }

    public boolean isEntregadoATiempo() {
        if (fechaEntregaReal == null || fechaCompromisoEntrega == null) return true;
        return !fechaEntregaReal.isAfter(fechaCompromisoEntrega);
    }

    public long getMinutosCicloOperativo() {
        LocalDateTime inicio = fechaConfirmacion != null ? fechaConfirmacion : fechaCreacion;
        LocalDateTime fin = fechaEntregaReal != null ? fechaEntregaReal : LocalDateTime.now();
        return Math.max(1, Duration.between(inicio, fin).toMinutes());
    }

    public String getFechaCreacionFormateada() {
        return fechaCreacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getFechaCompromisoFormateada() {
        if (fechaCompromisoEntrega == null || zonaAplicada == null) {
            return "Pendiente de validar zona";
        }
        return fechaCompromisoEntrega.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " (" + zonaAplicada.getTiempoEstimadoMinutos() + " min — " + zonaAplicada.getNombreZona() + ")";
    }

    public String getIdPedido() { return idPedido.get(); }
    public Cliente getCliente() { return cliente.get(); }
    public void setCliente(Cliente c) { this.cliente.set(c); }
    public ObservableList<DetallePedido> getItems() { return items; }
    public EstadoPedido getEstado() { return estado.get(); }
    public double getSubtotalItems() { return subtotalItems.get(); }
    public double getTarifaFlete() { return tarifaFlete.get(); }
    public double getDescuento() { return descuento.get(); }
    public double getTotalFinal() { return totalFinal.get(); }
    public ZonaCobertura getZonaAplicada() { return zonaAplicada; }
    public CuponDescuento getCuponAplicado() { return cuponAplicado; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public FacturaElectronica getFacturaElectronica() { return facturaElectronica; }
    public ComandaDigital getComandaDigital() { return comandaDigital; }
    public RolActor getOperadorAuditor() { return operadorAuditor; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public Repartidor getRepartidorAsignado() { return repartidorAsignado.get(); }
    public boolean isRecaudoContraEntregaConfirmado() { return recaudoContraEntregaConfirmado.get(); }
    public String getMotivoIncidencia() { return motivoIncidencia.get(); }
    public String getNotasOperativas() { return notasOperativas.get(); }
    public LocalDateTime getFechaInicioPreparacion() { return fechaInicioPreparacion; }
    public LocalDateTime getFechaEmpaque() { return fechaEmpaque; }
    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public LocalDateTime getFechaEntregaReal() { return fechaEntregaReal; }
    public ObservableList<String> getHistorialEventos() { return historialEventos; }

    public ReadOnlyDoubleProperty subtotalItemsProperty() { return subtotalItems; }
    public DoubleProperty tarifaFleteProperty() { return tarifaFlete; }
    public ReadOnlyDoubleProperty descuentoProperty() { return descuento; }
    public ReadOnlyDoubleProperty totalFinalProperty() { return totalFinal; }
    public ObjectProperty<EstadoPedido> estadoProperty() { return estado; }
    public ObjectProperty<Repartidor> repartidorAsignadoProperty() { return repartidorAsignado; }
    public StringProperty motivoIncidenciaProperty() { return motivoIncidencia; }
}
