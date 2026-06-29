package com.omprela.boards.view;

import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.model.Tarea;
import com.omprela.boards.model.Ticket;
import com.omprela.boards.model.Ticket.Estado;
import com.omprela.boards.model.excepciones.OmprelaException;
import com.omprela.boards.service.TicketService;
import com.omprela.boards.reporte.ReporteBacklog;
import com.omprela.boards.archivo.ExportadorArchivos;
import com.omprela.boards.util.DBConnection;
import com.omprela.boards.util.BootstrapDB;

import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola con menu de seleccion del prototipo OMPRELA-Boards (TP3).
 * <p>
 * Esta version persiste los datos en MySQL a traves de la capa Service -> DAO.
 * Cumple los requisitos de la consigna del TP3:
 * <ul>
 *   <li><b>Menu de seleccion</b> con estructuras condicionales (switch) y repetitivas (while).</li>
 *   <li><b>Declaracion y creacion de objetos</b> mediante constructores parametrizados.</li>
 *   <li><b>Manejo de excepciones</b> con try/catch (OmprelaException + NumberFormatException).</li>
 *   <li><b>Polimorfismo</b>: historias y tareas se procesan uniformemente.</li>
 *   <li><b>Persistencia en MySQL</b> via JDBC (cumple el requisito de base de datos).</li>
 * </ul>
 */
public class MainConsola {

    private final TicketService servicio;
    private final Scanner scanner;

    public MainConsola() {
        this.servicio = new TicketService();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new MainConsola().ejecutar();
    }

    public void ejecutar() {
        imprimirBanner();

        // Bootstrap automatico: crea la base, las tablas y los datos si no existen
        try {
            System.out.println("\n[bootstrap] Inicializando base de datos...");
            BootstrapDB.inicializar();
            System.out.printf("[OK] Base de datos lista. %d tickets en la base.%n",
                servicio.tickets());
        } catch (RuntimeException e) {
            System.out.println("\n[ERROR DE CONEXION] " + e.getMessage());
            System.out.println("Verifica que MySQL este corriendo y que las credenciales");
            System.out.println("en DBConnection.java (usuario/password/puerto) sean correctas.");
            return;
        }

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1": crearHistoriaUsuario(); break;
                    case "2": crearTarea(); break;
                    case "3": listarTodos(); break;
                    case "4": listarPorPrioridad(); break;
                    case "5": filtrarPorEstado(); break;
                    case "6": moverEstado(); break;
                    case "7": deshacerUltimoMovimiento(); break;
                    case "8": calcularEsfuerzoTotal(); break;
                    case "9": buscarTicket(); break;
                    case "10": mostrarReporte(); break;
                    case "11": exportarBacklog(); break;
                    case "0":
                        salir = true;
                        DBConnection.close();
                        System.out.println("\n[Saliendo] Conexion cerrada. Hasta luego.");
                        break;
                    default:
                        System.out.println("Opcion invalida. Por favor elija entre 0 y 9.");
                }
            } catch (OmprelaException ex) {
                System.out.println("[ERROR DE NEGOCIO] " + ex.getMessage());
            } catch (NumberFormatException ex) {
                System.out.println("[ERROR] El valor ingresado no es un numero valido.");
            } catch (Exception ex) {
                System.out.println("[ERROR INESPERADO] " + ex.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

    private void imprimirBanner() {
        System.out.println("=========================================================");
        System.out.println(" OMPRELA-Boards - Prototipo Java - Integrador Final (TP4)");
        System.out.println(" Universidad Siglo 21 - INF275");
        System.out.println(" Alumno: Chavez Alan Ezequiel - VINF018147");
        System.out.println("=========================================================");
    }

    private void mostrarMenu() {
        System.out.println("\n----------- MENU PRINCIPAL -----------");
        System.out.println("1.  Crear historia de usuario");
        System.out.println("2.  Crear tarea tecnica");
        System.out.println("3.  Listar todos los tickets");
        System.out.println("4.  Listar tickets ordenados por prioridad (quicksort)");
        System.out.println("5.  Filtrar tickets por estado");
        System.out.println("6.  Mover ticket a otro estado");
        System.out.println("7.  Deshacer ultimo movimiento (pila LIFO)");
        System.out.println("8.  Calcular esfuerzo total (polimorfismo)");
        System.out.println("9.  Buscar ticket por id");
        System.out.println("10. Reporte del backlog (arreglos nativos)");
        System.out.println("11. Exportar backlog a archivo CSV (java.io)");
        System.out.println("0.  Salir");
        System.out.print("\nElija una opcion: ");
    }

    private void crearHistoriaUsuario() {
        System.out.println("\n--- Nueva historia de usuario ---");
        System.out.print("Titulo: ");
        String titulo = scanner.nextLine();
        System.out.print("Prioridad (1-5): ");
        int prioridad = Integer.parseInt(scanner.nextLine());
        System.out.printf("Story points %s: ", HistoriaUsuario.FIBONACCI);
        int sp = Integer.parseInt(scanner.nextLine());

        HistoriaUsuario h = new HistoriaUsuario(titulo, prioridad, 1, sp, 1);
        servicio.crear(h);
        System.out.println("[OK] Guardada en MySQL: " + h);
    }

    private void crearTarea() {
        System.out.println("\n--- Nueva tarea tecnica ---");
        System.out.print("Titulo: ");
        String titulo = scanner.nextLine();
        System.out.print("Prioridad (1-5): ");
        int prioridad = Integer.parseInt(scanner.nextLine());
        System.out.print("Horas estimadas: ");
        double horas = Double.parseDouble(scanner.nextLine());

        Tarea t = new Tarea(titulo, prioridad, 1, horas, 1);
        servicio.crear(t);
        System.out.println("[OK] Guardada en MySQL: " + t);
    }

    private void listarTodos() {
        System.out.println("\n--- Todos los tickets (desde MySQL) ---");
        for (Ticket t : servicio.listarOrdenadoPorPrioridad()) {
            System.out.println("  " + t);
        }
    }

    private void listarPorPrioridad() {
        System.out.println("\n--- Tickets ordenados por prioridad (quicksort) ---");
        List<Ticket> ordenados = servicio.listarOrdenadoPorPrioridad();
        for (int i = 0; i < ordenados.size(); i++) {
            System.out.printf("  %2d) %s%n", i + 1, ordenados.get(i));
        }
    }

    private void filtrarPorEstado() {
        System.out.println("\n--- Filtrar por estado ---");
        System.out.print("Estado (POR_HACER, EN_PROGRESO, EN_REVISION, HECHO, CANCELADA): ");
        Estado e = Estado.valueOf(scanner.nextLine().trim().toUpperCase());
        List<Ticket> filtrados = servicio.filtrarPorEstado(e);
        System.out.printf("[%d tickets en estado %s]%n", filtrados.size(), e);
        for (Ticket t : filtrados) {
            System.out.println("  " + t);
        }
    }

    private void moverEstado() {
        System.out.println("\n--- Mover ticket a otro estado ---");
        System.out.print("ID del ticket: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Nuevo estado (POR_HACER, EN_PROGRESO, EN_REVISION, HECHO, CANCELADA): ");
        Estado destino = Estado.valueOf(scanner.nextLine().trim().toUpperCase());

        servicio.moverEstado(id, destino);
        System.out.printf("[OK] Ticket #%d ahora esta en %s (persistido en MySQL)%n", id, destino);
        System.out.printf("     Historial disponible: %d movimientos.%n", servicio.historialDisponible());
    }

    private void deshacerUltimoMovimiento() {
        System.out.println("\n--- Deshacer ultimo movimiento ---");
        if (servicio.deshacerUltimoMovimiento()) {
            System.out.println("[OK] Ultimo movimiento revertido en MySQL.");
        } else {
            System.out.println("[INFO] No hay movimientos por deshacer en esta sesion.");
        }
    }

    private void calcularEsfuerzoTotal() {
        double total = servicio.calcularEsfuerzoTotal();
        System.out.printf("%n[Esfuerzo total del backlog] %.2f horas (sumando polimorficamente)%n", total);
    }

    private void buscarTicket() {
        System.out.print("\nID del ticket a buscar: ");
        int id = Integer.parseInt(scanner.nextLine());
        Ticket t = servicio.buscarPorId(id);
        System.out.println("[Encontrado] " + t);
        System.out.printf("Tipo: %s | Esfuerzo individual: %.2f%n",
            t.getTipo(), t.calcularEsfuerzo());
    }

    /**
     * Opcion 10: genera un reporte estadistico del backlog usando ARREGLOS NATIVOS.
     * Recupera la lista (ArrayList) desde MySQL y la pasa al ReporteBacklog, que
     * internamente usa int[] y double[] para las estadisticas (uso complementario
     * de ArrayList + arreglos).
     */
    private void mostrarReporte() {
        List<Ticket> tickets = servicio.listarOrdenadoPorPrioridad();
        ReporteBacklog reporte = new ReporteBacklog();
        reporte.imprimirReporte(tickets);
    }

    /**
     * Opcion 11: exporta el backlog a un archivo CSV usando java.io, registra el
     * evento en un log y vuelve a leer el archivo para confirmar la escritura.
     */
    private void exportarBacklog() {
        List<Ticket> tickets = servicio.listarOrdenadoPorPrioridad();
        ExportadorArchivos exportador = new ExportadorArchivos("salida");

        String ruta = exportador.exportarCSV(tickets, "backlog.csv");
        exportador.registrarLog("Backlog exportado: " + tickets.size() + " tickets a backlog.csv");

        System.out.println("\n[OK] Backlog exportado a: " + ruta);

        // Releemos el archivo para confirmar (lectura con BufferedReader)
        List<String> lineas = exportador.leerCSV("backlog.csv");
        System.out.printf("[OK] Archivo verificado: %d lineas escritas.%n", lineas.size());
        System.out.println("\n-- Primeras lineas del CSV --");
        int max = Math.min(4, lineas.size());
        for (int i = 0; i < max; i++) {
            System.out.println("  " + lineas.get(i));
        }
    }
}
