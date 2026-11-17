package org.example.royecttofinal.Model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import java.util.List;

public class DetallePedido {
    public static final int MAX_MODIFICADORES = 5;

    private final ObjectProperty<Producto> producto = new SimpleObjectProperty<>();
    private final IntegerProperty cantidad = new SimpleIntegerProperty(1);
    private final ObservableList<Modificador> modificadores = FXCollections.observableArrayList();
    private final ObservableList<Alergeno> alergenos = FXCollections.observableArrayList();
    private final ObjectProperty<RestriccionAlimentaria> restriccion = new SimpleObjectProperty<>(RestriccionAlimentaria.NINGUNA);
    private final StringProperty observaciones = new SimpleStringProperty("");
    private final DoubleProperty subtotal = new SimpleDoubleProperty(0.0);

    public DetallePedido(Producto producto, int cantidad) {
        if (producto == null || !producto.isDisponibleKDS()) {
            throw new IllegalArgumentException("RN-01: El plato seleccionado está agotado o deshabilitado en Cocina KDS.");
        }
        if (cantidad < 1) {
            throw new IllegalArgumentException("RN-05: La cantidad mínima por ítem es 1 unidad.");
        }
        this.producto.set(producto);
        this.cantidad.set(cantidad);

        this.cantidad.addListener((obs, o, n) -> recalcularSubtotal());
        this.modificadores.addListener((ListChangeListener<Modificador>) c -> recalcularSubtotal());
        recalcularSubtotal();
    }

    public boolean agregarModificador(Modificador mod) {
        if (modificadores.size() >= MAX_MODIFICADORES) {
            return false;
        }
        modificadores.add(mod);
        return true;
    }

    public void asignarAlergenos(List<Alergeno> listaAlergenos) {
        alergenos.setAll(listaAlergenos);
    }

    public final void recalcularSubtotal() {
        double extras = modificadores.stream().mapToDouble(Modificador::getPrecioAdicional).sum();
        subtotal.set((producto.get().getPrecioBase() + extras) * cantidad.get());
    }

    public Producto getProducto() { return producto.get(); }
    public int getCantidad() { return cantidad.get(); }
    public ObservableList<Modificador> getModificadores() { return modificadores; }
    public ObservableList<Alergeno> getAlergenos() { return alergenos; }
    public RestriccionAlimentaria getRestriccion() { return restriccion.get(); }
    public void setRestriccion(RestriccionAlimentaria val) { restriccion.set(val); }
    public String getObservaciones() { return observaciones.get(); }
    public void setObservaciones(String val) { observaciones.set(val != null ? val : ""); }
    public double getSubtotal() { return subtotal.get(); }
    public ReadOnlyDoubleProperty subtotalProperty() { return subtotal; }
}