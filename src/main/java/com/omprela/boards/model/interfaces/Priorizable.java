package com.omprela.boards.model.interfaces;

/**
 * Contrato que deben cumplir las entidades que se ordenan por prioridad
 * (épicas, historias de usuario, tareas) y participan del backlog priorizado.
 * <p>
 * Permite que el AlgoritmoOrdenacion procese cualquier lista heterogénea de
 * priorizables de manera <b>polimórfica</b>, sin conocer el tipo concreto.
 */
public interface Priorizable {

    /** Retorna la prioridad numérica de 1 (máxima) a 5 (mínima). */
    Integer getPrioridad();

    /** Retorna un identificador único para desempatar elementos con igual prioridad. */
    Integer getId();
}
