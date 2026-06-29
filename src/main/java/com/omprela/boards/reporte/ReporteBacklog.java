package com.omprela.boards.reporte;

import com.omprela.boards.model.Ticket;
import com.omprela.boards.model.Ticket.Estado;

import java.util.List;

/**
 * Genera reportes estadisticos del backlog usando ARREGLOS NATIVOS de Java,
 * complementando los ArrayList que utiliza el resto del sistema.
 * <p>
 * Esta clase demuestra el uso COMPLEMENTARIO de las dos estructuras que pide
 * la consigna del TP4:
 * <ul>
 *   <li><b>ArrayList</b> (List&lt;Ticket&gt;): lo recibe como entrada desde el
 *       servicio, porque la cantidad de tickets es dinamica y no se conoce de
 *       antemano.</li>
 *   <li><b>Arreglos nativos</b> (int[], double[], String[]): los usa internamente
 *       para las estadisticas, porque la cantidad de estados y de niveles de
 *       prioridad es FIJA y conocida (5 estados, 5 prioridades). Un arreglo de
 *       tamano fijo es mas eficiente que un ArrayList para este caso.</li>
 * </ul>
 * <p>
 * Es el caso de uso ideal para arreglos: cuando la dimension es conocida y
 * constante, el arreglo nativo evita el overhead del ArrayList.
 */
public class ReporteBacklog {

    // Nombres de los estados, en arreglo de tamano fijo (paralelo al enum)
    private static final String[] NOMBRES_ESTADOS = {
        "POR_HACER", "EN_PROGRESO", "EN_REVISION", "HECHO", "CANCELADA"
    };

    /**
     * Cuenta cuantos tickets hay en cada estado, devolviendo un ARREGLO de enteros
     * indexado por el ordinal del enum Estado.
     * @param tickets lista (ArrayList) de tickets a analizar
     * @return arreglo int[5] donde cada posicion es la cantidad de tickets en ese estado
     */
    public int[] contarPorEstado(List<Ticket> tickets) {
        // Arreglo nativo de tamano fijo: 5 estados posibles
        int[] conteo = new int[Estado.values().length];

        // Recorremos el ArrayList y volcamos los conteos en el arreglo
        for (Ticket t : tickets) {
            int indice = t.getEstado().ordinal();
            conteo[indice]++;
        }
        return conteo;
    }

    /**
     * Calcula el esfuerzo total acumulado por nivel de prioridad (1 a 5).
     * @return arreglo double[6] donde el indice = prioridad (se ignora la posicion 0)
     */
    public double[] esfuerzoPorPrioridad(List<Ticket> tickets) {
        // Arreglo de 6 posiciones (indices 0..5); usamos 1..5 para las prioridades
        double[] esfuerzo = new double[6];

        for (Ticket t : tickets) {
            int prio = t.getPrioridad();
            esfuerzo[prio] += t.calcularEsfuerzo();
        }
        return esfuerzo;
    }

    /**
     * Devuelve el estado con mayor cantidad de tickets (el "cuello de botella").
     * Demuestra el recorrido de un arreglo nativo para encontrar el maximo.
     */
    public String estadoConMasTickets(List<Ticket> tickets) {
        int[] conteo = contarPorEstado(tickets);
        int idxMax = 0;
        for (int i = 1; i < conteo.length; i++) {
            if (conteo[i] > conteo[idxMax]) {
                idxMax = i;
            }
        }
        return NOMBRES_ESTADOS[idxMax];
    }

    /**
     * Imprime un reporte tabular del backlog usando los arreglos calculados.
     */
    public void imprimirReporte(List<Ticket> tickets) {
        int[] conteo = contarPorEstado(tickets);
        double[] esfuerzo = esfuerzoPorPrioridad(tickets);

        System.out.println("\n========== REPORTE DEL BACKLOG ==========");
        System.out.println("\n-- Tickets por estado --");
        // Recorremos el arreglo con for clasico usando el indice
        for (int i = 0; i < conteo.length; i++) {
            System.out.printf("  %-14s : %d tickets%n", NOMBRES_ESTADOS[i], conteo[i]);
        }

        System.out.println("\n-- Esfuerzo por prioridad (horas) --");
        for (int prio = 1; prio <= 5; prio++) {
            System.out.printf("  Prioridad %d : %.1f h%n", prio, esfuerzo[prio]);
        }

        System.out.println("\n-- Indicador --");
        System.out.printf("  Estado con mas tickets: %s%n", estadoConMasTickets(tickets));
        System.out.println("=========================================");
    }
}
