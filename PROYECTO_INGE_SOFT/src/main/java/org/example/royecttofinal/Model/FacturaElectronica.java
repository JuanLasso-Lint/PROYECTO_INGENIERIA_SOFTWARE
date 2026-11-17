package org.example.royecttofinal.Model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FacturaElectronica {
    private final String numeroFactura;
    private final String cufe;
    private final LocalDateTime fechaEmision;
    private final Pedido pedido;
    private final String referenciaPago;

    public FacturaElectronica(Pedido pedido, String referenciaPago) {
        if (pedido.getEstado() != EstadoPedido.PAGO_CONFIRMADO) {
            throw new IllegalStateException("RN-04: No se puede emitir factura electrónica sin pago aprobado.");
        }
        this.numeroFactura = "FE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.cufe = UUID.randomUUID().toString().replace("-", "");
        this.fechaEmision = LocalDateTime.now();
        this.pedido = pedido;
        this.referenciaPago = referenciaPago;
    }

    public String getNumeroFactura() { return numeroFactura; }
    public String getCufe() { return cufe; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public double getTotalFacturado() { return pedido.getTotalFinal(); }
    public String getReferenciaPago() { return referenciaPago; }
}