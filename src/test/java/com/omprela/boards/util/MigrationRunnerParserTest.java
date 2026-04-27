package com.omprela.boards.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del parser SQL embebido en MigrationRunner. No requiere conexion a MySQL.
 *
 * Verifica que el separador de sentencias respete:
 *  - punto y coma como delimitador de sentencia
 *  - cadenas con comilla simple, doble e identificadores con backtick
 *  - comentarios de linea (--) que pueden contener punto y coma
 *  - comillas escapadas dobles ('')
 */
class MigrationRunnerParserTest {

    @SuppressWarnings("unchecked")
    private List<String> separar(String sql) throws Exception {
        MigrationRunner runner = new MigrationRunner(null);
        Method m = MigrationRunner.class.getDeclaredMethod("separarSentencias", String.class);
        m.setAccessible(true);
        return (List<String>) m.invoke(runner, sql);
    }

    @Test
    @DisplayName("separa una sentencia simple")
    void separaUnaSentenciaSimple() throws Exception {
        List<String> out = separar("SELECT 1;");
        assertEquals(1, out.size());
        assertEquals("SELECT 1", out.get(0).trim());
    }

    @Test
    @DisplayName("separa varias sentencias delimitadas por ;")
    void separaVariasSentencias() throws Exception {
        List<String> out = separar("CREATE TABLE a (id INT);\nINSERT INTO a VALUES (1);\nINSERT INTO a VALUES (2);");
        assertEquals(3, out.size());
        assertTrue(out.get(0).contains("CREATE TABLE a"));
        assertTrue(out.get(1).contains("VALUES (1)"));
        assertTrue(out.get(2).contains("VALUES (2)"));
    }

    @Test
    @DisplayName("ignora ; dentro de cadenas con comilla simple")
    void ignoraPuntoYComaEnCadenas() throws Exception {
        String sql = "INSERT INTO t (texto) VALUES ('hola; mundo');";
        List<String> out = separar(sql);
        assertEquals(1, out.size());
        assertTrue(out.get(0).contains("hola; mundo"));
    }

    @Test
    @DisplayName("ignora ; dentro de comillas dobles y backticks")
    void ignoraPuntoYComaEnDoblesYBackticks() throws Exception {
        String sql = "INSERT INTO `t;raro` (texto) VALUES (\"a;b\");\nSELECT 1;";
        List<String> out = separar(sql);
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("`t;raro`"));
        assertTrue(out.get(0).contains("\"a;b\""));
    }

    @Test
    @DisplayName("ignora ; en comentario de linea --")
    void ignoraPuntoYComaEnComentarioLinea() throws Exception {
        String sql = "SELECT 1; -- aca; va; comentario\nSELECT 2;";
        List<String> out = separar(sql);
        assertEquals(2, out.size());
        assertEquals("SELECT 1", out.get(0).trim());
        assertTrue(out.get(1).contains("SELECT 2"));
    }

    @Test
    @DisplayName("comilla escapada '' no termina la cadena")
    void comillaEscapadaNoCierraCadena() throws Exception {
        String sql = "INSERT INTO t (texto) VALUES ('it''s; ok');\nSELECT 2;";
        List<String> out = separar(sql);
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("it''s; ok"));
    }

    @Test
    @DisplayName("ultima sentencia sin ; final tambien se incluye")
    void ultimaSentenciaSinPuntoYComa() throws Exception {
        List<String> out = separar("SELECT 1;\nSELECT 2");
        assertEquals(2, out.size());
        assertTrue(out.get(1).contains("SELECT 2"));
    }

    @Test
    @DisplayName("entrada vacia produce lista vacia")
    void entradaVaciaListaVacia() throws Exception {
        assertEquals(0, separar("").size());
        assertEquals(0, separar("   \n\n  ").size());
    }

    @Test
    @DisplayName("solo comentarios produce una sentencia descartable (vacio efectivo)")
    void soloComentarios() throws Exception {
        // El metodo no descarta vacios; lo hace ejecutarMigracion. Aca verificamos
        // que el comentario no se mezcle con sentencias siguientes.
        List<String> out = separar("-- comentario\nSELECT 1;");
        assertEquals(1, out.size());
        assertTrue(out.get(0).contains("SELECT 1"));
    }

    @Test
    @DisplayName("respeta multiples sentencias DDL del schema real")
    void manejaSchemaRealista() throws Exception {
        String sql =
            "CREATE TABLE clientes (id INT NOT NULL AUTO_INCREMENT, email VARCHAR(120) NOT NULL,\n" +
            "  CONSTRAINT pk_c PRIMARY KEY (id));\n" +
            "CREATE TABLE usuarios (id INT NOT NULL AUTO_INCREMENT, rol VARCHAR(30),\n" +
            "  CONSTRAINT chk_rol CHECK (rol IN ('A','B','C')));\n" +
            "CREATE INDEX idx_c ON clientes(email);";
        List<String> out = separar(sql);
        assertEquals(3, out.size());
        assertTrue(out.get(0).contains("CREATE TABLE clientes"));
        assertTrue(out.get(1).contains("CREATE TABLE usuarios"));
        assertTrue(out.get(1).contains("'A','B','C'"));
        assertTrue(out.get(2).contains("CREATE INDEX"));
    }
}
