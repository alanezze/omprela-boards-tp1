package com.omprela.boards.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Inicializa automaticamente la base de datos al arrancar la aplicacion,
 * ejecutando el script SQL canonico del proyecto ({@code sql/01_create_omprela_boards.sql}).
 * <p>
 * En vez de duplicar el esquema en Java, este componente usa el MISMO script que
 * el TP1 como unica fuente de verdad, de modo que la base que crea la app es
 * identica a la del script (con sus tablas separadas: {@code historias_usuario},
 * {@code tareas}, {@code proyectos}, {@code epicas}, etc.).
 * <p>
 * Comportamiento (idempotente, no destructivo):
 * <ol>
 *   <li>Crea la base {@code omprela_boards} si no existe.</li>
 *   <li>Si la base aun NO esta inicializada (no existe la tabla
 *       {@code historias_usuario}), ejecuta el script completo: crea TODAS las
 *       tablas, vistas y carga el seeder.</li>
 *   <li>Si la base ya estaba inicializada, no toca los datos existentes.</li>
 *   <li>Aplica migraciones livianas idempotentes (p.ej. permitir el estado
 *       CANCELADA en tareas) para mantener el esquema al dia.</li>
 * </ol>
 */
public class BootstrapDB {

    /** Tabla "centinela": si existe, asumimos que la base ya fue inicializada. */
    private static final String TABLA_CENTINELA = "historias_usuario";

    /** Rutas candidatas donde buscar el script de creacion (relativas al cwd). */
    private static final String[] RUTAS_SCRIPT = {
        "sql/01_create_omprela_boards.sql",
        "prototipo_v4/sql/01_create_omprela_boards.sql",
        "../sql/01_create_omprela_boards.sql"
    };

    private BootstrapDB() { }

    /** Ejecuta el bootstrap completo. Es idempotente: se puede llamar muchas veces. */
    public static void inicializar() {
        crearBaseSiNoExiste();
        if (!yaInicializada()) {
            System.out.println("[bootstrap] Base nueva: ejecutando script de creacion + seeder...");
            ejecutarScriptCreacion();
        } else {
            System.out.println("[bootstrap] Base ya inicializada: se conservan los datos existentes.");
        }
        aplicarMigraciones();
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

    /** Indica si la base ya tiene el esquema cargado (existe la tabla centinela). */
    private static boolean yaInicializada() {
        String sql =
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = ? AND table_name = ?";
        try (Connection con = DBConnection.getConnection();
             var ps = con.prepareStatement(sql)) {
            ps.setString(1, DBConnection.getNombreBase());
            ps.setString(2, TABLA_CENTINELA);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar el estado de la base: " + e.getMessage(), e);
        }
    }

    /**
     * Paso 2: ejecuta el script SQL de creacion (DDL + seeder) sentencia por sentencia.
     * Se descartan las lineas DROP/CREATE DATABASE/USE: la seleccion de base ya la
     * maneja la conexion (asi se respeta la idempotencia y el nombre de base configurable).
     */
    private static void ejecutarScriptCreacion() {
        String script = leerScript();

        // Quita comentarios de linea y las sentencias de contexto de base de datos.
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\\r?\\n")) {
            String t = linea.trim();
            if (t.isEmpty() || t.startsWith("--")) continue;
            String up = t.toUpperCase();
            if (up.startsWith("DROP DATABASE") || up.startsWith("CREATE DATABASE") || up.startsWith("USE ")) {
                continue;
            }
            limpio.append(linea).append('\n');
        }

        String[] sentencias = limpio.toString().split(";");
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            int ejecutadas = 0;
            for (String s : sentencias) {
                String sql = s.trim();
                if (sql.isEmpty()) continue;
                st.execute(sql);   // sirve para DDL, INSERT y SELECT de verificacion
                ejecutadas++;
            }
            System.out.printf("[bootstrap] Script ejecutado: %d sentencias.%n", ejecutadas);
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar el script de creacion: " + e.getMessage(), e);
        }
    }

    /** Busca y lee el script de creacion desde las rutas candidatas (o -Domprela.sql=...). */
    private static String leerScript() {
        String override = System.getProperty("omprela.sql");
        if (override != null) {
            File f = new File(override);
            if (f.isFile()) return leerArchivo(f);
            throw new RuntimeException("No se encontro el script indicado en -Domprela.sql: " + override);
        }
        for (String ruta : RUTAS_SCRIPT) {
            File f = new File(ruta);
            if (f.isFile()) return leerArchivo(f);
        }
        throw new RuntimeException(
            "No se encontro el script de creacion. Ejecuta la app desde la carpeta " +
            "'prototipo_v4' (donde esta la carpeta sql/) o indica la ruta con -Domprela.sql=<ruta>.");
    }

    private static String leerArchivo(File f) {
        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el script '" + f.getPath() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Paso 3: migraciones livianas idempotentes sobre el esquema existente.
     * <p>
     * Permite el estado CANCELADA en la tabla {@code tareas} (el script original solo
     * lo admitia en historias), para que la opcion "mover de estados" funcione completa
     * tambien sobre tareas. Si la migracion ya fue aplicada, los errores se ignoran.
     */
    private static void aplicarMigraciones() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            try { st.executeUpdate("ALTER TABLE tareas DROP CHECK chk_tareas_estado"); }
            catch (Exception ignored) { /* no existia o ya fue removida */ }
            try {
                st.executeUpdate(
                    "ALTER TABLE tareas ADD CONSTRAINT chk_tareas_estado CHECK (estado IN " +
                    "('POR_HACER','EN_PROGRESO','EN_REVISION','HECHO','CANCELADA'))");
            } catch (Exception ignored) { /* ya estaba aplicada */ }
        } catch (Exception e) {
            // Las migraciones no son criticas para arrancar: avisamos y seguimos.
            System.err.println("[bootstrap] Aviso: no se pudieron aplicar migraciones: " + e.getMessage());
        }
    }
}
