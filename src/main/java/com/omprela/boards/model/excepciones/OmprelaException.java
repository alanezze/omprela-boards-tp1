package com.omprela.boards.model.excepciones;

/**
 * Excepción base para errores de negocio del sistema OMPRELA-Boards.
 * <p>
 * Esta clase es la raíz de la jerarquía de excepciones de dominio. Hereda de
 * RuntimeException (excepción no comprobada) porque las reglas de negocio violadas
 * representan condiciones que no deberían ocurrir en operación normal, sino que
 * indican un error del cliente del API o un estado inválido del sistema.
 * <p>
 * Aplica el pilar de <b>herencia</b>: todas las excepciones específicas
 * (TransicionEstadoInvalidaException, EntidadNoEncontradaException, etc.) heredan
 * de esta clase, permitiendo capturarlas de manera genérica cuando es necesario.
 */
public class OmprelaException extends RuntimeException {

    public OmprelaException(String mensaje) {
        super(mensaje);
    }

    public OmprelaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
