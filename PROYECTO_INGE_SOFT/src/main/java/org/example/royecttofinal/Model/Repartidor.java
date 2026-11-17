package org.example.royecttofinal.Model;

import javafx.beans.property.*;

public class Repartidor {
    private final StringProperty idRepartidor = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty vehiculoPlaca = new SimpleStringProperty();
    private final BooleanProperty disponible = new SimpleBooleanProperty(true);
    private final IntegerProperty pedidosActivos = new SimpleIntegerProperty(0);

    public Repartidor(String idRepartidor, String nombre, String telefono, String vehiculoPlaca) {
        this.idRepartidor.set(idRepartidor);
        this.nombre.set(nombre);
        this.telefono.set(telefono);
        this.vehiculoPlaca.set(vehiculoPlaca);
    }

    public void asignarPedido() {
        pedidosActivos.set(pedidosActivos.get() + 1);
        if (pedidosActivos.get() >= 2) {
            disponible.set(false);
        }
    }

    public void liberarPedido() {
        if (pedidosActivos.get() > 0) {
            pedidosActivos.set(pedidosActivos.get() - 1);
        }
        disponible.set(true);
    }

    public String getIdRepartidor() { return idRepartidor.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getVehiculoPlaca() { return vehiculoPlaca.get(); }
    public boolean isDisponible() { return disponible.get(); }
    public void setDisponible(boolean val) { disponible.set(val); }
    public int getPedidosActivos() { return pedidosActivos.get(); }

    public StringProperty idRepartidorProperty() { return idRepartidor; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty telefonoProperty() { return telefono; }
    public StringProperty vehiculoPlacaProperty() { return vehiculoPlaca; }
    public BooleanProperty disponibleProperty() { return disponible; }
    public IntegerProperty pedidosActivosProperty() { return pedidosActivos; }

    @Override
    public String toString() {
        String estadoTxt = isDisponible() ? "Disponible" : "Ocupado";
        return String.format("[%s] %s — %s (%s | Activos: %d)",
                getIdRepartidor(), getNombre(), getVehiculoPlaca(), estadoTxt, getPedidosActivos());
    }
}
