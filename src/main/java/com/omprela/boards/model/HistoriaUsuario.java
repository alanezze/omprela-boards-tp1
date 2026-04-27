package com.omprela.boards.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Historia de Usuario (ticket).
 * Es la unidad de valor que entrega el sistema y la que circula por el tablero
 * Kanban en estados POR_HACER -> EN_PROGRESO -> EN_REVISION -> HECHO.
 */
public class HistoriaUsuario {

    public enum Estado { POR_HACER, EN_PROGRESO, EN_REVISION, HECHO, CANCELADA }

    private Integer idHistoria;
    private String titulo;
    private String descripcion;
    private String criteriosAceptacion;
    private Integer storyPoints;
    private Integer prioridad;
    private Estado estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private Integer idEpica;
    private Integer idSprint;
    private Integer idUsuarioAsignado;

    public HistoriaUsuario() { }

    public Integer getIdHistoria() { return idHistoria; }
    public void setIdHistoria(Integer idHistoria) { this.idHistoria = idHistoria; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCriteriosAceptacion() { return criteriosAceptacion; }
    public void setCriteriosAceptacion(String criteriosAceptacion) { this.criteriosAceptacion = criteriosAceptacion; }

    public Integer getStoryPoints() { return storyPoints; }
    public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }

    public Integer getPrioridad() { return prioridad; }
    public void setPrioridad(Integer prioridad) { this.prioridad = prioridad; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public Integer getIdEpica() { return idEpica; }
    public void setIdEpica(Integer idEpica) { this.idEpica = idEpica; }

    public Integer getIdSprint() { return idSprint; }
    public void setIdSprint(Integer idSprint) { this.idSprint = idSprint; }

    public Integer getIdUsuarioAsignado() { return idUsuarioAsignado; }
    public void setIdUsuarioAsignado(Integer idUsuarioAsignado) { this.idUsuarioAsignado = idUsuarioAsignado; }

    /**
     * Verifica si una transicion de estado es valida segun el flujo de trabajo.
     * Reglas: solo se puede avanzar al siguiente estado o regresar a EN_PROGRESO desde EN_REVISION.
     */
    public boolean puedeTransicionarA(Estado destino) {
        if (this.estado == Estado.CANCELADA || this.estado == Estado.HECHO) return false;
        switch (this.estado) {
            case POR_HACER:   return destino == Estado.EN_PROGRESO || destino == Estado.CANCELADA;
            case EN_PROGRESO: return destino == Estado.EN_REVISION || destino == Estado.CANCELADA;
            case EN_REVISION: return destino == Estado.HECHO || destino == Estado.EN_PROGRESO;
            default: return false;
        }
    }

    @Override
    public String toString() {
        return String.format("[H%d] %-40s | %-12s | sp:%s | prio:%d",
            idHistoria, titulo, estado, storyPoints, prioridad);
    }
}
