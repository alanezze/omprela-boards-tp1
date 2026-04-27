package com.omprela.boards.dao;

import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Historia de Usuario.
 * Implementa CRUD basico, consultas para el tablero Kanban y registro de
 * transiciones de estado en el log de auditoria.
 */
public class HistoriaUsuarioDAO {

    public HistoriaUsuario insertar(HistoriaUsuario h) throws SQLException {
        String sql = "INSERT INTO historias_usuario (titulo, descripcion, criterios_aceptacion, " +
                     "story_points, prioridad, estado, id_epica, id_sprint, id_usuario_asignado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, h.getTitulo());
            ps.setString(2, h.getDescripcion());
            ps.setString(3, h.getCriteriosAceptacion());
            if (h.getStoryPoints() != null) ps.setInt(4, h.getStoryPoints());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, h.getPrioridad() == null ? 3 : h.getPrioridad());
            ps.setString(6, h.getEstado() == null ? "POR_HACER" : h.getEstado().name());
            ps.setInt(7, h.getIdEpica());
            if (h.getIdSprint() != null) ps.setInt(8, h.getIdSprint());
            else ps.setNull(8, Types.INTEGER);
            if (h.getIdUsuarioAsignado() != null) ps.setInt(9, h.getIdUsuarioAsignado());
            else ps.setNull(9, Types.INTEGER);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) h.setIdHistoria(keys.getInt(1));
            }
        }
        return h;
    }

    public HistoriaUsuario buscarPorId(int idHistoria) throws SQLException {
        String sql = "SELECT * FROM historias_usuario WHERE id_historia = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idHistoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /**
     * Devuelve las historias de un sprint agrupadas para alimentar el tablero Kanban.
     */
    public List<HistoriaUsuario> listarPorSprint(int idSprint) throws SQLException {
        String sql = "SELECT * FROM historias_usuario WHERE id_sprint = ? " +
                     "ORDER BY prioridad ASC, id_historia ASC";
        List<HistoriaUsuario> lista = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idSprint);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<HistoriaUsuario> listarPorEpica(int idEpica) throws SQLException {
        String sql = "SELECT * FROM historias_usuario WHERE id_epica = ? " +
                     "ORDER BY prioridad ASC, id_historia ASC";
        List<HistoriaUsuario> lista = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEpica);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Cambia el estado de la historia y registra la transicion en log_auditoria.
     * Se ejecuta dentro de una transaccion para garantizar atomicidad.
     */
    public boolean cambiarEstado(int idHistoria, HistoriaUsuario.Estado nuevoEstado, int idUsuarioOperador)
            throws SQLException {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            String estadoActual;
            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT estado FROM historias_usuario WHERE id_historia = ?")) {
                ps.setInt(1, idHistoria);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { cn.rollback(); return false; }
                    estadoActual = rs.getString("estado");
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE historias_usuario SET estado = ?, " +
                    "fecha_cierre = CASE WHEN ? = 'HECHO' THEN NOW() ELSE fecha_cierre END " +
                    "WHERE id_historia = ?")) {
                ps.setString(1, nuevoEstado.name());
                ps.setString(2, nuevoEstado.name());
                ps.setInt(3, idHistoria);
                if (ps.executeUpdate() == 0) { cn.rollback(); return false; }
            }

            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO log_auditoria (entidad, id_entidad, accion, valor_anterior, " +
                    "valor_nuevo, id_usuario) VALUES ('HistoriaUsuario', ?, 'CAMBIO_ESTADO', ?, ?, ?)")) {
                ps.setInt(1, idHistoria);
                ps.setString(2, estadoActual);
                ps.setString(3, nuevoEstado.name());
                ps.setInt(4, idUsuarioOperador);
                ps.executeUpdate();
            }

            cn.commit();
            return true;
        } catch (SQLException ex) {
            if (cn != null) cn.rollback();
            throw ex;
        } finally {
            if (cn != null) cn.setAutoCommit(true);
        }
    }

    public boolean eliminar(int idHistoria) throws SQLException {
        String sql = "UPDATE historias_usuario SET estado = 'CANCELADA' WHERE id_historia = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idHistoria);
            return ps.executeUpdate() > 0;
        }
    }

    private HistoriaUsuario mapear(ResultSet rs) throws SQLException {
        HistoriaUsuario h = new HistoriaUsuario();
        h.setIdHistoria(rs.getInt("id_historia"));
        h.setTitulo(rs.getString("titulo"));
        h.setDescripcion(rs.getString("descripcion"));
        h.setCriteriosAceptacion(rs.getString("criterios_aceptacion"));
        int sp = rs.getInt("story_points");
        h.setStoryPoints(rs.wasNull() ? null : sp);
        h.setPrioridad(rs.getInt("prioridad"));
        h.setEstado(HistoriaUsuario.Estado.valueOf(rs.getString("estado")));
        Timestamp tsCre = rs.getTimestamp("fecha_creacion");
        if (tsCre != null) h.setFechaCreacion(tsCre.toLocalDateTime());
        Timestamp tsCie = rs.getTimestamp("fecha_cierre");
        if (tsCie != null) h.setFechaCierre(tsCie.toLocalDateTime());
        h.setIdEpica(rs.getInt("id_epica"));
        int sprint = rs.getInt("id_sprint");
        h.setIdSprint(rs.wasNull() ? null : sprint);
        int asign = rs.getInt("id_usuario_asignado");
        h.setIdUsuarioAsignado(rs.wasNull() ? null : asign);
        return h;
    }
}
