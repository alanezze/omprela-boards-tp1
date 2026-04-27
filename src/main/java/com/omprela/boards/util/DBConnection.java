package com.omprela.boards.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor unico de conexiones JDBC al motor MySQL.
 * Aplica el patron Singleton para reutilizar la conexion durante la ejecucion
 * del prototipo. En la version final, se reemplazara por un pool de conexiones
 * (HikariCP / DBCP) administrado por Spring.
 */
public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/omprela_boards?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "root";

    private static Connection conexion;

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC de MySQL no encontrado", e);
            }
        }
        return conexion;
    }

    public static void close() {
        if (conexion != null) {
            try { conexion.close(); } catch (SQLException ignored) { }
            conexion = null;
        }
    }
}
