package org.example.royecttofinal.Model;

public interface PedidoObserver {
    void onCambioEstadoPedido(Pedido pedido, EstadoPedido estadoAnterior, EstadoPedido nuevoEstado);
}