package com.omprela.boards.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestor unico de conexiones JDBC al motor MySQL.
 * Aplica el patron Singleton para reutilizar la conexion durante la ejecucion
 * del prototipo. En la version final, se reemplazara por un pool de conexiones
 * (HikariCP / DBCP) administrado por Spring.
 *
 * En el primer uso se crea la base si no existe y se aplican las migraciones
 * pendientes (ver MigrationRunner).
 */
public class DBConnection {

    private static final String ARCHIVO_CONFIG = "/db.properties";
    private static final Properties config = new Properties();
    private static Connection conexion;
    private static boolean inicializada = false;

    static {
        cargarConfiguracion();
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        if (!inicializada) {
            inicializarBaseDeDatos();
            inicializada = true;
        }
        if (conexion == null || conexion.isClosed()) {
            conexion = abrirConexion(true);
        }
        return conexion;
    }

    public static void close() {
        if (conexion != null) {
            try { conexion.close(); } catch (SQLException ignored) { }
            conexion = null;
        }
    }

    public static String getNombreBaseDeDatos() {
        return config.getProperty("db.name", "omprela_boards");
    }

    private static void cargarConfiguracion() {
        try (InputStream is = DBConnection.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (is == null) {
                throw new IllegalStateException(
                    "No se encontro " + ARCHIVO_CONFIG + " en el classpath. " +
                    "Verifica que src/main/resources este incluido como carpeta de recursos.");
            }
            config.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIG, e);
        }
    }

    /**
     * Crea la base si no existe y aplica las migraciones pendientes.
     * Se invoca una sola vez por ejecucion (la primera vez que alguien pide conexion).
     */
    private static void inicializarBaseDeDatos() throws SQLException {
        cargarDriver();
        try (Connection bootstrap = abrirConexion(false);
             Statement st = bootstrap.createStatement()) {
            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS `" + getNombreBaseDeDatos() +
                "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        try (Connection cn = abrirConexion(true)) {
            new MigrationRunner(cn).aplicar();
        }
    }

    private static Connection abrirConexion(boolean conBase) throws SQLException {
        return DriverManager.getConnection(buildUrl(conBase), usuario(), password());
    }

    private static void cargarDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC de MySQL no encontrado en el classpath", e);
        }
    }

    private static String buildUrl(boolean conBase) {
        String host = config.getProperty("db.host", "localhost");
        String port = config.getProperty("db.port", "3306");
        String tz   = config.getProperty("db.timezone", "America/Argentina/Buenos_Aires");
        String suf  = conBase ? "/" + getNombreBaseDeDatos() : "/";
        return "jdbc:mysql://" + host + ":" + port + suf +
               "?useSSL=false&allowPublicKeyRetrieval=true" +
               "&serverTimezone=" + tz +
               "&allowMultiQueries=true";
    }

    private static String usuario() {
        return config.getProperty("db.user", "root");
    }

    private static String password() {
        return config.getProperty("db.password", "");
    }
}
