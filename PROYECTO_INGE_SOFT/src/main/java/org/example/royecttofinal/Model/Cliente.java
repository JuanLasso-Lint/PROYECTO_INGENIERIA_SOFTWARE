package org.example.royecttofinal.Model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {
    private final StringProperty idCliente = new SimpleStringProperty();
    private final StringProperty documento = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final ObjectProperty<Direccion> direccion = new SimpleObjectProperty<>();

    public Cliente(String idCliente, String documento, String nombre, String telefono, Direccion direccion) {
        this.idCliente.set(idCliente);
        this.documento.set(documento);
        this.nombre.set(nombre);
        this.telefono.set(telefono);
        this.direccion.set(direccion);
    }

    public String getIdCliente() { return idCliente.get(); }
    public String getDocumento() { return documento.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public Direccion getDireccion() { return direccion.get(); }
    public void setDireccion(Direccion value) { direccion.set(value); }

    @Override
    public String toString() {
        return String.format("%s — %s (%s)", getDocumento(), getNombre(), getDireccion());
    }
}