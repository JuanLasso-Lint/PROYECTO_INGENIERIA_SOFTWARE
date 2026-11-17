package org.example.royecttofinal.Model;

public class PagoEfectivoContraEntrega implements MetodoPago {
    @Override
    public ResultadoPago procesarPago(double monto) {
        return new ResultadoPago(true, "COD-CONTRA-ENTREGA", "Acuerdo de recaudo contra entrega registrado.", true);
    }
    @Override public String getNombreModalidad() { return "Pago Contra Entrega"; }
}