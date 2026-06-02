package com.omprela.boards.dao;

import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.model.Tarea;
import com.omprela.boards.model.Ticket;
import com.omprela.boards.model.Ticket.Estado;
import com.omprela.boards.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la persistencia de tickets en MySQL via JDBC.
 * <p>
 * Implementa la estrategia de herencia "single table": la tabla 'tickets' tiene
 * una columna discriminadora 'tipo' que indica si la fila es una HistoriaUsuario
 * o una Tarea. El metodo mapear() reconstruye el objeto del tipo correcto a partir
 * de esa columna, demostrando polimorfismo en la capa de persistencia.
 */
public class TicketDAO {

    /**
     * Inserta un ticket en la base y devuelve el id autogenerado.
     */
    public int insertar(Ticket t) {
        String sql = "INSERT INTO tickets " +
            "(tipo, titulo, descripcion, prioridad, estado, story_points, horas_estimadas) " +
            "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getTipo().equals("HISTORIA_USUARIO") ? "HISTORIA" : "TAREA");
            ps.setString(2, t.getTitulo());
            ps.setString(3, t.getDescripcion());
            ps.setInt(4, t.getPrioridad());
            ps.setString(5, t.getEstado().name());

            if (t instanceof HistoriaUsuario) {
                Integer sp = ((HistoriaUsuario) t).getStoryPoints();
                if (sp != null) ps.setInt(6, sp); else ps.setNull(6, java.sql.Types.INTEGER);
                ps.setNull(7, java.sql.Types.DECIMAL);
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
                Double h = ((Tarea) t).getHorasEstimadas();
                if (h != null) ps.setDouble(7, h); else ps.setNull(7, java.sql.Types.DECIMAL);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    t.setId(id);
                    return id;
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar ticket: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos los tickets de la base, reconstruyendo cada uno como
     * HistoriaUsuario o Tarea segun la columna discriminadora 'tipo'.
     */
    public List<Ticket> listarTodos() {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY prioridad ASC, id ASC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar tickets: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca un ticket por su id. Devuelve null si no existe.
     */
    public Ticket buscarPorId(int id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar ticket: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Actualiza el estado de un ticket y registra el movimiento en log_movimientos,
     * todo dentro de una transaccion para garantizar atomicidad.
     */
    public void actualizarEstado(int id, Estado anterior, Estado nuevo) {
        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE tickets SET estado = ? WHERE id = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO log_movimientos (id_ticket, estado_anterior, estado_nuevo) " +
                    "VALUES (?,?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, anterior == null ? null : anterior.name());
                ps.setString(3, nuevo.name());
                ps.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
        }
    }

    /**
     * Reconstruye un Ticket del tipo correcto a partir de una fila del ResultSet.
     * El polimorfismo se materializa aqui: segun la columna 'tipo' se crea
     * una HistoriaUsuario o una Tarea.
     */
    private Ticket mapear(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        Ticket t;
        if ("HISTORIA".equals(tipo)) {
            int sp = rs.getInt("story_points");
            HistoriaUsuario h = new HistoriaUsuario();
            h.setTitulo(rs.getString("titulo"));
            h.setPrioridad(rs.getInt("prioridad"));
            if (!rs.wasNull()) h.setStoryPoints(sp);
            t = h;
        } else {
            Tarea ta = new Tarea();
            ta.setTitulo(rs.getString("titulo"));
            ta.setPrioridad(rs.getInt("prioridad"));
            double he = rs.getDouble("horas_estimadas");
            if (!rs.wasNull()) ta.setHorasEstimadas(he);
            t = ta;
        }
        t.setId(rs.getInt("id"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setEstado(Estado.valueOf(rs.getString("estado")));
        return t;
    }
}
