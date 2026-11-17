package org.example.royecttofinal.Model;

import java.util.UUID;

public class PagoTransferencia implements MetodoPago {
    @Override
    public ResultadoPago procesarPago(double monto) {
        String ref = "TXN-TRF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new ResultadoPago(true, ref, "Transferencia rápida aprobada.", false);
    }
    @Override public String getNombreModalidad() { return "Transferencia Rápida"; }
}