package org.example.royecttofinal.Model;

public enum RestriccionAlimentaria {
    NINGUNA("Estándar / Sin Restricción"),
    VEGETARIANO("Vegetariano"),
    VEGANO("Vegano"),
    SIN_SAL("Sin Sal / Bajo en Sodio"),
    SIN_PICANTE("Sin Picante"),
    TERMINO_MEDIO("Cocción Término Medio"),
    BIEN_COCIDO("Cocción Bien Cocido");

    private final String etiqueta;
    RestriccionAlimentaria(String etiqueta) { this.etiqueta = etiqueta; }
    @Override public String toString() { return etiqueta; }
}