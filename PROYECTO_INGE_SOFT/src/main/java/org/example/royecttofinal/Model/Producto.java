package org.example.royecttofinal.Model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Arrays;

public class Producto {
    private final StringProperty idProducto = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty categoria = new SimpleStringProperty();
    private final DoubleProperty precioBase = new SimpleDoubleProperty();
    private final BooleanProperty habilitadoEnCocina = new SimpleBooleanProperty(true);
    private final ObservableList<Insumo> ingredientesBase = FXCollections.observableArrayList();

    public Producto(String idProducto, String nombre, String categoria, double precioBase, Insumo... insumosBase) {
        this.idProducto.set(idProducto);
        this.nombre.set(nombre);
        this.categoria.set(categoria);
        this.precioBase.set(precioBase);
        if (insumosBase != null) {
            this.ingredientesBase.addAll(Arrays.asList(insumosBase));
        }
    }

    public boolean isDisponibleKDS() {
        if (!habilitadoEnCocina.get()) return false;
        return ingredientesBase.stream().allMatch(Insumo::isDisponibleKDS);
    }

    public StringProperty idProductoProperty() { return idProducto; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty categoriaProperty() { return categoria; }
    public DoubleProperty precioBaseProperty() { return precioBase; }
    public BooleanProperty habilitadoEnCocinaProperty() { return habilitadoEnCocina; }

    public String getIdProducto() { return idProducto.get(); }
    public String getNombre() { return nombre.get(); }
    public void setNombre(String value) { nombre.set(value); }
    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String value) { categoria.set(value); }
    public double getPrecioBase() { return precioBase.get(); }
    public void setPrecioBase(double value) { precioBase.set(value); }
    public void setHabilitadoEnCocina(boolean value) { habilitadoEnCocina.set(value); }
    public ObservableList<Insumo> getIngredientesBase() { return ingredientesBase; }

    @Override
    public String toString() {
        String estado = isDisponibleKDS() ? "" : " [AGOTADO EN KDS]";
        return String.format("[%s] %s (%s) — $%.2f%s", getIdProducto(), getNombre(), getCategoria(), getPrecioBase(), estado);
    }
}