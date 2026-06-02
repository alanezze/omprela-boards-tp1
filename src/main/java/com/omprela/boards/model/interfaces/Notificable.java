package com.omprela.boards.model.interfaces;

/**
 * Contrato para entidades que pueden disparar notificaciones por correo cuando
 * ocurren ciertos eventos sobre ellas (cambio de estado, asignación, comentario).
 * <p>
 * Aplica el pilar de <b>abstracción</b>: el módulo de notificaciones no necesita
 * conocer qué clase concreta lo invocó; solo necesita el destinatario, el asunto
 * y el mensaje.
 */
public interface Notificable {

    /** Retorna el email del usuario que debe recibir la notificación. */
    String getEmailDestinatario();

    /** Retorna el asunto de la notificación. */
    String getAsuntoNotificacion();

    /** Retorna el cuerpo del mensaje de la notificación. */
    String getMensajeNotificacion();
}
