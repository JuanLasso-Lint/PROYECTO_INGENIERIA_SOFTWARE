package org.example.royecttofinal.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ComandaDigital {
    private final String idComanda;
    private final String idPedido;
    private final LocalDateTime horaEmision;
    private final EstadoPedido condicionFinanciera;
    private final RolActor operadorRegistro;
    private final List<DetallePedido> items;

    public ComandaDigital(Pedido pedido, RolActor operador) {
        if (pedido.getEstado() != EstadoPedido.PAGO_CONFIRMADO && pedido.getEstado() != EstadoPedido.PENDIENTE_RECAUDO) {
            throw new IllegalStateException("RN-06: La comanda KDS solo se emite con Pago Confirmado o Pendiente de Recaudo.");
        }
        this.idComanda = "CMD-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.idPedido = pedido.getIdPedido();
        this.horaEmision = LocalDateTime.now();
        this.condicionFinanciera = pedido.getEstado();
        this.operadorRegistro = operador;
        this.items = List.copyOf(pedido.getItems());
    }

    public String getIdComanda() { return idComanda; }
    public String getIdPedido() { return idPedido; }
    public LocalDateTime getHoraEmision() { return horaEmision; }
    public EstadoPedido getCondicionFinanciera() { return condicionFinanciera; }
    public RolActor getOperadorRegistro() { return operadorRegistro; }
    public List<DetallePedido> getItems() { return items; }
}