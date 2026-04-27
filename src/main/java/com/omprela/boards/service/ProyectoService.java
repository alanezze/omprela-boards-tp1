package com.omprela.boards.service;

import com.omprela.boards.dao.ProyectoDAO;
import com.omprela.boards.model.Proyecto;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Capa de servicio para la entidad Proyecto.
 * Aplica reglas de negocio (validaciones de fechas, transiciones de estado)
 * antes de delegar la persistencia al DAO. Forma parte del patron MVC,
 * donde esta clase actua como parte del Controlador / capa de servicio.
 */
public class ProyectoService {

    private final ProyectoDAO dao = new ProyectoDAO();

    public Proyecto crear(Proyecto p) throws SQLException {
        validar(p);
        return dao.insertar(p);
    }

    public Proyecto buscar(int idProyecto) throws SQLException {
        return dao.buscarPorId(idProyecto);
    }

    public List<Proyecto> listar() throws SQLException {
        return dao.listarTodos();
    }

    public List<Proyecto> listarPorCliente(int idCliente) throws SQLException {
        return dao.listarPorCliente(idCliente);
    }

    public boolean modificar(Proyecto p) throws SQLException {
        validar(p);
        if (p.getIdProyecto() == null) {
            throw new IllegalArgumentException("Para modificar se requiere idProyecto");
        }
        return dao.actualizar(p);
    }

    public boolean cancelar(int idProyecto) throws SQLException {
        return dao.eliminar(idProyecto);
    }

    private void validar(Proyecto p) {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proyecto es obligatorio");
        }
        if (p.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (p.getFechaFinEstimada() != null
                && p.getFechaFinEstimada().isBefore(p.getFechaInicio())) {
            throw new IllegalArgumentException(
                "La fecha fin estimada no puede ser anterior a la fecha de inicio");
        }
        if (p.getIdCliente() == null) {
            throw new IllegalArgumentException("El proyecto debe estar asociado a un cliente");
        }
        if (p.getEstado() == null) p.setEstado(Proyecto.Estado.ACTIVO);
        if (p.getFechaInicio().isBefore(LocalDate.of(2020,1,1))) {
            throw new IllegalArgumentException("La fecha de inicio es invalida");
        }
    }
}
