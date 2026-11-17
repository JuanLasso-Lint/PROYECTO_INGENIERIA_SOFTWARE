package org.example.royecttofinal.Model;

public interface MetodoPago {
    class ResultadoPago {
        public final boolean aprobado;
        public final String referenciaTransaccion;
        public final String mensaje;
        public final boolean esContraEntrega;

        public ResultadoPago(boolean aprobado, String referenciaTransaccion, String mensaje, boolean esContraEntrega) {
            this.aprobado = aprobado;
            this.referenciaTransaccion = referenciaTransaccion;
            this.mensaje = mensaje;
            this.esContraEntrega = esContraEntrega;
        }
    }

    ResultadoPago procesarPago(double monto);
    String getNombreModalidad();
}