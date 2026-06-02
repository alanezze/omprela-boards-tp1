package com.omprela.boards.model;

import com.omprela.boards.model.excepciones.ValidacionException;

/**
 * Tarea técnica: subdivisión operativa de una Historia de Usuario.
 * <p>
 * <b>Herencia</b>: extiende de {@link Ticket} compartiendo el flujo de estados y
 * la auditoría con HistoriaUsuario. La diferencia clave es que el esfuerzo se mide
 * directamente en horas (no en story points), porque las tareas son ejecutables
 * por un único desarrollador y caben dentro de un día de trabajo.
 * <p>
 * <b>Polimorfismo</b>: sobrescribe {@code getTipo()} y {@code calcularEsfuerzo()}
 * con la semántica propia de las tareas técnicas.
 */
public class Tarea extends Ticket {

    private Double horasEstimadas;
    private Double horasReales;
    private Integer idHistoria;

    public Tarea(String titulo, Integer prioridad, Integer idUsuarioCreador,
                 Double horasEstimadas, Integer idHistoria) {
        super(titulo, prioridad, idUsuarioCreador);
        setHorasEstimadas(horasEstimadas);
        if (idHistoria == null) {
            throw new ValidacionException("idHistoria",
                "la tarea debe pertenecer a una historia");
        }
        this.idHistoria = idHistoria;
        this.horasReales = 0.0;
    }

    public Tarea() {
        super();
        this.horasReales = 0.0;
    }

    @Override
    public String getTipo() {
        return "TAREA_TECNICA";
    }

    /**
     * El esfuerzo de una tarea es directamente sus horas estimadas.
     * Si la tarea está terminada y tiene horas reales registradas, devuelve esas.
     */
    @Override
    public double calcularEsfuerzo() {
        if (getEstado() == Estado.HECHO && horasReales != null && horasReales > 0) {
            return horasReales;
        }
        return horasEstimadas == null ? 0.0 : horasEstimadas;
    }

    /**
     * Registra horas trabajadas sobre la tarea. Las horas se acumulan.
     */
    public void registrarHoras(double horas) {
        if (horas <= 0) {
            throw new ValidacionException("horas", "las horas deben ser positivas");
        }
        if (horas > 24) {
            throw new ValidacionException("horas", "no se pueden registrar más de 24 horas en un solo registro");
        }
        if (this.horasReales == null) this.horasReales = 0.0;
        this.horasReales += horas;
    }

    @Override
    public String toString() {
        return String.format("[Tarea #%d] %s | est:%.1fh | real:%.1fh | %s",
            getId(), getTitulo(),
            horasEstimadas == null ? 0.0 : horasEstimadas,
            horasReales == null ? 0.0 : horasReales,
            getEstado());
    }

    public Double getHorasEstimadas() { return horasEstimadas; }
    public void setHorasEstimadas(Double horasEstimadas) {
        if (horasEstimadas != null && horasEstimadas <= 0) {
            throw new ValidacionException("horasEstimadas",
                "las horas estimadas deben ser positivas");
        }
        this.horasEstimadas = horasEstimadas;
    }

    public Double getHorasReales() { return horasReales; }
    public void setHorasReales(Double hr) { this.horasReales = hr; }

    public Integer getIdHistoria() { return idHistoria; }
    public void setIdHistoria(Integer idHistoria) { this.idHistoria = idHistoria; }
}
