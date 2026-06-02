package com.omprela.boards.algoritmos;

import java.util.EmptyStackException;

/**
 * Implementación propia de una pila (LIFO) basada en una lista enlazada interna.
 * <p>
 * Es genérica ({@code Pila<T>}), lo que permite reutilizarla con cualquier tipo.
 * Se utiliza en el sistema para almacenar las últimas N operaciones realizadas
 * por el usuario y permitir su deshacer manual (Ctrl+Z) sobre el tablero Kanban.
 * <p>
 * Aplica los pilares de POO: encapsulamiento (la lista interna es privada),
 * abstracción (el cliente solo ve push / pop / peek / isEmpty) y polimorfismo
 * paramétrico via genéricos.
 */
public class Pila<T> {

    /** Nodo interno de la pila. Encapsulado como clase estática anidada. */
    private static class Nodo<E> {
        E valor;
        Nodo<E> siguiente;
        Nodo(E valor, Nodo<E> siguiente) {
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Nodo<T> tope;
    private int tamanio;

    public Pila() {
        this.tope = null;
        this.tamanio = 0;
    }

    /** Apila un elemento en la cima. Complejidad O(1). */
    public void push(T valor) {
        tope = new Nodo<>(valor, tope);
        tamanio++;
    }

    /** Devuelve y remueve el elemento del tope. Complejidad O(1). */
    public T pop() {
        if (tope == null) throw new EmptyStackException();
        T valor = tope.valor;
        tope = tope.siguiente;
        tamanio--;
        return valor;
    }

    /** Devuelve el elemento del tope sin removerlo. Complejidad O(1). */
    public T peek() {
        if (tope == null) throw new EmptyStackException();
        return tope.valor;
    }

    public boolean isEmpty() {
        return tope == null;
    }

    public int size() {
        return tamanio;
    }

    public void clear() {
        tope = null;
        tamanio = 0;
    }
}
