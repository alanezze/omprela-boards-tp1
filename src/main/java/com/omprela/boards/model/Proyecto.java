package com.omprela.boards.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad de dominio Proyecto.
 * Representa un proyecto de software activo en OMPRELA, asociado a un cliente.
 */
public class Proyecto {

    public enum Estado { ACTIVO, PAUSADO, FINALIZADO, CANCELADO }

    private Integer idProyecto;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private Estado estado;
    private BigDecimal presupuesto;
    private Integer idCliente;

    public Proyecto() { }

    public Proyecto(String nombre, LocalDate fechaInicio, Integer idCliente) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.idCliente = idCliente;
        this.estado = Estado.ACTIVO;
    }

    public Integer getIdProyecto() { return idProyecto; }
    public void setIdProyecto(Integer idProyecto) { this.idProyecto = idProyecto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFinEstimada() { return fechaFinEstimada; }
    public void setFechaFinEstimada(LocalDate fechaFinEstimada) { this.fechaFinEstimada = fechaFinEstimada; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public BigDecimal getPresupuesto() { return presupuesto; }
    public void setPresupuesto(BigDecimal presupuesto) { this.presupuesto = presupuesto; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    @Override
    public String toString() {
        return String.format("[%d] %-30s | %-10s | inicio: %s",
            idProyecto, nombre, estado, fechaInicio);
    }
}
