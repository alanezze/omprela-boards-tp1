package com.omprela.boards.model;

import com.omprela.boards.model.excepciones.ValidacionException;
import com.omprela.boards.model.interfaces.Auditable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Proyecto de software gestionado en el sistema OMPRELA-Boards.
 * <p>
 * Aplica encapsulamiento completo: todos los atributos son privados y las
 * validaciones se aplican dentro de los setters. Implementa la interfaz
 * {@link Auditable} para permitir el seguimiento de auditoria centralizado.
 */
public class Proyecto implements Auditable {

    public enum Estado { ACTIVO, PAUSADO, FINALIZADO, CANCELADO }

    private Integer idProyecto;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private Estado estado;
    private BigDecimal presupuesto;
    private Integer idCliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private Integer idUsuarioCreador;

    public Proyecto(String nombre, LocalDate fechaInicio, Integer idCliente,
                    Integer idUsuarioCreador) {
        setNombre(nombre);
        setFechaInicio(fechaInicio);
        setIdCliente(idCliente);
        this.idUsuarioCreador = idUsuarioCreador;
        this.estado = Estado.ACTIVO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public Proyecto() { }

    public Integer getIdProyecto() { return idProyecto; }
    public void setIdProyecto(Integer id) { this.idProyecto = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("nombre", "el nombre del proyecto es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate f) {
        if (f == null) throw new ValidacionException("fechaInicio", "obligatoria");
        this.fechaInicio = f;
    }

    public LocalDate getFechaFinEstimada() { return fechaFinEstimada; }
    public void setFechaFinEstimada(LocalDate f) {
        if (f != null && fechaInicio != null && f.isBefore(fechaInicio)) {
            throw new ValidacionException("fechaFinEstimada",
                "no puede ser anterior a la fecha de inicio");
        }
        this.fechaFinEstimada = f;
    }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public BigDecimal getPresupuesto() { return presupuesto; }
    public void setPresupuesto(BigDecimal p) {
        if (p != null && p.signum() < 0) {
            throw new ValidacionException("presupuesto", "no puede ser negativo");
        }
        this.presupuesto = p;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer id) {
        if (id == null) throw new ValidacionException("idCliente", "obligatorio");
        this.idCliente = id;
    }

    @Override public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    @Override public LocalDateTime getFechaUltimaModificacion() { return fechaUltimaModificacion; }
    public void setFechaUltimaModificacion(LocalDateTime f) { this.fechaUltimaModificacion = f; }

    @Override public Integer getIdUsuarioCreador() { return idUsuarioCreador; }
    public void setIdUsuarioCreador(Integer id) { this.idUsuarioCreador = id; }

    @Override public String getNombreEntidad() { return "Proyecto"; }
    @Override public Integer getId() { return idProyecto; }

    @Override
    public String toString() {
        return String.format("[Proyecto #%d] %s | %s | inicio: %s",
            idProyecto, nombre, estado, fechaInicio);
    }
}
