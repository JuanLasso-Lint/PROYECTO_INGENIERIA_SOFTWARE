package org.example.royecttofinal.Model;

import java.time.LocalDate;

public class CuponDescuento {
    private final String codigo;
    private final double porcentajeDescuento;
    private final double montoMinimo;
    private final LocalDate fechaVencimiento;
    private boolean activo;

    public CuponDescuento(String codigo, double porcentajeDescuento, double montoMinimo, LocalDate fechaVencimiento) {
        this.codigo = codigo.trim().toUpperCase();
        this.porcentajeDescuento = porcentajeDescuento;
        this.montoMinimo = montoMinimo;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = true;
    }

    public boolean esValido(double subtotal) {
        boolean vigente = fechaVencimiento == null || !LocalDate.now().isAfter(fechaVencimiento);
        return activo && vigente && subtotal >= montoMinimo;
    }

    public double calcularDescuento(double subtotal) {
        return esValido(subtotal) ? subtotal * (porcentajeDescuento / 100.0) : 0.0;
    }

    public String getCodigo() { return codigo; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public double getMontoMinimo() { return montoMinimo; }
}