package org.example.royecttofinal.Model;

public enum EstadoPedido {
    PENDIENTE_COBERTURA("Pendiente de Cobertura"),
    EN_SELECCION("En Selección de Platos"),
    LIQUIDADO("Cuenta Liquidada"),
    PAGO_CONFIRMADO("Pago Confirmado"),
    PENDIENTE_RECAUDO("Pendiente de Recaudo (Contra Entrega)"),
    ENVIADO_A_COCINA_KDS("Recibido en Cocina / Bodega (KDS)"),
    EN_PREPARACION("En Preparación y Verificación"),
    EMPACADO_LISTO_DESPACHO("Empacado y Listo para Despacho"),
    EN_CAMINO_DESPACHADO("Despachado / En Camino"),
    ENTREGADO_Y_CERRADO("Entregado y Cerrado"),
    CON_INCIDENCIA("Con Novedad / Incidencia");

    private final String etiqueta;
    EstadoPedido(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
