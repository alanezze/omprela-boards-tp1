package com.omprela.boards.model.excepciones;

/**
 * Se lanza cuando se solicita una entidad por su identificador y no existe
 * en la base de datos.
 */
public class EntidadNoEncontradaException extends OmprelaException {

    private final String tipoEntidad;
    private final Object id;

    public EntidadNoEncontradaException(String tipoEntidad, Object id) {
        super(String.format("%s con id=%s no encontrado", tipoEntidad, id));
        this.tipoEntidad = tipoEntidad;
        this.id = id;
    }

    public String getTipoEntidad() { return tipoEntidad; }
    public Object getId() { return id; }
}
