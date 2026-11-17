package org.example.royecttofinal.Model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class KDSNotificationService implements PedidoObserver {
    @Override
    public void onCambioEstadoPedido(Pedido pedido, EstadoPedido estadoAnterior, EstadoPedido nuevoEstado) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        if (nuevoEstado == EstadoPedido.ENVIADO_A_COCINA_KDS && pedido.getComandaDigital() != null) {
            ComandaDigital cmd = pedido.getComandaDigital();
            System.out.println("[AUDITORÍA / KDS COCINA] Comanda " + cmd.getIdComanda()
                    + " | Pedido: " + cmd.getIdPedido()
                    + " | Operador: " + cmd.getOperadorRegistro().getEtiqueta()
                    + " | Hora: " + hora
                    + " | Condición: " + cmd.getCondicionFinanciera().getEtiqueta());
        } else if (nuevoEstado == EstadoPedido.EN_PREPARACION
                || nuevoEstado == EstadoPedido.EMPACADO_LISTO_DESPACHO
                || nuevoEstado == EstadoPedido.EN_CAMINO_DESPACHADO
                || nuevoEstado == EstadoPedido.ENTREGADO_Y_CERRADO
                || nuevoEstado == EstadoPedido.CON_INCIDENCIA) {
            System.out.println("[OPERACIONES Y LOGÍSTICA] Pedido: " + pedido.getIdPedido()
                    + " | Transición: " + estadoAnterior.getEtiqueta() + " -> " + nuevoEstado.getEtiqueta()
                    + " | Hora: " + hora);
        }
    }
}
