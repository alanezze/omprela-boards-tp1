package com.omprela.boards.model;

import com.omprela.boards.model.excepciones.TransicionEstadoInvalidaException;
import com.omprela.boards.model.excepciones.ValidacionException;
import com.omprela.boards.model.interfaces.Auditable;
import com.omprela.boards.model.interfaces.Priorizable;

import java.time.LocalDateTime;

/**
 * Clase abstracta base de la jerarquía de tickets del sistema OMPRELA-Boards.
 * <p>
 * Representa la noción común de "trabajo a realizar" que comparten las historias
 * de usuario (visibles por el equipo de Producto) y las tareas técnicas (visibles
 * por el equipo de Desarrollo). Aplica los cuatro pilares de la POO:
 * <ul>
 *   <li><b>Abstracción</b>: define qué es un ticket sin imponer detalles específicos.
 *       Los métodos {@code getTipo()} y {@code calcularEsfuerzo()} son abstractos.</li>
 *   <li><b>Encapsulamiento</b>: todos los atributos son privados y se exponen mediante
 *       getters/setters con validaciones de negocio.</li>
 *   <li><b>Herencia</b>: HistoriaUsuario y Tarea extienden de esta clase, reutilizando
 *       la lógica común de transiciones de estado.</li>
 *   <li><b>Polimorfismo</b>: el método {@code cambiarEstado(Estado)} aplica las mismas
 *       reglas de transición a todas las subclases, pero cada una puede personalizar
 *       el cálculo del esfuerzo.</li>
 * </ul>
 * <p>
 * Implementa además las interfaces {@link Auditable} y {@link Priorizable}, lo que
 * permite que cualquier subclase sea procesada por los servicios de auditoría y los
 * algoritmos de ordenación sin acoplarse al tipo concreto.
 */
public abstract class Ticket implements Auditable, Priorizable {

    /**
     * Estados posibles del flujo de trabajo Kanban.
     * Aplicación de enums como tipos seguros (vs Strings mágicos).
     */
    public enum Estado {
        POR_HACER, EN_PROGRESO, EN_REVISION, HECHO, CANCELADA
    }

    // --- Atributos encapsulados (privados) ---
    private Integer id;
    private String titulo;
    private String descripcion;
    private Integer prioridad;
    private Estado estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private Integer idUsuarioCreador;
    private Integer idUsuarioAsignado;

    /**
     * Constructor protegido: solo las subclases pueden instanciar tickets.
     * Inicializa los valores por defecto (estado POR_HACER, fechaCreacion = ahora).
     */
    protected Ticket(String titulo, Integer prioridad, Integer idUsuarioCreador) {
        validarTitulo(titulo);
        validarPrioridad(prioridad);
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.idUsuarioCreador = idUsuarioCreador;
        this.estado = Estado.POR_HACER;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    /** Constructor sin argumentos para los DAOs (recuperación desde MySQL). */
    protected Ticket() { }

    // ============== MÉTODOS ABSTRACTOS (polimorfismo) ==============

    /**
     * Cada subclase debe identificarse con un tipo único.
     * Ejemplos: "HISTORIA_USUARIO", "TAREA_TECNICA".
     */
    public abstract String getTipo();

    /**
     * Cada subclase calcula su esfuerzo de manera distinta:
     * - HistoriaUsuario usa story points (Fibonacci 1-2-3-5-8-13...).
     * - Tarea usa horas estimadas.
     */
    public abstract double calcularEsfuerzo();

    // ============== LÓGICA COMÚN (reutilizada por herencia) ==============

    /**
     * Cambia el estado del ticket validando que la transición sea legal.
     * <p>
     * Flujo permitido:
     * POR_HACER → EN_PROGRESO → EN_REVISION → HECHO
     * Adicionalmente:
     * - desde EN_REVISION se puede volver a EN_PROGRESO (rechazo de revisión).
     * - desde cualquier estado activo se puede CANCELAR.
     * - HECHO y CANCELADA son estados terminales.
     */
    public void cambiarEstado(Estado nuevoEstado) {
        if (!puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                this.estado.name(), nuevoEstado.name());
        }
        this.estado = nuevoEstado;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    /**
     * Indica si una transición de estado es válida desde el estado actual.
     * Método público para que el frontend pueda mostrar/ocultar acciones.
     */
    public boolean puedeTransicionarA(Estado destino) {
        if (destino == null) return false;
        if (this.estado == Estado.HECHO || this.estado == Estado.CANCELADA) return false;
        if (destino == Estado.CANCELADA && this.estado != Estado.HECHO) return true;
        switch (this.estado) {
            case POR_HACER:   return destino == Estado.EN_PROGRESO;
            case EN_PROGRESO: return destino == Estado.EN_REVISION;
            case EN_REVISION: return destino == Estado.HECHO || destino == Estado.EN_PROGRESO;
            default: return false;
        }
    }

    // ============== VALIDACIONES (encapsuladas) ==============

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ValidacionException("titulo", "el título es obligatorio");
        }
        if (titulo.length() > 150) {
            throw new ValidacionException("titulo", "el título no puede superar los 150 caracteres");
        }
    }

    private static void validarPrioridad(Integer prioridad) {
        if (prioridad == null || prioridad < 1 || prioridad > 5) {
            throw new ValidacionException("prioridad", "la prioridad debe estar entre 1 (máx) y 5 (mín)");
        }
    }

    // ============== GETTERS Y SETTERS (encapsulamiento) ==============

    @Override public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) {
        validarTitulo(titulo);
        this.titulo = titulo;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override public Integer getPrioridad() { return prioridad; }
    public void setPrioridad(Integer prioridad) {
        validarPrioridad(prioridad);
        this.prioridad = prioridad;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    @Override public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fc) { this.fechaCreacion = fc; }

    @Override public LocalDateTime getFechaUltimaModificacion() { return fechaUltimaModificacion; }
    public void setFechaUltimaModificacion(LocalDateTime f) { this.fechaUltimaModificacion = f; }

    @Override public Integer getIdUsuarioCreador() { return idUsuarioCreador; }
    public void setIdUsuarioCreador(Integer id) { this.idUsuarioCreador = id; }

    public Integer getIdUsuarioAsignado() { return idUsuarioAsignado; }
    public void setIdUsuarioAsignado(Integer id) { this.idUsuarioAsignado = id; }

    /** Por defecto, el nombre de entidad es el del tipo runtime. Las subclases pueden sobrescribir. */
    @Override
    public String getNombreEntidad() {
        return this.getClass().getSimpleName();
    }

    /**
     * Representación textual genérica - polimorfismo.
     * Cada subclase puede sobrescribirla para incluir información adicional.
     */
    @Override
    public String toString() {
        return String.format("[%s #%d] %s | prio:%d | %s",
            getTipo(), id, titulo, prioridad, estado);
    }
}
