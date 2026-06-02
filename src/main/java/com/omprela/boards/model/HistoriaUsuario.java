package com.omprela.boards.model;

import com.omprela.boards.model.excepciones.ValidacionException;
import com.omprela.boards.model.interfaces.Notificable;

import java.util.Arrays;
import java.util.List;

/**
 * Historia de usuario: pieza de valor entregable al cliente final.
 * <p>
 * <b>Herencia</b>: extiende de {@link Ticket} reutilizando todos los atributos y
 * comportamientos comunes (cambio de estado, validaciones, auditoria).
 * <p>
 * <b>Polimorfismo</b>: sobrescribe los metodos abstractos {@code getTipo()} y
 * {@code calcularEsfuerzo()}, implementando el calculo en story points segun
 * la escala Fibonacci tradicional de las metodologias agiles.
 * <p>
 * Implementa tambien {@link Notificable}, lo que permite al servicio de
 * notificaciones avisarle a los stakeholders sobre cambios relevantes.
 */
public class HistoriaUsuario extends Ticket implements Notificable {

    /** Valores validos de story points (escala Fibonacci). */
    public static final List<Integer> FIBONACCI = Arrays.asList(1, 2, 3, 5, 8, 13, 21);

    private Integer storyPoints;
    private String criteriosAceptacion;
    private Integer idEpica;
    private Integer idSprint;
    private String emailAsignado;

    public HistoriaUsuario(String titulo, Integer prioridad, Integer idUsuarioCreador,
                            Integer storyPoints, Integer idEpica) {
        super(titulo, prioridad, idUsuarioCreador);
        setStoryPoints(storyPoints);
        if (idEpica == null) {
            throw new ValidacionException("idEpica", "la historia debe pertenecer a una epica");
        }
        this.idEpica = idEpica;
    }

    public HistoriaUsuario() {
        super();
    }

    @Override
    public String getTipo() {
        return "HISTORIA_USUARIO";
    }

    @Override
    public double calcularEsfuerzo() {
        if (storyPoints == null) return 0.0;
        return storyPoints * 4.0;
    }

    @Override
    public String toString() {
        return String.format("[Historia #%d] %s | sp:%s | prio:%d | %s",
            getId(), getTitulo(), storyPoints, getPrioridad(), getEstado());
    }

    @Override
    public String getEmailDestinatario() {
        return emailAsignado;
    }

    @Override
    public String getAsuntoNotificacion() {
        return String.format("[OMPRELA-Boards] Historia '%s' actualizada", getTitulo());
    }

    @Override
    public String getMensajeNotificacion() {
        return String.format(
            "La historia #%d '%s' ha sido actualizada.%nEstado actual: %s.%nPrioridad: %d.%nStory points: %s.",
            getId(), getTitulo(), getEstado(), getPrioridad(), storyPoints);
    }

    public Integer getStoryPoints() { return storyPoints; }
    public void setStoryPoints(Integer storyPoints) {
        if (storyPoints != null && !FIBONACCI.contains(storyPoints)) {
            throw new ValidacionException("storyPoints",
                "debe ser un valor de la escala Fibonacci: " + FIBONACCI);
        }
        this.storyPoints = storyPoints;
    }

    public String getCriteriosAceptacion() { return criteriosAceptacion; }
    public void setCriteriosAceptacion(String c) { this.criteriosAceptacion = c; }

    public Integer getIdEpica() { return idEpica; }
    public void setIdEpica(Integer idEpica) { this.idEpica = idEpica; }

    public Integer getIdSprint() { return idSprint; }
    public void setIdSprint(Integer idSprint) { this.idSprint = idSprint; }

    public String getEmailAsignado() { return emailAsignado; }
    public void setEmailAsignado(String email) { this.emailAsignado = email; }
}
