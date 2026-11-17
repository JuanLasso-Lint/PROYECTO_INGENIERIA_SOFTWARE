package org.example.royecttofinal.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class CatalogoMenu {
    private final ObservableList<Producto> todosLosProductos = FXCollections.observableArrayList();
    private final FilteredList<Producto> productosDisponibles = new FilteredList<>(todosLosProductos, Producto::isDisponibleKDS);

    public void registrarProducto(Producto producto) {
        todosLosProductos.add(producto);
        for (Insumo insumo : producto.getIngredientesBase()) {
            insumo.disponibleKDSProperty().addListener((obs, o, n) -> refrescarDisponibilidad());
        }
    }

    public boolean eliminarProducto(Producto producto) {
        return todosLosProductos.remove(producto);
    }

    public boolean existeId(String idProducto) {
        return todosLosProductos.stream().anyMatch(p -> p.getIdProducto().equalsIgnoreCase(idProducto.trim()));
    }

    public void refrescarDisponibilidad() {
        productosDisponibles.setPredicate(null);
        productosDisponibles.setPredicate(Producto::isDisponibleKDS);
    }

    public ObservableList<Producto> getTodosLosProductos() { return todosLosProductos; }
    public FilteredList<Producto> getProductosDisponibles() { return productosDisponibles; }
}