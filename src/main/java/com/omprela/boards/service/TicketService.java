package com.omprela.boards.service;

import com.omprela.boards.algoritmos.AlgoritmosTickets;
import com.omprela.boards.algoritmos.Pila;
import com.omprela.boards.dao.TicketDAO;
import com.omprela.boards.model.Ticket;
import com.omprela.boards.model.Ticket.Estado;
import com.omprela.boards.model.excepciones.EntidadNoEncontradaException;

import java.util.List;

/**
 * Servicio de aplicacion que opera sobre Tickets de manera polimorfica,
 * delegando la persistencia en MySQL al {@link TicketDAO}.
 * <p>
 * El parametro de tipo de varios metodos es {@link Ticket} (la clase abstracta
 * padre), no las clases concretas. Esto permite que el servicio procese historias
 * de usuario y tareas tecnicas sin saber cual es cual en tiempo de compilacion.
 * <p>
 * Mantiene ademas una pila de los ultimos movimientos para permitir el "deshacer".
 */
public class TicketService {

    private final TicketDAO dao = new TicketDAO();
    private final Pila<MovimientoTicket> historial = new Pila<>();

    // ============== CREACION POLIMORFICA ==============

    /**
     * Crea (persiste) un ticket en MySQL asignandole el id autogenerado por la base.
     * Acepta cualquier subclase de Ticket (HistoriaUsuario o Tarea) - polimorfismo.
     */
    public <T extends Ticket> T crear(T ticket) {
        dao.insertar(ticket);  // asigna el id devuelto por MySQL
        return ticket;
    }

    // ============== CAMBIO DE ESTADO ==============

    /**
     * Mueve un ticket a un nuevo estado, validando la transicion en el dominio
     * y persistiendo el cambio + el registro de auditoria en MySQL.
     */
    public void moverEstado(int idTicket, Estado nuevoEstado) {
        Ticket t = buscarPorId(idTicket);
        Estado anterior = t.getEstado();
        t.cambiarEstado(nuevoEstado);                 // valida (puede lanzar excepcion)
        dao.actualizarEstado(idTicket, anterior, nuevoEstado);  // persiste + audita
        historial.push(new MovimientoTicket(idTicket, anterior, nuevoEstado));
    }

    /**
     * Deshace el ultimo movimiento registrado, revirtiendo el estado en MySQL.
     */
    public boolean deshacerUltimoMovimiento() {
        if (historial.isEmpty()) return false;
        MovimientoTicket m = historial.pop();
        // Revierte directamente en la base (sin validar la transicion inversa)
        dao.actualizarEstado(m.idTicket, m.estadoNuevo, m.estadoAnterior);
        return true;
    }

    // ============== CONSULTAS ==============

    /** Busca un ticket por id; lanza excepcion si no existe. */
    public Ticket buscarPorId(int id) {
        Ticket t = dao.buscarPorId(id);
        if (t == null) throw new EntidadNoEncontradaException("Ticket", id);
        return t;
    }

    /** Lista todos los tickets ordenados por prioridad (quicksort sobre lo traido de MySQL). */
    public List<Ticket> listarOrdenadoPorPrioridad() {
        return AlgoritmosTickets.quicksortPorPrioridad(dao.listarTodos());
    }

    /** Filtra los tickets que estan en un determinado estado. */
    public List<Ticket> filtrarPorEstado(Estado estado) {
        List<Ticket> resultado = new java.util.ArrayList<>();
        for (Ticket t : dao.listarTodos()) {
            if (t.getEstado() == estado) resultado.add(t);
        }
        return resultado;
    }

    // ============== ESFUERZO TOTAL (polimorfismo) ==============

    /**
     * Calcula el esfuerzo total sumando los esfuerzos individuales. Cada ticket
     * reporta su propio esfuerzo segun su tipo gracias al polimorfismo.
     */
    public double calcularEsfuerzoTotal() {
        double total = 0;
        for (Ticket t : dao.listarTodos()) {
            total += t.calcularEsfuerzo();
        }
        return total;
    }

    public int tickets() { return dao.listarTodos().size(); }
    public int historialDisponible() { return historial.size(); }

    /** DTO interno para representar un movimiento historico. */
    private static class MovimientoTicket {
        final int idTicket;
        final Estado estadoAnterior;
        final Estado estadoNuevo;
        MovimientoTicket(int idTicket, Estado anterior, Estado nuevo) {
            this.idTicket = idTicket;
            this.estadoAnterior = anterior;
            this.estadoNuevo = nuevo;
        }
    }
}
