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
 * A diferencia de la version "single table", este DAO respeta el esquema relacional
 * del proyecto y persiste cada tipo en SU PROPIA tabla:
 * <ul>
 *   <li>Las {@link HistoriaUsuario} van a la tabla {@code historias_usuario}.</li>
 *   <li>Las {@link Tarea} van a la tabla {@code tareas}.</li>
 * </ul>
 * Como ambos tipos comparten el id autoincremental de su tabla, las operaciones que
 * referencian un ticket puntual se identifican por la dupla (tipo, id). El tipo se
 * expresa con las constantes {@link #TIPO_HISTORIA} y {@link #TIPO_TAREA}.
 * <p>
 * Los cambios de estado se auditan en la tabla {@code log_auditoria}.
 */
public class TicketDAO {

    public static final String TIPO_HISTORIA = "HISTORIA";
    public static final String TIPO_TAREA    = "TAREA";

    /** Usuario por defecto al que se atribuyen las acciones de auditoria (Alan, id=1). */
    private static final int ID_USUARIO_AUDITORIA = 1;

    /** Tarea de prioridad por defecto (la tabla 'tareas' no persiste prioridad). */
    private static final int PRIORIDAD_TAREA_DEFECTO = 3;

    // ============================================================
    //  ALTA
    // ============================================================

    /** Inserta un ticket en la tabla que corresponde a su tipo y devuelve el id. */
    public int insertar(Ticket t) {
        if (t instanceof HistoriaUsuario) {
            return insertarHistoria((HistoriaUsuario) t);
        }
        return insertarTarea((Tarea) t);
    }

    private int insertarHistoria(HistoriaUsuario h) {
        String sql = "INSERT INTO historias_usuario " +
            "(titulo, descripcion, criterios_aceptacion, story_points, prioridad, estado, " +
            " id_epica, id_sprint, id_usuario_asignado) " +
            "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getTitulo());
            ps.setString(2, h.getDescripcion());
            ps.setString(3, h.getCriteriosAceptacion());
            if (h.getStoryPoints() != null) ps.setInt(4, h.getStoryPoints());
            else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setInt(5, h.getPrioridad());
            ps.setString(6, h.getEstado().name());
            ps.setInt(7, h.getIdEpica());                 // NOT NULL en el esquema
            if (h.getIdSprint() != null) ps.setInt(8, h.getIdSprint());
            else ps.setNull(8, java.sql.Types.INTEGER);
            ps.setNull(9, java.sql.Types.INTEGER);        // id_usuario_asignado opcional
            return ejecutarYObtenerId(ps, h);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar historia: " + e.getMessage(), e);
        }
    }

    private int insertarTarea(Tarea ta) {
        String sql = "INSERT INTO tareas " +
            "(titulo, descripcion, horas_estimadas, horas_reales, estado, " +
            " id_historia, id_usuario_asignado) " +
            "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ta.getTitulo());
            ps.setString(2, ta.getDescripcion());
            if (ta.getHorasEstimadas() != null) ps.setDouble(3, ta.getHorasEstimadas());
            else ps.setNull(3, java.sql.Types.DECIMAL);
            ps.setDouble(4, ta.getHorasReales() == null ? 0.0 : ta.getHorasReales());
            ps.setString(5, ta.getEstado().name());
            ps.setInt(6, ta.getIdHistoria());             // NOT NULL en el esquema
            ps.setNull(7, java.sql.Types.INTEGER);        // id_usuario_asignado opcional
            return ejecutarYObtenerId(ps, ta);
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar tarea: " + e.getMessage(), e);
        }
    }

    private int ejecutarYObtenerId(PreparedStatement ps, Ticket t) throws SQLException {
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                int id = rs.getInt(1);
                t.setId(id);
                return id;
            }
        }
        return -1;
    }

    // ============================================================
    //  CONSULTA
    // ============================================================

    /** Lista todos los tickets reuniendo las dos tablas (historias + tareas). */
    public List<Ticket> listarTodos() {
        List<Ticket> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery(
                    "SELECT id_historia, titulo, descripcion, story_points, prioridad, estado " +
                    "FROM historias_usuario")) {
                while (rs.next()) lista.add(mapearHistoria(rs));
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT id_tarea, titulo, descripcion, horas_estimadas, horas_reales, estado, id_historia " +
                    "FROM tareas")) {
                while (rs.next()) lista.add(mapearTarea(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar tickets: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Busca un ticket por (tipo, id). Devuelve null si no existe. */
    public Ticket buscarPorId(String tipo, int id) {
        if (esHistoria(tipo)) {
            String sql = "SELECT id_historia, titulo, descripcion, story_points, prioridad, estado " +
                         "FROM historias_usuario WHERE id_historia = ?";
            return buscar(sql, id, true);
        } else {
            String sql = "SELECT id_tarea, titulo, descripcion, horas_estimadas, horas_reales, estado, id_historia " +
                         "FROM tareas WHERE id_tarea = ?";
            return buscar(sql, id, false);
        }
    }

    private Ticket buscar(String sql, int id, boolean historia) {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return historia ? mapearHistoria(rs) : mapearTarea(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar ticket: " + e.getMessage(), e);
        }
        return null;
    }

    // ============================================================
    //  CAMBIO DE ESTADO + AUDITORIA
    // ============================================================

    /**
     * Actualiza el estado de un ticket en su tabla y registra el movimiento en
     * {@code log_auditoria}. El UPDATE y la auditoria comparten transaccion; si la
     * auditoria fallara, igualmente se confirma el cambio de estado (best-effort).
     */
    public void actualizarEstado(String tipo, int id, Estado anterior, Estado nuevo) {
        String tabla = esHistoria(tipo) ? "historias_usuario" : "tareas";
        String columnaId = esHistoria(tipo) ? "id_historia" : "id_tarea";

        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE " + tabla + " SET estado = ? WHERE " + columnaId + " = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO log_auditoria " +
                    "(entidad, id_entidad, accion, valor_anterior, valor_nuevo, id_usuario) " +
                    "VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, tipo);
                ps.setInt(2, id);
                ps.setString(3, "CAMBIO_ESTADO");
                ps.setString(4, anterior == null ? null : anterior.name());
                ps.setString(5, nuevo.name());
                ps.setInt(6, ID_USUARIO_AUDITORIA);
                ps.executeUpdate();
            } catch (SQLException auditEx) {
                // La auditoria es deseable pero no debe impedir el cambio de estado.
                System.err.println("[aviso] No se pudo auditar el movimiento: " + auditEx.getMessage());
            }

            con.commit();
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ============================================================
    //  MAPEO
    // ============================================================

    private Ticket mapearHistoria(ResultSet rs) throws SQLException {
        HistoriaUsuario h = new HistoriaUsuario();
        h.setId(rs.getInt("id_historia"));
        h.setTitulo(rs.getString("titulo"));
        h.setDescripcion(rs.getString("descripcion"));
        h.setPrioridad(rs.getInt("prioridad"));
        int sp = rs.getInt("story_points");
        if (!rs.wasNull()) h.setStoryPoints(sp);
        h.setEstado(Estado.valueOf(rs.getString("estado")));
        return h;
    }

    private Ticket mapearTarea(ResultSet rs) throws SQLException {
        Tarea ta = new Tarea();
        ta.setId(rs.getInt("id_tarea"));
        ta.setTitulo(rs.getString("titulo"));
        ta.setDescripcion(rs.getString("descripcion"));
        // La tabla 'tareas' no persiste prioridad: se usa un valor por defecto.
        ta.setPrioridad(PRIORIDAD_TAREA_DEFECTO);
        double he = rs.getDouble("horas_estimadas");
        if (!rs.wasNull()) ta.setHorasEstimadas(he);
        double hr = rs.getDouble("horas_reales");
        if (!rs.wasNull()) ta.setHorasReales(hr);
        ta.setEstado(Estado.valueOf(rs.getString("estado")));
        ta.setIdHistoria(rs.getInt("id_historia"));
        return ta;
    }

    private static boolean esHistoria(String tipo) {
        return TIPO_HISTORIA.equalsIgnoreCase(tipo);
    }
}
