package com.omprela.boards.model.excepciones;

/**
 * Se lanza cuando los datos de entrada no cumplen las reglas de validación
 * del dominio (campos obligatorios vacíos, valores fuera de rango, etc.).
 */
public class ValidacionException extends OmprelaException {

    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(String.format("Validación fallida en '%s': %s", campo, mensaje));
        this.campo = campo;
    }

    public String getCampo() { return campo; }
}
