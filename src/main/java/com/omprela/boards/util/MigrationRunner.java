package com.omprela.boards.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sistema simple de migraciones versionadas inspirado en Flyway.
 *
 * Cada archivo .sql listado en /migrations/migrations.list se ejecuta una
 * unica vez. El registro se guarda en la tabla schema_migrations, de modo que
 * al iniciar la aplicacion se aplican solo las migraciones pendientes.
 *
 * Para agregar una nueva migracion:
 *   1. Crear el archivo /src/main/resources/migrations/Vxxx__descripcion.sql
 *   2. Agregar el nombre del archivo al final de migrations.list
 */
public class MigrationRunner {

    private static final String CARPETA = "/migrations/";
    private static final String LISTADO = CARPETA + "migrations.list";
    private static final String TABLA   = "schema_migrations";

    private final Connection cn;

    public MigrationRunner(Connection cn) {
        this.cn = cn;
    }

    public void aplicar() throws SQLException {
        crearTablaMigraciones();
        Set<String> aplicadas = leerAplicadas();
        List<String> archivos = listarMigraciones();

        int pendientes = 0;
        for (String archivo : archivos) {
            if (aplicadas.contains(archivo)) continue;
            ejecutarMigracion(archivo, leerArchivo(archivo));
            pendientes++;
        }

        if (pendientes == 0) {
            System.out.println("[migrations] esquema al dia (" + archivos.size() +
                " migraciones registradas)");
        } else {
            System.out.println("[migrations] " + pendientes +
                " migracion(es) aplicadas correctamente");
        }
    }

    private void crearTablaMigraciones() throws SQLException {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + TABLA + " (" +
                "version VARCHAR(100) NOT NULL PRIMARY KEY, " +
                "aplicada_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private Set<String> leerAplicadas() throws SQLException {
        Set<String> out = new HashSet<>();
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT version FROM " + TABLA)) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    private List<String> listarMigraciones() {
        List<String> out = new ArrayList<>();
        try (InputStream is = MigrationRunner.class.getResourceAsStream(LISTADO)) {
            if (is == null) {
                throw new IllegalStateException("No se encontro el listado " + LISTADO);
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String l = linea.trim();
                    if (!l.isEmpty() && !l.startsWith("#")) out.add(l);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo " + LISTADO, e);
        }
        return out;
    }

    private String leerArchivo(String nombre) {
        String ruta = CARPETA + nombre;
        try (InputStream is = MigrationRunner.class.getResourceAsStream(ruta)) {
            if (is == null) {
                throw new IllegalStateException("No se encontro la migracion: " + ruta);
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) sb.append(linea).append('\n');
                return sb.toString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo migracion " + nombre, e);
        }
    }

    private void ejecutarMigracion(String nombre, String sql) throws SQLException {
        System.out.println("[migrations] aplicando " + nombre);
        boolean autoCommitOriginal = cn.getAutoCommit();
        cn.setAutoCommit(false);
        try (Statement st = cn.createStatement()) {
            for (String stmt : separarSentencias(sql)) {
                String limpio = stmt.trim();
                if (!limpio.isEmpty()) st.execute(limpio);
            }
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO " + TABLA + "(version) VALUES (?)")) {
                ps.setString(1, nombre);
                ps.executeUpdate();
            }
            cn.commit();
        } catch (SQLException e) {
            try { cn.rollback(); } catch (SQLException ignored) { }
            throw new SQLException("Fallo al aplicar la migracion " + nombre +
                ": " + e.getMessage(), e);
        } finally {
            cn.setAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * Divide un script SQL en sentencias por punto y coma, ignorando los punto
     * y coma que esten dentro de cadenas o comentarios de linea (--).
     * No soporta comentarios /* ... *\/ ni delimitadores personalizados,
     * suficientes para los scripts del prototipo.
     */
    private List<String> separarSentencias(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean enComilla = false;
        char comillaActual = 0;
        boolean enComentarioLinea = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (enComentarioLinea) {
                buf.append(c);
                if (c == '\n') enComentarioLinea = false;
                continue;
            }

            if (!enComilla && c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                enComentarioLinea = true;
                buf.append(c);
                continue;
            }

            if (!enComilla && (c == '\'' || c == '"' || c == '`')) {
                enComilla = true;
                comillaActual = c;
                buf.append(c);
                continue;
            }

            if (enComilla && c == comillaActual) {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == comillaActual) {
                    buf.append(c).append(c);
                    i++;
                    continue;
                }
                enComilla = false;
                buf.append(c);
                continue;
            }

            if (!enComilla && c == ';') {
                out.add(buf.toString());
                buf.setLength(0);
            } else {
                buf.append(c);
            }
        }
        if (!buf.toString().trim().isEmpty()) out.add(buf.toString());
        return out;
    }
}
