package com.omprela.boards.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Inicializa automaticamente la base de datos al arrancar la aplicacion.
 * <p>
 * Replica el "bootstrap automatico" del prototipo original: no hace falta correr
 * ningun script SQL a mano. Al iniciar MainConsola, este componente:
 * <ol>
 *   <li>Crea la base de datos omprela_boards si no existe.</li>
 *   <li>Crea las tablas tickets y log_movimientos si no existen.</li>
 *   <li>Carga 6 tickets de ejemplo si la tabla esta vacia (idempotente:
 *       en arranques posteriores no duplica datos).</li>
 * </ol>
 */
public class BootstrapDB {

    private BootstrapDB() { }

    /**
     * Ejecuta el bootstrap completo. Es idempotente: se puede llamar muchas veces
     * sin efectos secundarios (usa IF NOT EXISTS y verifica antes de insertar).
     */
    public static void inicializar() {
        crearBaseSiNoExiste();
        crearTablas();
        cargarDatosSiVacia();
    }

    /** Paso 1: crea la base de datos usando una conexion al servidor (sin base). */
    private static void crearBaseSiNoExiste() {
        try (Connection con = DBConnection.getServerConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS " + DBConnection.getNombreBase() +
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la base de datos: " + e.getMessage(), e);
        }
    }

    /** Paso 2: crea las tablas tickets y log_movimientos. */
    private static void crearTablas() {
        String tablaTickets =
            "CREATE TABLE IF NOT EXISTS tickets (" +
            "  id              INT NOT NULL AUTO_INCREMENT," +
            "  tipo            VARCHAR(20)  NOT NULL," +
            "  titulo          VARCHAR(150) NOT NULL," +
            "  descripcion     TEXT," +
            "  prioridad       INT          NOT NULL DEFAULT 3," +
            "  estado          VARCHAR(20)  NOT NULL DEFAULT 'POR_HACER'," +
            "  story_points    INT," +
            "  horas_estimadas DECIMAL(6,2)," +
            "  horas_reales    DECIMAL(6,2) DEFAULT 0," +
            "  fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "  CONSTRAINT pk_tickets PRIMARY KEY (id)," +
            "  CONSTRAINT chk_tickets_tipo CHECK (tipo IN ('HISTORIA','TAREA'))," +
            "  CONSTRAINT chk_tickets_estado CHECK (estado IN " +
            "    ('POR_HACER','EN_PROGRESO','EN_REVISION','HECHO','CANCELADA'))," +
            "  CONSTRAINT chk_tickets_prioridad CHECK (prioridad BETWEEN 1 AND 5))";

        String tablaLog =
            "CREATE TABLE IF NOT EXISTS log_movimientos (" +
            "  id_log          INT NOT NULL AUTO_INCREMENT," +
            "  id_ticket       INT NOT NULL," +
            "  estado_anterior VARCHAR(20)," +
            "  estado_nuevo    VARCHAR(20)," +
            "  fecha_evento    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "  CONSTRAINT pk_log PRIMARY KEY (id_log)," +
            "  CONSTRAINT fk_log_ticket FOREIGN KEY (id_ticket)" +
            "    REFERENCES tickets(id) ON DELETE CASCADE)";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(tablaTickets);
            st.executeUpdate(tablaLog);
            // Indice para el filtrado por estado (puede fallar si ya existe, se ignora)
            try { st.executeUpdate("CREATE INDEX idx_tickets_estado ON tickets(estado)"); }
            catch (Exception ignored) { /* el indice ya existe */ }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear las tablas: " + e.getMessage(), e);
        }
    }

    /** Paso 3: carga datos de ejemplo solo si la tabla tickets esta vacia. */
    private static void cargarDatosSiVacia() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tickets")) {
                rs.next();
                if (rs.getInt(1) > 0) return;  // ya hay datos, no recargar
            }

            // 3 historias de usuario
            st.executeUpdate("INSERT INTO tickets (tipo, titulo, prioridad, estado, story_points) VALUES " +
                "('HISTORIA','Login de usuarios', 1, 'POR_HACER', 3)," +
                "('HISTORIA','CRUD de proyectos', 1, 'POR_HACER', 5)," +
                "('HISTORIA','Tablero Kanban',    1, 'POR_HACER', 8)");

            // 3 tareas tecnicas
            st.executeUpdate("INSERT INTO tickets (tipo, titulo, prioridad, estado, horas_estimadas) VALUES " +
                "('TAREA','Disenar tabla usuarios',         2, 'POR_HACER', 2.0)," +
                "('TAREA','Implementar endpoint POST login',1, 'POR_HACER', 4.0)," +
                "('TAREA','Validacion bcrypt',              3, 'POR_HACER', 3.0)");

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar datos de ejemplo: " + e.getMessage(), e);
        }
    }
}
