package org.example.royecttofinal.Model;

import java.util.UUID;

public class PagoTarjeta implements MetodoPago {
    @Override
    public ResultadoPago procesarPago(double monto) {
        String ref = "TXN-CARD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new ResultadoPago(true, ref, "Pago en línea autorizado por pasarela financiera (Tarjeta).", false);
    }
    @Override public String getNombreModalidad() { return "Pago en Línea (Tarjeta Crédito/Débito)"; }
}