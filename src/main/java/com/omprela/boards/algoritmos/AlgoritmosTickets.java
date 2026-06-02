package com.omprela.boards.algoritmos;

import com.omprela.boards.model.interfaces.Priorizable;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementaciones propias de algoritmos clásicos de ordenación y búsqueda,
 * aplicados a los tickets del sistema OMPRELA-Boards.
 * <p>
 * Si bien Java ofrece {@code Collections.sort()} y {@code Collections.binarySearch()},
 * estas implementaciones explícitas se incluyen para demostrar el conocimiento de los
 * algoritmos según lo solicitado por la consigna del TP3.
 * <p>
 * Las firmas usan la interfaz {@link Priorizable} (no las clases concretas), aplicando
 * el principio de programación contra abstracciones: cualquier objeto que implemente
 * Priorizable puede ser ordenado o buscado.
 */
public class AlgoritmosTickets {

    /** Constructor privado: clase utilitaria con solo métodos estáticos. */
    private AlgoritmosTickets() { }

    // ========== ORDENACIÓN ==========

    /**
     * Quicksort recursivo sobre una lista de elementos Priorizables, ordenados
     * de manera ascendente por prioridad (1 primero, 5 último).
     * Complejidad promedio: O(n log n). Peor caso: O(n²) si el pivote es siempre
     * el mínimo o el máximo.
     * <p>
     * En el contexto del sistema, se usa para mostrar el backlog priorizado.
     */
    public static <T extends Priorizable> List<T> quicksortPorPrioridad(List<T> lista) {
        if (lista == null || lista.size() <= 1) return lista;
        List<T> copia = new ArrayList<>(lista);
        quicksort(copia, 0, copia.size() - 1);
        return copia;
    }

    private static <T extends Priorizable> void quicksort(List<T> lista, int izq, int der) {
        if (izq >= der) return;
        int pivote = particionar(lista, izq, der);
        quicksort(lista, izq, pivote - 1);
        quicksort(lista, pivote + 1, der);
    }

    private static <T extends Priorizable> int particionar(List<T> lista, int izq, int der) {
        T pivote = lista.get(der);
        int i = izq - 1;
        for (int j = izq; j < der; j++) {
            if (comparar(lista.get(j), pivote) <= 0) {
                i++;
                T tmp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, tmp);
            }
        }
        T tmp = lista.get(i + 1);
        lista.set(i + 1, lista.get(der));
        lista.set(der, tmp);
        return i + 1;
    }

    /**
     * Compara dos elementos: primero por prioridad ascendente, luego por id ascendente.
     */
    private static int comparar(Priorizable a, Priorizable b) {
        int cmp = Integer.compare(a.getPrioridad(), b.getPrioridad());
        if (cmp != 0) return cmp;
        return Integer.compare(a.getId(), b.getId());
    }

    /**
     * Ordenamiento por burbuja sobre una lista de Priorizables.
     * Complejidad: O(n²). Útil para datasets pequeños y comprensible visualmente.
     * Se incluye con fines didácticos.
     */
    public static <T extends Priorizable> List<T> burbujaPorPrioridad(List<T> lista) {
        if (lista == null || lista.size() <= 1) return lista;
        List<T> copia = new ArrayList<>(lista);
        int n = copia.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (comparar(copia.get(j), copia.get(j + 1)) > 0) {
                    T tmp = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, tmp);
                }
            }
        }
        return copia;
    }

    // ========== BÚSQUEDA ==========

    /**
     * Búsqueda lineal de un Priorizable por su id.
     * Complejidad: O(n). Útil cuando la lista no está ordenada por id.
     */
    public static <T extends Priorizable> T buscarLinealPorId(List<T> lista, int idBuscado) {
        if (lista == null) return null;
        for (T elem : lista) {
            if (elem.getId() != null && elem.getId() == idBuscado) {
                return elem;
            }
        }
        return null;
    }

    /**
     * Búsqueda binaria recursiva sobre una lista YA ORDENADA POR ID ascendente.
     * Complejidad: O(log n). Requiere precondición de orden, lo cual se verifica
     * en {@link #estaOrdenadaPorId(List)}.
     */
    public static <T extends Priorizable> T buscarBinarioPorId(List<T> ordenada, int idBuscado) {
        if (ordenada == null || ordenada.isEmpty()) return null;
        if (!estaOrdenadaPorId(ordenada)) {
            throw new IllegalArgumentException(
                "La lista debe estar ordenada por id ascendente antes de la búsqueda binaria");
        }
        return buscarBinarioRecursivo(ordenada, idBuscado, 0, ordenada.size() - 1);
    }

    private static <T extends Priorizable> T buscarBinarioRecursivo(
            List<T> lista, int idBuscado, int izq, int der) {
        if (izq > der) return null;
        int medio = izq + (der - izq) / 2;
        T candidato = lista.get(medio);
        int cmp = Integer.compare(candidato.getId(), idBuscado);
        if (cmp == 0) return candidato;
        if (cmp > 0) return buscarBinarioRecursivo(lista, idBuscado, izq, medio - 1);
        return buscarBinarioRecursivo(lista, idBuscado, medio + 1, der);
    }

    private static boolean estaOrdenadaPorId(List<? extends Priorizable> lista) {
        for (int i = 1; i < lista.size(); i++) {
            if (lista.get(i - 1).getId() > lista.get(i).getId()) return false;
        }
        return true;
    }
}
