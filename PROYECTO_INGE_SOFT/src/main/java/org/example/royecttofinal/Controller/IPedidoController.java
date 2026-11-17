package org.example.royecttofinal.Controller;

import javafx.collections.ObservableList;
import org.example.royecttofinal.Model.*;
import java.io.File;
import java.util.List;

public interface IPedidoController {
    Pedido getPedidoActual();
    CatalogoMenu getCatalogoMenu();
    List<Modificador> getModificadoresDisponibles();
    ObservableList<Cliente> getClientesRegistrados();
    File getUltimoReciboPdf();

    void registrarObserver(PedidoObserver observer);
    Cliente crearYRegistrarCliente(String documento, String nombre, String telefono, String calle, String barrio);
    boolean validarCobertura(String documento, String nombreCliente, String telefono, String calle, String barrio, RolActor rol);
    void agregarProductoAlPedido(Producto producto, int cantidad, List<Modificador> mods,
                                 List<Alergeno> alergenos, RestriccionAlimentaria restriccion, String obs);
    void eliminarDetalleDelPedido(DetallePedido detalle);
    boolean aplicarCuponPromocional(String codigoCupon, RolActor rol);
    MetodoPago.ResultadoPago confirmarPedidoYEmitirComanda(MetodoPago metodoPago, RolActor operador);
    void abrirUltimoReciboPdf();
    void iniciarNuevoPedido();

    // CRUD de Productos (Pantalla 2 - RF-11)
    void crearProductoCatalogo(String id, String nombre, String categoria, double precio, boolean disponibleKDS);
    void actualizarProductoCatalogo(Producto producto, String nombre, String categoria, double precio, boolean disponibleKDS);
    void eliminarProductoCatalogo(Producto producto);

    // Proceso 2: Procesar Pedido — Preparación, Empaque, Despacho y Entrega (Pantalla 3)
    ObservableList<Pedido> getPedidosOperativos();
    ObservableList<Repartidor> getRepartidores();
    void iniciarPreparacionPedido(Pedido pedido, String observacionBodega);
    void empacarPedidoYPrepararDocumentacion(Pedido pedido, String observacionEmpaque);
    void despacharPedidoConRepartidor(Pedido pedido, Repartidor repartidor);
    void confirmarEntregaPedido(Pedido pedido, boolean recaudoConfirmado, String notaCliente);
    void registrarIncidenciaPedido(Pedido pedido, String tipoNovedad, String detalle);
    File generarYAbrirGuiaDespacho(Pedido pedido);
}
