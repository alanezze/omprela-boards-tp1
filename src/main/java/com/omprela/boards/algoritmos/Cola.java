package com.omprela.boards.algoritmos;

import java.util.NoSuchElementException;

/**
 * Implementación propia de una cola (FIFO) genérica basada en lista enlazada.
 * <p>
 * Se utiliza en el sistema para encolar notificaciones de correo y procesarlas
 * de forma asíncrona en el orden en que fueron generadas, evitando bloquear el
 * hilo principal mientras se envían los emails al servicio SMTP externo.
 */
public class Cola<T> {

    private static class Nodo<E> {
        E valor;
        Nodo<E> siguiente;
        Nodo(E valor) { this.valor = valor; }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    /** Encola un elemento al final. Complejidad O(1). */
    public void encolar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (fin == null) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamanio++;
    }

    /** Desencola el elemento del frente. Complejidad O(1). */
    public T desencolar() {
        if (frente == null) throw new NoSuchElementException("Cola vacía");
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamanio--;
        return valor;
    }

    public T verFrente() {
        if (frente == null) throw new NoSuchElementException("Cola vacía");
        return frente.valor;
    }

    public boolean isEmpty() {
        return frente == null;
    }

    public int size() {
        return tamanio;
    }
}
