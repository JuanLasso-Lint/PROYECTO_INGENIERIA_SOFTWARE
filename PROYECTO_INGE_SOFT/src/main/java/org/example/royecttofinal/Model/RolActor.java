package org.example.royecttofinal.Model;

public enum RolActor {
    CONSUMIDOR(
            "Consumidor",
            "Autogestión cliente: pedido en línea/contra entrega (máx. $60), cupón PROMO10. Sin acceso a CRUD ni KDS.",
            false, false, false, true
    ),
    AUXILIAR_REGISTRO(
            "Auxiliar de Registro",
            "Call Center / Mesero: registra clientes, gestiona CRUD y KDS, pedidos Transferencia/Contra Entrega. Sin cupón VIP20 ni Tarjeta Caja.",
            true, true, false, false
    ),
    CAJERO(
            "Cajero",
            "Control Total POS: todos los medios de pago, cupones VIP20/PROMO10, Facturación Electrónica, CRUD y KDS.",
            true, true, true, true
    );

    private final String etiqueta;
    private final String descripcionPermisos;
    private final boolean puedeGestionarCrud;
    private final boolean puedeSimularKds;
    private final boolean puedeAplicarCuponVip;
    private final boolean puedeCobrarTarjeta;

    RolActor(String etiqueta, String descripcionPermisos,
             boolean puedeGestionarCrud, boolean puedeSimularKds,
             boolean puedeAplicarCuponVip, boolean puedeCobrarTarjeta) {
        this.etiqueta = etiqueta;
        this.descripcionPermisos = descripcionPermisos;
        this.puedeGestionarCrud = puedeGestionarCrud;
        this.puedeSimularKds = puedeSimularKds;
        this.puedeAplicarCuponVip = puedeAplicarCuponVip;
        this.puedeCobrarTarjeta = puedeCobrarTarjeta;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getDescripcionPermisos() { return descripcionPermisos; }
    public boolean isPuedeGestionarCrud() { return puedeGestionarCrud; }
    public boolean isPuedeSimularKds() { return puedeSimularKds; }
    public boolean isPuedeAplicarCuponVip() { return puedeAplicarCuponVip; }
    public boolean isPuedeCobrarTarjeta() { return puedeCobrarTarjeta; }

    @Override
    public String toString() { return etiqueta; }
}
