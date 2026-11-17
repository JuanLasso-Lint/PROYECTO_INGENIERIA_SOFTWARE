package org.example.royecttofinal.Model;

import java.util.Set;

public class ZonaCobertura {
    private final String nombreZona;
    private final Set<String> barriosAutorizados;
    private final double tarifaFlete;
    private final int tiempoEstimadoMinutos;

    public ZonaCobertura(String nombreZona, Set<String> barriosAutorizados, double tarifaFlete, int tiempoEstimadoMinutos) {
        this.nombreZona = nombreZona;
        this.barriosAutorizados = barriosAutorizados;
        this.tarifaFlete = tarifaFlete;
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public boolean cubreDireccion(Direccion direccion) {
        if (direccion == null || direccion.getCalle().isBlank() || direccion.getBarrio().isBlank()) {
            return false;
        }
        String barrioNorm = direccion.getBarrio().trim().toLowerCase();
        return barriosAutorizados.stream().anyMatch(b -> b.toLowerCase().equals(barrioNorm));
    }

    public String getNombreZona() { return nombreZona; }
    public double getTarifaFlete() { return tarifaFlete; }
    public int getTiempoEstimadoMinutos() { return tiempoEstimadoMinutos; }
}