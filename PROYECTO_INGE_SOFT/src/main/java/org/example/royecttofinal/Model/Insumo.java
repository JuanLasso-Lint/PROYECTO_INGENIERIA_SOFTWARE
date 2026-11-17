package org.example.royecttofinal.Model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Insumo {
    private final StringProperty idInsumo = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final BooleanProperty disponibleKDS = new SimpleBooleanProperty(true);

    public Insumo(String idInsumo, String nombre, boolean disponibleKDS) {
        this.idInsumo.set(idInsumo);
        this.nombre.set(nombre);
        this.disponibleKDS.set(disponibleKDS);
    }

    public String getIdInsumo() { return idInsumo.get(); }
    public String getNombre() { return nombre.get(); }
    public boolean isDisponibleKDS() { return disponibleKDS.get(); }
    public void setDisponibleKDS(boolean value) { disponibleKDS.set(value); }
    public BooleanProperty disponibleKDSProperty() { return disponibleKDS; }
}