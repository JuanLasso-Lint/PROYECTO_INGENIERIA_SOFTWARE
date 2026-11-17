package org.example.royecttofinal.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Modificador {
    private final StringProperty idModificador = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final DoubleProperty precioAdicional = new SimpleDoubleProperty();

    public Modificador(String idModificador, String nombre, double precioAdicional) {
        this.idModificador.set(idModificador);
        this.nombre.set(nombre);
        this.precioAdicional.set(precioAdicional);
    }

    public String getIdModificador() { return idModificador.get(); }
    public String getNombre() { return nombre.get(); }
    public double getPrecioAdicional() { return precioAdicional.get(); }

    @Override
    public String toString() {
        return String.format("%s (+$%.2f)", getNombre(), getPrecioAdicional());
    }
}