package org.example.royecttofinal.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Direccion {
    private final StringProperty calle = new SimpleStringProperty("");
    private final StringProperty barrio = new SimpleStringProperty("");

    public Direccion(String calle, String barrio) {
        this.calle.set(calle != null ? calle.trim() : "");
        this.barrio.set(barrio != null ? barrio.trim() : "");
    }

    public String getCalle() { return calle.get(); }
    public void setCalle(String value) { calle.set(value); }
    public String getBarrio() { return barrio.get(); }
    public void setBarrio(String value) { barrio.set(value); }

    @Override
    public String toString() {
        return getCalle() + " (" + getBarrio() + ")";
    }
}