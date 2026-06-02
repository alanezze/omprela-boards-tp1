package com.omprela.boards.model.interfaces;

import java.time.LocalDateTime;

/**
 * Contrato que deben cumplir todas las entidades que requieren auditoría
 * automática de su historia (quién y cuándo las creó/modificó por última vez).
 * <p>
 * Aplica el pilar de <b>abstracción</b>: define qué deben proveer las entidades
 * auditables, sin imponer cómo lo hacen. Cualquier clase que implemente esta
 * interfaz puede ser tratada genéricamente por el AuditoriaService.
 */
public interface Auditable {

    /** Retorna la fecha de creación del registro. */
    LocalDateTime getFechaCreacion();

    /** Retorna el identificador del usuario que creó el registro. */
    Integer getIdUsuarioCreador();

    /** Retorna la fecha de la última modificación. */
    LocalDateTime getFechaUltimaModificacion();

    /** Retorna un identificador legible del tipo de entidad (ej: "HistoriaUsuario"). */
    String getNombreEntidad();

    /** Retorna el identificador único de la instancia. */
    Integer getId();
}
