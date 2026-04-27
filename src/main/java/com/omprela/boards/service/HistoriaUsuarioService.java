package com.omprela.boards.service;

import com.omprela.boards.dao.HistoriaUsuarioDAO;
import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.model.HistoriaUsuario.Estado;

import java.sql.SQLException;
import java.util.List;

/**
 * Capa de servicio para la entidad Historia de Usuario.
 * Aplica las reglas del flujo de trabajo (transiciones de estado validas) y
 * encapsula la logica de negocio antes de delegar la persistencia al DAO.
 */
public class HistoriaUsuarioService {

    private final HistoriaUsuarioDAO dao = new HistoriaUsuarioDAO();

    public HistoriaUsuario crear(HistoriaUsuario h) throws SQLException {
        validar(h);
        if (h.getEstado() == null) h.setEstado(Estado.POR_HACER);
        return dao.insertar(h);
    }

    public HistoriaUsuario buscar(int idHistoria) throws SQLException {
        return dao.buscarPorId(idHistoria);
    }

    public List<HistoriaUsuario> listarPorSprint(int idSprint) throws SQLException {
        return dao.listarPorSprint(idSprint);
    }

    public List<HistoriaUsuario> listarPorEpica(int idEpica) throws SQLException {
        return dao.listarPorEpica(idEpica);
    }

    /**
     * Mueve la historia a un nuevo estado validando que la transicion sea legal.
     * Caso de uso CU10 (Mover ticket de estado).
     */
    public boolean moverEstado(int idHistoria, Estado destino, int idUsuarioOperador) throws SQLException {
        HistoriaUsuario actual = dao.buscarPorId(idHistoria);
        if (actual == null) {
            throw new IllegalArgumentException("La historia no existe: " + idHistoria);
        }
        if (!actual.puedeTransicionarA(destino)) {
            throw new IllegalStateException(
                "Transicion no permitida: " + actual.getEstado() + " -> " + destino);
        }
        return dao.cambiarEstado(idHistoria, destino, idUsuarioOperador);
    }

    public boolean cancelar(int idHistoria) throws SQLException {
        return dao.eliminar(idHistoria);
    }

    private void validar(HistoriaUsuario h) {
        if (h.getTitulo() == null || h.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El titulo de la historia es obligatorio");
        }
        if (h.getIdEpica() == null) {
            throw new IllegalArgumentException("La historia debe pertenecer a una epica");
        }
        if (h.getPrioridad() == null) h.setPrioridad(3);
        if (h.getPrioridad() < 1 || h.getPrioridad() > 5) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 y 5");
        }
        if (h.getStoryPoints() != null && h.getStoryPoints() <= 0) {
            throw new IllegalArgumentException("Los story points deben ser positivos");
        }
    }
}
