package com.omprela.boards.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integracion (IT) - requiere MySQL corriendo y credenciales validas
 * en db.properties. Para ejecutarlo:
 *
 *     mvn -Dintegration test
 *
 * El test dropea la base configurada antes de empezar para validar el flujo
 * de creacion desde cero. NO usar contra una base de produccion.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BootstrapDBIT {

    private static Properties config;

    @BeforeAll
    static void prepararEntorno() throws Exception {
        config = cargarConfig();
        // Drop de la base para validar creacion desde cero
        dropearBaseSiExiste();
        // Limpiar el singleton de DBConnection (por si otro test la inicializo)
        DBConnection.close();
    }

    private static String resolver(String clave, String porDefecto) {
        String s = System.getProperty(clave);
        if (s != null && !s.isEmpty()) return s;
        String env = System.getenv(clave.replace('.', '_').toUpperCase());
        if (env != null && !env.isEmpty()) return env;
        return config.getProperty(clave, porDefecto);
    }

    @Test
    @Order(1)
    @DisplayName("crea la base, aplica las 4 migraciones y conecta")
    void bootstrapDesdeCero() throws Exception {
        try (Connection cn = DBConnection.getConnection()) {
            assertNotNull(cn);
            assertFalse(cn.isClosed());

            // La base existe
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA " +
                     "WHERE SCHEMA_NAME = '" + DBConnection.getNombreBaseDeDatos() + "'")) {
                assertTrue(rs.next(), "La base " + DBConnection.getNombreBaseDeDatos() + " deberia existir");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("schema_migrations tiene las 4 versiones aplicadas")
    void schemaMigrationsCompleta() throws Exception {
        Set<String> versiones = consultarVersiones();
        assertEquals(4, versiones.size(),
            "Deberian estar registradas 4 migraciones, hay: " + versiones);
        assertTrue(versiones.contains("V001__schema.sql"));
        assertTrue(versiones.contains("V002__indices.sql"));
        assertTrue(versiones.contains("V003__vistas.sql"));
        assertTrue(versiones.contains("V004__datos_prueba.sql"));
    }

    @Test
    @Order(3)
    @DisplayName("todas las tablas de dominio existen")
    void tablasDeDominioCreadas() throws Exception {
        Set<String> esperadas = Set.of(
            "clientes", "usuarios", "proyectos", "epicas", "sprints",
            "historias_usuario", "tareas", "registro_horas", "comentarios",
            "log_auditoria", "schema_migrations");

        Set<String> reales = new HashSet<>();
        try (Connection cn = DBConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT TABLE_NAME FROM information_schema.TABLES " +
                 "WHERE TABLE_SCHEMA = '" + DBConnection.getNombreBaseDeDatos() + "' " +
                 "AND TABLE_TYPE = 'BASE TABLE'")) {
            while (rs.next()) reales.add(rs.getString(1).toLowerCase());
        }

        for (String t : esperadas) {
            assertTrue(reales.contains(t),
                "Falta la tabla " + t + ". Existentes: " + reales);
        }
    }

    @Test
    @Order(4)
    @DisplayName("vistas analiticas creadas")
    void vistasCreadas() throws Exception {
        Set<String> reales = new HashSet<>();
        try (Connection cn = DBConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT TABLE_NAME FROM information_schema.VIEWS " +
                 "WHERE TABLE_SCHEMA = '" + DBConnection.getNombreBaseDeDatos() + "'")) {
            while (rs.next()) reales.add(rs.getString(1));
        }
        assertTrue(reales.contains("v_backlog_priorizado"),
            "Falta vista v_backlog_priorizado. Existentes: " + reales);
        assertTrue(reales.contains("v_velocity_proyecto"),
            "Falta vista v_velocity_proyecto. Existentes: " + reales);
    }

    @Test
    @Order(5)
    @DisplayName("datos de prueba cargados con los conteos esperados")
    void datosDePruebaCargados() throws Exception {
        assertEquals(3, contarFilas("clientes"));
        assertEquals(6, contarFilas("usuarios"));
        assertEquals(3, contarFilas("proyectos"));
        assertEquals(5, contarFilas("epicas"));
        assertEquals(3, contarFilas("sprints"));
        assertEquals(5, contarFilas("historias_usuario"));
        assertEquals(7, contarFilas("tareas"));
        assertEquals(7, contarFilas("registro_horas"));
        assertEquals(3, contarFilas("comentarios"));
    }

    @Test
    @Order(6)
    @DisplayName("indices secundarios creados")
    void indicesCreados() throws Exception {
        Set<String> reales = new HashSet<>();
        try (Connection cn = DBConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT INDEX_NAME FROM information_schema.STATISTICS " +
                 "WHERE TABLE_SCHEMA = '" + DBConnection.getNombreBaseDeDatos() + "' " +
                 "AND INDEX_NAME LIKE 'idx_%'")) {
            while (rs.next()) reales.add(rs.getString(1));
        }
        assertTrue(reales.contains("idx_historias_estado"));
        assertTrue(reales.contains("idx_historias_sprint"));
        assertTrue(reales.contains("idx_tareas_estado"));
        assertTrue(reales.contains("idx_horas_usuario"));
    }

    @Test
    @Order(7)
    @DisplayName("re-aplicar migraciones es idempotente: no agrega filas ni duplica datos")
    void idempotencia() throws Exception {
        int versionesAntes = consultarVersiones().size();
        int clientesAntes  = contarFilas("clientes");

        // Invocamos directamente MigrationRunner.aplicar() sobre una conexion nueva
        // para forzar un segundo recorrido del listado de migraciones.
        try (Connection cn = abrirConBase()) {
            new MigrationRunner(cn).aplicar();
        }

        assertEquals(versionesAntes, consultarVersiones().size(),
            "schema_migrations no deberia crecer al re-aplicar migraciones");
        assertEquals(clientesAntes, contarFilas("clientes"),
            "Los datos de prueba no deberian duplicarse al re-aplicar V004");
    }

    private static Connection abrirConBase() throws SQLException {
        String url = "jdbc:mysql://" + host() + ":" + port() + "/" + nombreBase() +
                     "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" +
                     "&allowMultiQueries=true";
        return DriverManager.getConnection(url, usuario(), password());
    }

    // ----------------------------------------------------------------- helpers

    private static Properties cargarConfig() throws IOException {
        Properties p = new Properties();
        try (InputStream is = BootstrapDBIT.class.getResourceAsStream("/db.properties")) {
            assertNotNull(is, "No se encontro /db.properties en el classpath de test");
            p.load(is);
        }
        return p;
    }

    private static void dropearBaseSiExiste() throws SQLException {
        try (Connection cn = abrirSinBase();
             Statement st = cn.createStatement()) {
            st.executeUpdate("DROP DATABASE IF EXISTS `" + nombreBase() + "`");
        }
    }

    private static Connection abrirSinBase() throws SQLException {
        String url = "jdbc:mysql://" + host() + ":" + port() + "/?useSSL=false" +
                     "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, usuario(), password());
    }

    private static int contarFilas(String tabla) throws SQLException {
        try (Connection cn = DBConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }

    private static Set<String> consultarVersiones() throws SQLException {
        Set<String> out = new HashSet<>();
        try (Connection cn = DBConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT version FROM schema_migrations")) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    private static String host()       { return resolver("db.host", "localhost"); }
    private static String port()       { return resolver("db.port", "3306"); }
    private static String usuario()    { return resolver("db.user", "root"); }
    private static String password()   { return resolver("db.password", ""); }
    private static String nombreBase() { return resolver("db.name", "omprela_boards"); }
}
