package com.omprela.boards.api;

import com.omprela.boards.model.HistoriaUsuario;
import com.omprela.boards.model.Tarea;
import com.omprela.boards.model.Ticket;
import com.omprela.boards.model.Ticket.Estado;
import com.omprela.boards.model.excepciones.OmprelaException;
import com.omprela.boards.service.TicketService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para exponer los tickets del sistema via HTTP.
 * <p>
 * Esta clase está pensada para Spring Boot. Las anotaciones {@code @RestController}
 * y {@code @RequestMapping} se aplican cuando se compila con la dependencia de
 * Spring presente; sin ellas, la clase sigue siendo Java válido pero no se
 * registra como endpoint.
 * <p>
 * En el documento se incluyen las anotaciones a título ilustrativo. El código
 * que efectivamente se entrega y compila estandalone es el del paquete
 * {@code view.MainConsola}.
 */
// @RestController
// @RequestMapping("/api/tickets")
public class TicketController {

    // @Autowired
    private final TicketService servicio;

    public TicketController(TicketService servicio) {
        this.servicio = servicio;
    }

    /** GET /api/tickets - lista todos los tickets ordenados por prioridad. */
    // @GetMapping
    public List<Ticket> listarTodos() {
        return servicio.listarOrdenadoPorPrioridad();
    }

    /** GET /api/tickets/{id} - busca un ticket por su id. */
    // @GetMapping("/{id}")
    public Ticket buscar(/*@PathVariable*/ int id) {
        return servicio.buscarPorId(id);
    }

    /** GET /api/tickets/estado/{estado} - filtra por estado. */
    // @GetMapping("/estado/{estado}")
    public List<Ticket> filtrarPorEstado(/*@PathVariable*/ Estado estado) {
        return servicio.filtrarPorEstado(estado);
    }

    /** POST /api/tickets/historia - crea una nueva historia de usuario. */
    // @PostMapping("/historia")
    public HistoriaUsuario crearHistoria(/*@RequestBody*/ HistoriaUsuario h) {
        return servicio.crear(h);
    }

    /** POST /api/tickets/tarea - crea una nueva tarea técnica. */
    // @PostMapping("/tarea")
    public Tarea crearTarea(/*@RequestBody*/ Tarea t) {
        return servicio.crear(t);
    }

    /** PATCH /api/tickets/{id}/estado - mueve un ticket a otro estado. */
    // @PatchMapping("/{id}/estado")
    public Map<String, Object> moverEstado(
            /*@PathVariable*/ int id,
            /*@RequestParam*/ Estado nuevoEstado) {
        servicio.moverEstado(id, nuevoEstado);
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("id", id);
        resultado.put("nuevoEstado", nuevoEstado);
        resultado.put("ok", true);
        return resultado;
    }

    /** POST /api/tickets/deshacer - deshace el último movimiento (pila LIFO). */
    // @PostMapping("/deshacer")
    public Map<String, Object> deshacer() {
        boolean exito = servicio.deshacerUltimoMovimiento();
        Map<String, Object> resp = new HashMap<>();
        resp.put("deshecho", exito);
        return resp;
    }

    /** GET /api/tickets/esfuerzo - calcula el esfuerzo total polimorficamente. */
    // @GetMapping("/esfuerzo")
    public Map<String, Object> esfuerzo() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", servicio.calcularEsfuerzoTotal());
        resp.put("unidad", "horas");
        return resp;
    }

    /**
     * Manejador global de excepciones de negocio.
     * <p>
     * Cualquier {@link OmprelaException} (TransicionEstadoInvalidaException,
     * EntidadNoEncontradaException, ValidacionException) se traduce
     * automaticamente a un HTTP 400 con un cuerpo JSON descriptivo.
     */
    // @ExceptionHandler(OmprelaException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarErrorDeNegocio(OmprelaException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("error", ex.getClass().getSimpleName());
        resp.put("mensaje", ex.getMessage());
        return resp;
    }
}
