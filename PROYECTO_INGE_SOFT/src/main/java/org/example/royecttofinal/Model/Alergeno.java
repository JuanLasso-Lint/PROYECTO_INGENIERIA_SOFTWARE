package org.example.royecttofinal.Model;

public enum Alergeno {
    NINGUNO("Sin Alérgenos"),
    GLUTEN("Gluten"),
    LACTEOS("Lácteos"),
    FRUTOS_SECOS("Frutos Secos"),
    MARISCOS("Mariscos"),
    HUEVO("Huevo"),
    SOYA("Soya");

    private final String etiqueta;
    Alergeno(String etiqueta) { this.etiqueta = etiqueta; }
    @Override public String toString() { return etiqueta; }
}