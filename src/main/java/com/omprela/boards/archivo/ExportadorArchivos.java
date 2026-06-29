package com.omprela.boards.archivo;

import com.omprela.boards.model.Ticket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la exportacion e importacion de datos a/desde archivos de texto,
 * usando el paquete java.io (clase File, FileWriter, BufferedReader, etc.).
 * <p>
 * Demuestra la manipulacion de archivos solicitada (opcionalmente) por el TP4:
 * <ul>
 *   <li>Exportar el backlog a un archivo CSV (escritura con FileWriter/PrintWriter).</li>
 *   <li>Registrar eventos en un archivo de log (escritura en modo append).</li>
 *   <li>Leer de vuelta el CSV exportado (lectura con BufferedReader).</li>
 * </ul>
 * Todas las operaciones manejan IOException con try-with-resources, garantizando
 * el cierre correcto de los flujos (streams).
 */
public class ExportadorArchivos {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Carpeta donde se guardan los archivos generados. */
    private final File carpeta;

    public ExportadorArchivos(String rutaCarpeta) {
        this.carpeta = new File(rutaCarpeta);
        // La clase File permite operar a nivel de sistema de archivos:
        // crear la carpeta si no existe.
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    /**
     * Exporta la lista de tickets a un archivo CSV.
     * @param tickets lista de tickets (ArrayList) a exportar
     * @param nombreArchivo nombre del archivo CSV de destino
     * @return la ruta absoluta del archivo generado
     */
    public String exportarCSV(List<Ticket> tickets, String nombreArchivo) {
        File destino = new File(carpeta, nombreArchivo);

        // try-with-resources: cierra el flujo automaticamente al terminar
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(destino)))) {
            // Cabecera del CSV
            pw.println("id,tipo,titulo,prioridad,estado,esfuerzo");

            // Una linea por ticket
            for (Ticket t : tickets) {
                pw.printf("%d,%s,\"%s\",%d,%s,%.1f%n",
                    t.getId(),
                    t.getTipo(),
                    t.getTitulo().replace("\"", "'"),
                    t.getPrioridad(),
                    t.getEstado(),
                    t.calcularEsfuerzo());
            }
            return destino.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException(
                "Error al exportar el CSV '" + nombreArchivo + "': " + e.getMessage(), e);
        }
    }

    /**
     * Registra un evento en el archivo de log (modo append: agrega al final
     * sin borrar lo anterior).
     */
    public void registrarLog(String evento) {
        File log = new File(carpeta, "eventos.log");

        // FileWriter con segundo parametro 'true' = modo append
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(log, true)))) {
            String linea = String.format("[%s] %s", LocalDateTime.now().format(FMT), evento);
            pw.println(linea);
        } catch (IOException e) {
            // El log no es critico: avisamos pero no interrumpimos el programa
            System.err.println("No se pudo escribir en el log: " + e.getMessage());
        }
    }

    /**
     * Lee de vuelta un archivo CSV exportado y devuelve sus lineas.
     * Demuestra la lectura de archivos con BufferedReader.
     * @return lista (ArrayList) con las lineas leidas del archivo
     */
    public List<String> leerCSV(String nombreArchivo) {
        File origen = new File(carpeta, nombreArchivo);
        List<String> lineas = new ArrayList<>();

        if (!origen.exists()) {
            throw new RuntimeException("El archivo '" + nombreArchivo + "' no existe");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(origen))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Error al leer el CSV '" + nombreArchivo + "': " + e.getMessage(), e);
        }
        return lineas;
    }
}
