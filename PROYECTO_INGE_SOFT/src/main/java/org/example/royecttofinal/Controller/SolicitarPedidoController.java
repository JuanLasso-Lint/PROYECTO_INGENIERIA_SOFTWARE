package org.example.royecttofinal.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.royecttofinal.Model.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SolicitarPedidoController implements IPedidoController {

    private Pedido pedidoActual;
    private File ultimoReciboPdf;
    private final ReciboPdfService reciboPdfService;
    private final CatalogoMenu catalogoMenu;
    private final List<ZonaCobertura> zonasAutorizadas;
    private final List<CuponDescuento> cuponesActivos;
    private final List<Modificador> modificadoresDisponibles;
    private final ObservableList<Cliente> clientesRegistrados;
    private final ObservableList<Pedido> pedidosOperativos;
    private final ObservableList<Repartidor> repartidores;
    private final List<PedidoObserver> observersGlobales;

    public SolicitarPedidoController() {
        this.reciboPdfService = new ReciboPdfService();
        this.catalogoMenu = new CatalogoMenu();
        this.zonasAutorizadas = new ArrayList<>();
        this.cuponesActivos = new ArrayList<>();
        this.modificadoresDisponibles = new ArrayList<>();
        this.clientesRegistrados = FXCollections.observableArrayList();
        this.pedidosOperativos = FXCollections.observableArrayList();
        this.repartidores = FXCollections.observableArrayList();
        this.observersGlobales = new ArrayList<>();

        this.observersGlobales.add(new KDSNotificationService());

        inicializarDatosMaestro();
        inicializarPedidosOperativosDemo();
        iniciarNuevoPedido();
    }

    private void inicializarDatosMaestro() {
        zonasAutorizadas.add(new ZonaCobertura("Zona Centro-Norte", Set.of("Centro", "Cabecera", "Alameda", "Sotomayor"), 3.50, 25));
        zonasAutorizadas.add(new ZonaCobertura("Zona Sur-Residencial", Set.of("Provenza", "Cañaveral", "Lagos", "Diamante"), 4.50, 35));

        clientesRegistrados.addAll(
                new Cliente("CLI-01", "1085100101", "Laura Gómez", "3105551001", new Direccion("Cra 15 # 20-10", "Centro")),
                new Cliente("CLI-02", "1085100102", "Carlos Mendoza", "3105551002", new Direccion("Cll 48 # 32-15", "Cabecera")),
                new Cliente("CLI-03", "1085100103", "Sofía Martínez", "3105551003", new Direccion("Cra 27 # 10-40", "Alameda")),
                new Cliente("CLI-04", "1085100104", "Andrés Herrera", "3105551004", new Direccion("Cll 105 # 22-18", "Provenza")),
                new Cliente("CLI-05", "1085100105", "Valentina Ríos", "3105551005", new Direccion("Cra 24 # 150-12", "Cañaveral")),
                new Cliente("CLI-06", "1085100106", "Mateo Castro", "3105551006", new Direccion("Cll 33 # 18-09", "Lagos")),
                new Cliente("CLI-07", "1085100107", "Camila Vargas", "3105551007", new Direccion("Cra 35 # 52-20", "Sotomayor")),
                new Cliente("CLI-08", "1085100108", "Julián Pineda", "3105551008", new Direccion("Cll 88 # 19-45", "Diamante")),
                new Cliente("CLI-09", "1085100109", "Mariana Salazar", "3105551009", new Direccion("Cra 19 # 36-11", "Centro")),
                new Cliente("CLI-10", "1085100110", "Sebastián Ortiz", "3105551010", new Direccion("Cll 51 # 29-30", "Cabecera")),
                new Cliente("CLI-11", "1085100111", "Natalia Duarte (Sin Cobertura)", "3105551011", new Direccion("Km 12 Vía Rural", "Vereda Norte")),
                new Cliente("CLI-12", "1085100112", "Diego Restrepo (Sin Cobertura)", "3105551012", new Direccion("Bodega 4", "Zona Industrial"))
        );

        repartidores.addAll(
                new Repartidor("REP-01", "Carlos Ramírez", "3154001101", "Moto XTZ-45E"),
                new Repartidor("REP-02", "Diana Morales", "3154001102", "Moto NKD-88F"),
                new Repartidor("REP-03", "Jorge Benítez", "3154001103", "Bici-Cargo #03"),
                new Repartidor("REP-04", "Paola Cárdenas", "3154001104", "Moto AKT-19G"),
                new Repartidor("REP-04","Santiago Arcila", "311548","Moto XSW - 474")
                );

        Insumo carneAngus = new Insumo("INS-01", "Carne Angus 150g", true);
        Insumo quesoMozzarella = new Insumo("INS-02", "Queso Mozzarella", false);
        Insumo pechugaPollo = new Insumo("INS-03", "Pechuga de Pollo", true);
        Insumo salmonFresco = new Insumo("INS-04", "Salmón Atlántico", false);
        Insumo lomoRes = new Insumo("INS-05", "Lomo Fino de Res", true);

        catalogoMenu.registrarProducto(new Producto("P-01", "Hamburguesa Artesanal Angus", "Fuertes", 12.00, carneAngus));
        catalogoMenu.registrarProducto(new Producto("P-02", "Pizza Pepperoni Extremo", "Pizzas", 15.00, quesoMozzarella));
        catalogoMenu.registrarProducto(new Producto("P-03", "Wrap de Pollo Grillé", "Fuertes", 10.50, pechugaPollo));
        catalogoMenu.registrarProducto(new Producto("P-04", "Limonada de Coco Natural", "Bebidas", 4.50));
        catalogoMenu.registrarProducto(new Producto("P-05", "Hamburguesa Doble Tocino", "Fuertes", 14.50, carneAngus));
        catalogoMenu.registrarProducto(new Producto("P-06", "Salmón en Salsa Teriyaki", "Fuertes", 18.00, salmonFresco));
        catalogoMenu.registrarProducto(new Producto("P-07", "Churrasco Argentino 300g", "Carnes", 17.50, lomoRes));
        catalogoMenu.registrarProducto(new Producto("P-08", "Ensalada César con Pollo", "Ensaladas", 9.50, pechugaPollo));
        catalogoMenu.registrarProducto(new Producto("P-09", "Pizza Cuatro Quesos", "Pizzas", 14.00, quesoMozzarella));
        catalogoMenu.registrarProducto(new Producto("P-10", "Papas Rústicas con Cheddar", "Entradas", 5.50));
        catalogoMenu.registrarProducto(new Producto("P-11", "Aros de Cebolla Apanados", "Entradas", 5.00));
        catalogoMenu.registrarProducto(new Producto("P-12", "Alitas BBQ x10 unidades", "Entradas", 11.00, pechugaPollo));
        catalogoMenu.registrarProducto(new Producto("P-13", "Pasta Carbonara Tradicional", "Pastas", 12.50));
        catalogoMenu.registrarProducto(new Producto("P-14", "Lasaña Boloñesa Gratinada", "Pastas", 13.50, quesoMozzarella));
        catalogoMenu.registrarProducto(new Producto("P-15", "Jugo Natural de Maracuyá", "Bebidas", 3.50));
        catalogoMenu.registrarProducto(new Producto("P-16", "Té Helado Durazno", "Bebidas", 3.20));
        catalogoMenu.registrarProducto(new Producto("P-17", "Cerveza Artesanal Rubia", "Bebidas", 4.80));
        catalogoMenu.registrarProducto(new Producto("P-18", "Brownie con Helado Vainilla", "Postres", 6.00));
        catalogoMenu.registrarProducto(new Producto("P-19", "Cheesecake de Frutos Rojos", "Postres", 6.50));
        catalogoMenu.registrarProducto(new Producto("P-20", "Bowl Vegetariano Quinoa", "Ensaladas", 10.00));

        modificadoresDisponibles.add(new Modificador("M-01", "Extra Queso Cheddar", 1.50));
        modificadoresDisponibles.add(new Modificador("M-02", "Tocineta Crujiente", 2.00));
        modificadoresDisponibles.add(new Modificador("M-03", "Aguacate Fresco", 1.80));
        modificadoresDisponibles.add(new Modificador("M-04", "Papas Agrandadas", 2.50));
        modificadoresDisponibles.add(new Modificador("M-05", "Salsa de la Casa", 0.80));
        modificadoresDisponibles.add(new Modificador("M-06", "Huevo Frito", 1.20));

        cuponesActivos.add(new CuponDescuento("PROMO10", 10.0, 20.00, LocalDate.now().plusDays(15)));
        cuponesActivos.add(new CuponDescuento("VIP20", 20.0, 35.00, LocalDate.now().plusDays(7)));
        cuponesActivos.add(new CuponDescuento("EXPIRADO", 15.0, 10.00, LocalDate.now().minusDays(2)));
    }

    private void inicializarPedidosOperativosDemo() {
        Producto p1 = catalogoMenu.getTodosLosProductos().get(0); // Hamburguesa Angus
        Producto p3 = catalogoMenu.getTodosLosProductos().get(2); // Wrap Pollo
        Producto p4 = catalogoMenu.getTodosLosProductos().get(3); // Limonada
        Producto p7 = catalogoMenu.getTodosLosProductos().get(6); // Churrasco

        // Demo 1: Recibido en Cocina/Bodega (Pago Tarjeta)
        Pedido demo1 = new Pedido("PED-1001A");
        demo1.setCliente(clientesRegistrados.get(0));
        demo1.validarCobertura(zonasAutorizadas.get(0), RolActor.CAJERO);
        demo1.agregarItem(new DetallePedido(p1, 2));
        demo1.agregarItem(new DetallePedido(p4, 2));
        demo1.procesarYConfirmarPedido(new PagoTarjeta(), RolActor.CAJERO);

        // Demo 2: En Preparación (Pago Contra Entrega - para probar recaudo en sitio)
        Pedido demo2 = new Pedido("PED-1002B");
        demo2.setCliente(clientesRegistrados.get(3));
        demo2.validarCobertura(zonasAutorizadas.get(1), RolActor.AUXILIAR_REGISTRO);
        demo2.agregarItem(new DetallePedido(p7, 1));
        demo2.agregarItem(new DetallePedido(p4, 1));
        demo2.procesarYConfirmarPedido(new PagoEfectivoContraEntrega(), RolActor.AUXILIAR_REGISTRO);
        demo2.iniciarPreparacionBodega("Insumos verificados en bodega fría.");

        // Demo 3: Empacado y Listo para Despacho
        Pedido demo3 = new Pedido("PED-1003C");
        demo3.setCliente(clientesRegistrados.get(1));
        demo3.validarCobertura(zonasAutorizadas.get(0), RolActor.CAJERO);
        demo3.agregarItem(new DetallePedido(p3, 2));
        demo3.procesarYConfirmarPedido(new PagoTransferencia(), RolActor.CAJERO);
        demo3.iniciarPreparacionBodega("Orden verificada completa.");
        demo3.marcarEmpacadoYDocumentado("Bolsa térmica #14 sellada con comprobante.");

        pedidosOperativos.addAll(demo1, demo2, demo3);
    }

    @Override
    public void iniciarNuevoPedido() {
        String idGenerado = "PED-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.pedidoActual = new Pedido(idGenerado);
        this.ultimoReciboPdf = null;
        for (PedidoObserver obs : observersGlobales) {
            this.pedidoActual.registrarObserver(obs);
        }
    }

    @Override
    public void registrarObserver(PedidoObserver observer) {
        this.observersGlobales.add(observer);
        if (this.pedidoActual != null) {
            this.pedidoActual.registrarObserver(observer);
        }
    }

    @Override
    public Cliente crearYRegistrarCliente(String documento, String nombre, String telefono, String calle, String barrio) {
        if (documento == null || documento.isBlank()
                || nombre == null || nombre.isBlank()
                || telefono == null || telefono.isBlank()
                || calle == null || calle.isBlank()
                || barrio == null || barrio.isBlank()) {
            throw new IllegalArgumentException("Complete Documento, Nombre, Teléfono, Dirección y Barrio para crear el cliente.");
        }
        String docNorm = documento.trim();
        boolean yaExiste = clientesRegistrados.stream().anyMatch(c -> c.getDocumento().equalsIgnoreCase(docNorm));
        if (yaExiste) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el documento: " + docNorm);
        }

        String nuevoId = String.format("CLI-%02d", clientesRegistrados.size() + 1);
        Cliente nuevoCliente = new Cliente(
                nuevoId,
                docNorm,
                nombre.trim(),
                telefono.trim(),
                new Direccion(calle.trim(), barrio.trim())
        );
        clientesRegistrados.add(0, nuevoCliente);
        return nuevoCliente;
    }

    @Override
    public boolean validarCobertura(String documento, String nombreCliente, String telefono, String calle, String barrio, RolActor rol) {
        if (nombreCliente == null || nombreCliente.isBlank() || calle == null || calle.isBlank() || barrio == null || barrio.isBlank()) {
            return false;
        }
        String docFinal = (documento != null && !documento.isBlank())
                ? documento.trim()
                : "DOC-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Direccion direccion = new Direccion(calle.trim(), barrio.trim());
        Cliente cliente = clientesRegistrados.stream()
                .filter(c -> c.getDocumento().equalsIgnoreCase(docFinal))
                .findFirst()
                .orElseGet(() -> new Cliente("CLI-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                        docFinal, nombreCliente.trim(), telefono != null ? telefono.trim() : "", direccion));

        cliente.setDireccion(direccion);
        pedidoActual.setCliente(cliente);

        for (ZonaCobertura zona : zonasAutorizadas) {
            if (pedidoActual.validarCobertura(zona, rol)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void agregarProductoAlPedido(Producto producto, int cantidad, List<Modificador> mods,
                                        List<Alergeno> alergenos, RestriccionAlimentaria restriccion, String obs) {
        DetallePedido detalle = new DetallePedido(producto, cantidad);
        if (mods != null) {
            for (Modificador mod : mods) {
                if (!detalle.agregarModificador(mod)) {
                    throw new IllegalStateException("RN-05: Límite máximo de " + DetallePedido.MAX_MODIFICADORES + " adicionales por plato.");
                }
            }
        }
        if (alergenos != null) detalle.asignarAlergenos(alergenos);
        detalle.setRestriccion(restriccion != null ? restriccion : RestriccionAlimentaria.NINGUNA);
        detalle.setObservaciones(obs);
        pedidoActual.agregarItem(detalle);
    }

    @Override
    public void eliminarDetalleDelPedido(DetallePedido detalle) {
        pedidoActual.eliminarItem(detalle);
    }

    @Override
    public boolean aplicarCuponPromocional(String codigoCupon, RolActor rol) {
        if (codigoCupon == null || codigoCupon.isBlank()) return false;
        String codigoNorm = codigoCupon.trim().toUpperCase();
        for (CuponDescuento cupon : cuponesActivos) {
            if (cupon.getCodigo().equals(codigoNorm)) {
                return pedidoActual.aplicarCupon(cupon, rol);
            }
        }
        return false;
    }

    @Override
    public MetodoPago.ResultadoPago confirmarPedidoYEmitirComanda(MetodoPago metodoPago, RolActor operador) {
        MetodoPago.ResultadoPago res = pedidoActual.procesarYConfirmarPedido(metodoPago, operador);
        if (res.aprobado) {
            if (!pedidosOperativos.contains(pedidoActual)) {
                pedidosOperativos.add(0, pedidoActual);
            }
            this.ultimoReciboPdf = reciboPdfService.generarReciboPdf(pedidoActual, res);
            System.out.println("[RECIBO PDF GENERADO] Archivo guardado en: " + ultimoReciboPdf.getAbsolutePath());
        }
        return res;
    }

    @Override
    public void abrirUltimoReciboPdf() {
        abrirArchivoPdf(ultimoReciboPdf);
    }

    private void abrirArchivoPdf(File archivo) {
        if (archivo != null && archivo.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(archivo);
            } catch (IOException e) {
                System.err.println("No se pudo abrir automáticamente el visor PDF: " + e.getMessage());
            }
        }
    }

    @Override
    public void crearProductoCatalogo(String id, String nombre, String categoria, double precio, boolean disponibleKDS) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank() || categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Todos los campos del producto son obligatorios.");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a $0.00.");
        }
        if (catalogoMenu.existeId(id)) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + id.toUpperCase());
        }
        Producto nuevo = new Producto(id.trim().toUpperCase(), nombre.trim(), categoria.trim(), precio);
        nuevo.setHabilitadoEnCocina(disponibleKDS);
        catalogoMenu.registrarProducto(nuevo);
        catalogoMenu.refrescarDisponibilidad();
    }

    @Override
    public void actualizarProductoCatalogo(Producto producto, String nombre, String categoria, double precio, boolean disponibleKDS) {
        if (producto == null) throw new IllegalArgumentException("Seleccione un producto de la tabla.");
        if (nombre == null || nombre.isBlank() || categoria == null || categoria.isBlank() || precio <= 0) {
            throw new IllegalArgumentException("Verifique que el nombre, categoría y precio (> 0) sean válidos.");
        }
        producto.setNombre(nombre.trim());
        producto.setCategoria(categoria.trim());
        producto.setPrecioBase(precio);
        producto.setHabilitadoEnCocina(disponibleKDS);
        for (Insumo insumo : producto.getIngredientesBase()) {
            insumo.setDisponibleKDS(disponibleKDS);
        }
        catalogoMenu.refrescarDisponibilidad();
    }

    @Override
    public void eliminarProductoCatalogo(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("Seleccione un producto para eliminar.");
        catalogoMenu.eliminarProducto(producto);
    }

    // =========================================================================
    // IMPLEMENTACIÓN PROCESO 2: PROCESAR PEDIDO (BODEGA, DESPACHO Y ENTREGA)
    // =========================================================================

    @Override
    public void iniciarPreparacionPedido(Pedido pedido, String observacionBodega) {
        if (pedido == null) throw new IllegalArgumentException("Seleccione un pedido de la tabla operativa.");
        pedido.iniciarPreparacionBodega(observacionBodega);
    }

    @Override
    public void empacarPedidoYPrepararDocumentacion(Pedido pedido, String observacionEmpaque) {
        if (pedido == null) throw new IllegalArgumentException("Seleccione un pedido de la tabla operativa.");
        pedido.marcarEmpacadoYDocumentado(observacionEmpaque);
    }

    @Override
    public void despacharPedidoConRepartidor(Pedido pedido, Repartidor repartidor) {
        if (pedido == null) throw new IllegalArgumentException("Seleccione un pedido de la tabla operativa.");
        pedido.asignarRepartidorYDespachar(repartidor);
    }

    @Override
    public void confirmarEntregaPedido(Pedido pedido, boolean recaudoConfirmado, String notaCliente) {
        if (pedido == null) throw new IllegalArgumentException("Seleccione un pedido de la tabla operativa.");
        pedido.confirmarEntregaYCerrar(recaudoConfirmado, notaCliente);
    }

    @Override
    public void registrarIncidenciaPedido(Pedido pedido, String tipoNovedad, String detalle) {
        if (pedido == null) throw new IllegalArgumentException("Seleccione un pedido de la tabla operativa.");
        pedido.reportarNovedadOIncidencia(tipoNovedad, detalle);
    }

    @Override
    public File generarYAbrirGuiaDespacho(Pedido pedido) {
        File guia = reciboPdfService.generarGuiaDespachoPdf(pedido);
        abrirArchivoPdf(guia);
        return guia;
    }

    @Override public Pedido getPedidoActual() { return pedidoActual; }
    @Override public CatalogoMenu getCatalogoMenu() { return catalogoMenu; }
    @Override public List<Modificador> getModificadoresDisponibles() { return modificadoresDisponibles; }
    @Override public ObservableList<Cliente> getClientesRegistrados() { return clientesRegistrados; }
    @Override public File getUltimoReciboPdf() { return ultimoReciboPdf; }
    @Override public ObservableList<Pedido> getPedidosOperativos() { return pedidosOperativos; }
    @Override public ObservableList<Repartidor> getRepartidores() { return repartidores; }
}
