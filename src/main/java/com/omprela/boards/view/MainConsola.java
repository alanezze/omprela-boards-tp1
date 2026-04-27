package com.omprela.boards.view;

import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.model.HistoriaUsuario.Estado;
import com.omprela.boards.model.Proyecto;
import com.omprela.boards.service.HistoriaUsuarioService;
import com.omprela.boards.service.ProyectoService;
import com.omprela.boards.util.DBConnection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola del prototipo OMPRELA-Boards.
 * Funciona como demostracion operacional del modulo de gestion de proyectos
 * y tickets, ejercitando los casos de uso CU02 (Crear proyecto), CU05 (Crear
 * historia de usuario), CU09 (Consultar tablero) y CU10 (Mover ticket de estado).
 *
 * Esta clase actua como Vista + Controlador en el patron MVC simplificado del
 * prototipo. En la version final del sistema, la vista sera una aplicacion web
 * y el controlador estara desacoplado mediante endpoints REST.
 */
public class MainConsola {

    private static final ProyectoService proyectoService = new ProyectoService();
    private static final HistoriaUsuarioService historiaService = new HistoriaUsuarioService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final int USUARIO_OPERADOR_ID = 1; // En la version final viene de la sesion

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println(" OMPRELA-Boards - Prototipo de gestion de proyectos");
        System.out.println(" Universidad Siglo 21 - INF275 - TP1");
        System.out.println("================================================");

        try {
            DBConnection.getConnection();
            System.out.println("[OK] Conexion a MySQL establecida");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo conectar a MySQL: " + e.getMessage());
            return;
        }

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();
            try {
                switch (opcion) {
                    case "1": listarProyectos(); break;
                    case "2": crearProyecto(); break;
                    case "3": listarTableroPorSprint(); break;
                    case "4": crearHistoria(); break;
                    case "5": moverHistoria(); break;
                    case "0": salir = true; break;
                    default: System.out.println("Opcion no valida.");
                }
            } catch (Exception ex) {
                System.err.println("[ERROR] " + ex.getMessage());
            }
            System.out.println();
        }

        DBConnection.close();
        System.out.println("Hasta luego.");
    }

    private static void mostrarMenu() {
        System.out.println("\n----- MENU -----");
        System.out.println("1. Listar proyectos");
        System.out.println("2. Crear nuevo proyecto");
        System.out.println("3. Ver tablero Kanban por sprint");
        System.out.println("4. Crear nueva historia de usuario");
        System.out.println("5. Mover historia de estado");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private static void listarProyectos() throws Exception {
        List<Proyecto> proyectos = proyectoService.listar();
        System.out.println("\n--- Proyectos registrados (" + proyectos.size() + ") ---");
        proyectos.forEach(System.out::println);
    }

    private static void crearProyecto() throws Exception {
        Proyecto p = new Proyecto();
        System.out.print("Nombre: ");
        p.setNombre(scanner.nextLine());
        System.out.print("Descripcion: ");
        p.setDescripcion(scanner.nextLine());
        System.out.print("Fecha inicio (YYYY-MM-DD): ");
        p.setFechaInicio(LocalDate.parse(scanner.nextLine()));
        System.out.print("Fecha fin estimada (YYYY-MM-DD, vacio para omitir): ");
        String fin = scanner.nextLine();
        if (!fin.isEmpty()) p.setFechaFinEstimada(LocalDate.parse(fin));
        System.out.print("Presupuesto: ");
        String pre = scanner.nextLine();
        if (!pre.isEmpty()) p.setPresupuesto(new BigDecimal(pre));
        System.out.print("ID Cliente: ");
        p.setIdCliente(Integer.parseInt(scanner.nextLine()));

        Proyecto creado = proyectoService.crear(p);
        System.out.println("[OK] Proyecto creado con ID " + creado.getIdProyecto());
    }

    private static void listarTableroPorSprint() throws Exception {
        System.out.print("ID del sprint: ");
        int idSprint = Integer.parseInt(scanner.nextLine());
        List<HistoriaUsuario> historias = historiaService.listarPorSprint(idSprint);

        System.out.println("\n--- Tablero Kanban Sprint " + idSprint + " ---");
        for (Estado e : Estado.values()) {
            if (e == Estado.CANCELADA) continue;
            System.out.println("\n[" + e + "]");
            historias.stream()
                .filter(h -> h.getEstado() == e)
                .forEach(h -> System.out.println("  " + h));
        }
    }

    private static void crearHistoria() throws Exception {
        HistoriaUsuario h = new HistoriaUsuario();
        System.out.print("Titulo: ");
        h.setTitulo(scanner.nextLine());
        System.out.print("Descripcion: ");
        h.setDescripcion(scanner.nextLine());
        System.out.print("Criterios de aceptacion: ");
        h.setCriteriosAceptacion(scanner.nextLine());
        System.out.print("Story points: ");
        String sp = scanner.nextLine();
        if (!sp.isEmpty()) h.setStoryPoints(Integer.parseInt(sp));
        System.out.print("Prioridad (1-5): ");
        h.setPrioridad(Integer.parseInt(scanner.nextLine()));
        System.out.print("ID Epica: ");
        h.setIdEpica(Integer.parseInt(scanner.nextLine()));
        System.out.print("ID Sprint (vacio para backlog): ");
        String sprint = scanner.nextLine();
        if (!sprint.isEmpty()) h.setIdSprint(Integer.parseInt(sprint));
        System.out.print("ID Usuario asignado (vacio si no aplica): ");
        String asig = scanner.nextLine();
        if (!asig.isEmpty()) h.setIdUsuarioAsignado(Integer.parseInt(asig));

        HistoriaUsuario creada = historiaService.crear(h);
        System.out.println("[OK] Historia creada con ID " + creada.getIdHistoria());
    }

    private static void moverHistoria() throws Exception {
        System.out.print("ID Historia: ");
        int idH = Integer.parseInt(scanner.nextLine());
        System.out.print("Nuevo estado (POR_HACER, EN_PROGRESO, EN_REVISION, HECHO, CANCELADA): ");
        Estado destino = Estado.valueOf(scanner.nextLine().trim().toUpperCase());

        boolean ok = historiaService.moverEstado(idH, destino, USUARIO_OPERADOR_ID);
        System.out.println(ok ? "[OK] Estado actualizado y auditoria registrada"
                              : "[FALLO] No se pudo actualizar");
    }
}
