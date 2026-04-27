package com.omprela.boards.dao;

import com.omprela.boards.model.Proyecto;
import com.omprela.boards.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Proyecto.
 * Encapsula todas las operaciones de persistencia contra la tabla proyectos
 * de MySQL utilizando JDBC y PreparedStatement para prevenir inyeccion SQL.
 */
public class ProyectoDAO {

    public Proyecto insertar(Proyecto p) throws SQLException {
        String sql = "INSERT INTO proyectos (nombre, descripcion, fecha_inicio, " +
                     "fecha_fin_estimada, estado, presupuesto, id_cliente) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDate(3, Date.valueOf(p.getFechaInicio()));
            if (p.getFechaFinEstimada() != null) {
                ps.setDate(4, Date.valueOf(p.getFechaFinEstimada()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, p.getEstado().name());
            ps.setBigDecimal(6, p.getPresupuesto());
            ps.setInt(7, p.getIdCliente());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setIdProyecto(keys.getInt(1));
            }
        }
        return p;
    }

    public Proyecto buscarPorId(int idProyecto) throws SQLException {
        String sql = "SELECT * FROM proyectos WHERE id_proyecto = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Proyecto> listarTodos() throws SQLException {
        String sql = "SELECT * FROM proyectos ORDER BY id_proyecto";
        List<Proyecto> lista = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Proyecto> listarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT * FROM proyectos WHERE id_cliente = ? ORDER BY fecha_inicio DESC";
        List<Proyecto> lista = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public boolean actualizar(Proyecto p) throws SQLException {
        String sql = "UPDATE proyectos SET nombre = ?, descripcion = ?, fecha_inicio = ?, " +
                     "fecha_fin_estimada = ?, estado = ?, presupuesto = ?, id_cliente = ? " +
                     "WHERE id_proyecto = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDate(3, Date.valueOf(p.getFechaInicio()));
            if (p.getFechaFinEstimada() != null) {
                ps.setDate(4, Date.valueOf(p.getFechaFinEstimada()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, p.getEstado().name());
            ps.setBigDecimal(6, p.getPresupuesto());
            ps.setInt(7, p.getIdCliente());
            ps.setInt(8, p.getIdProyecto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idProyecto) throws SQLException {
        // Baja logica: el proyecto pasa a estado CANCELADO en lugar de eliminarse fisicamente
        String sql = "UPDATE proyectos SET estado = 'CANCELADO' WHERE id_proyecto = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            return ps.executeUpdate() > 0;
        }
    }

    private Proyecto mapear(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        Date fin = rs.getDate("fecha_fin_estimada");
        if (fin != null) p.setFechaFinEstimada(fin.toLocalDate());
        p.setEstado(Proyecto.Estado.valueOf(rs.getString("estado")));
        p.setPresupuesto(rs.getBigDecimal("presupuesto"));
        p.setIdCliente(rs.getInt("id_cliente"));
        return p;
    }
}
