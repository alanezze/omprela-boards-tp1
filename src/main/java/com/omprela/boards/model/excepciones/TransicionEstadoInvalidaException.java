package com.omprela.boards.model.excepciones;

/**
 * Se lanza cuando se intenta mover un ticket a un estado que no es alcanzable
 * desde su estado actual según las reglas del flujo de trabajo Kanban.
 * <p>
 * Por ejemplo: intentar pasar una historia directamente de POR_HACER a HECHO
 * sin haber pasado por EN_PROGRESO y EN_REVISION dispara esta excepción.
 */
public class TransicionEstadoInvalidaException extends OmprelaException {

    private final String estadoActual;
    private final String estadoDestino;

    public TransicionEstadoInvalidaException(String estadoActual, String estadoDestino) {
        super(String.format("Transición inválida: no se puede pasar de %s a %s",
                estadoActual, estadoDestino));
        this.estadoActual = estadoActual;
        this.estadoDestino = estadoDestino;
    }

    public String getEstadoActual() { return estadoActual; }
    public String getEstadoDestino() { return estadoDestino; }
}
