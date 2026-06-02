package com.omprela.boards.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexion a MySQL mediante JDBC, implementado con el patron Singleton.
 * <p>
 * Centraliza la configuracion de la conexion en un unico punto. Si se necesita
 * cambiar las credenciales o el puerto, se modifica aqui.
 * <p>
 * Provee dos modos de conexion:
 * <ul>
 *   <li>{@link #getServerConnection()}: conecta al servidor SIN seleccionar base,
 *       usado por el bootstrap para poder ejecutar CREATE DATABASE.</li>
 *   <li>{@link #getConnection()}: conecta a la base omprela_boards ya creada.</li>
 * </ul>
 * <p>
 * IMPORTANTE: requiere el driver mysql-connector-j en el classpath (carpeta lib/).
 */
public class DBConnection {

    // ===== CONFIGURACION DE LA BASE DE DATOS =====
    // Si tu MySQL usa otro puerto o password, cambialo aqui:
    private static final String HOST     = "localhost";
    private static final String PUERTO   = "3306";          // puerto por defecto de MySQL
    private static final String BASE     = "omprela_boards";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "clave123";

    private static final String PARAMS =
        "?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";

    // URL al servidor (sin base) - para crear la base si no existe
    private static final String URL_SERVER =
        "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + PARAMS;

    // URL a la base concreta
    private static final String URL_BASE =
        "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE + PARAMS;

    private static Connection instancia;

    private DBConnection() { }

    public static String getNombreBase() { return BASE; }

    /**
     * Conexion al servidor MySQL SIN seleccionar base de datos.
     * Se usa una sola vez en el bootstrap para crear la base si no existe.
     */
    public static Connection getServerConnection() {
        try {
            return DriverManager.getConnection(URL_SERVER, USUARIO, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo conectar al servidor MySQL. Verifica que este corriendo " +
                "y que las credenciales en DBConnection.java sean correctas.\n" +
                "Detalle: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la conexion activa a la base omprela_boards (la crea la primera vez).
     */
    public static Connection getConnection() {
        try {
            if (instancia == null || instancia.isClosed()) {
                instancia = DriverManager.getConnection(URL_BASE, USUARIO, PASSWORD);
            }
            return instancia;
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo conectar a la base '" + BASE + "'.\n" +
                "Detalle: " + e.getMessage(), e);
        }
    }

    /** Cierra la conexion activa. */
    public static void close() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
                instancia = null;
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}
