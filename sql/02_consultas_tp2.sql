-- =====================================================================
-- OMPRELA-Boards - Consultas SQL del Trabajo Práctico N° 2
-- Universidad Siglo 21 - Seminario de Práctica de Informática (INF275)
-- Alumno: Chavez Alan Ezequiel - Legajo VINF018147
-- =====================================================================
-- Estas consultas complementan el script 01_create_omprela_boards.sql
-- y se documentan en la sección 17 del documento TP2.
-- Para ejecutarlas, primero correr 01_create_omprela_boards.sql.
-- =====================================================================

USE omprela_boards;

-- =====================================================================
-- Consulta 1 — Tablero Kanban del sprint activo
-- =====================================================================
SELECT estado, COUNT(*) AS cantidad,
       SUM(COALESCE(story_points, 0)) AS puntos_totales
FROM historias_usuario
WHERE id_sprint = 3
GROUP BY estado
ORDER BY FIELD(estado, 'POR_HACER','EN_PROGRESO','EN_REVISION','HECHO');

-- =====================================================================
-- Consulta 2 — Velocity histórica por proyecto
-- =====================================================================
SELECT s.numero AS sprint, s.estado,
       COALESCE(SUM(h.story_points), 0) AS puntos_completados
FROM sprints s
LEFT JOIN historias_usuario h
       ON h.id_sprint = s.id_sprint
      AND h.estado = 'HECHO'
WHERE s.id_proyecto = 3
GROUP BY s.id_sprint, s.numero, s.estado
ORDER BY s.numero;

-- =====================================================================
-- Consulta 3 — Carga de trabajo por desarrollador (últimos 30 días)
-- =====================================================================
SELECT u.nombre, u.apellido,
       COUNT(rh.id_registro) AS dias_cargados,
       SUM(rh.cantidad_horas) AS total_horas
FROM usuarios u
LEFT JOIN registro_horas rh
       ON rh.id_usuario = u.id_usuario
      AND rh.fecha >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
WHERE u.rol IN ('DESARROLLADOR','TECH_LEAD')
GROUP BY u.id_usuario, u.nombre, u.apellido
ORDER BY total_horas DESC;

-- =====================================================================
-- Consulta 4 — Historias bloqueadas (sin movimiento hace más de 5 días)
-- =====================================================================
SELECT h.id_historia, h.titulo, h.estado,
       u.nombre AS asignado,
       MAX(la.fecha_evento) AS ultimo_movimiento
FROM historias_usuario h
INNER JOIN log_auditoria la
       ON la.id_entidad = h.id_historia
      AND la.entidad = 'HistoriaUsuario'
LEFT JOIN usuarios u ON u.id_usuario = h.id_usuario_asignado
WHERE h.estado IN ('EN_PROGRESO','EN_REVISION')
GROUP BY h.id_historia, h.titulo, h.estado, u.nombre
HAVING ultimo_movimiento < DATE_SUB(NOW(), INTERVAL 5 DAY);

-- =====================================================================
-- Consulta 5 — Top épicas por cantidad de historias
-- =====================================================================
SELECT e.id_epica, e.titulo, p.nombre AS proyecto,
       COUNT(h.id_historia) AS cant_historias,
       SUM(h.story_points) AS total_puntos
FROM epicas e
INNER JOIN proyectos p ON p.id_proyecto = e.id_proyecto
LEFT JOIN historias_usuario h ON h.id_epica = e.id_epica
GROUP BY e.id_epica, e.titulo, p.nombre
ORDER BY cant_historias DESC
LIMIT 10;

-- =====================================================================
-- Consulta 6 — Diferencia entre horas estimadas y reales por historia
-- =====================================================================
SELECT h.id_historia, h.titulo,
       SUM(t.horas_estimadas) AS estimadas,
       SUM(t.horas_reales)    AS reales,
       SUM(t.horas_reales) - SUM(t.horas_estimadas) AS diferencia
FROM historias_usuario h
INNER JOIN tareas t ON t.id_historia = h.id_historia
WHERE h.estado = 'HECHO'
GROUP BY h.id_historia, h.titulo
ORDER BY ABS(diferencia) DESC;

-- =====================================================================
-- Consulta 7 — Auditoría de cambios de estado de una historia específica
-- =====================================================================
SELECT la.fecha_evento, la.valor_anterior, la.valor_nuevo,
       CONCAT(u.nombre,' ',u.apellido) AS operador
FROM log_auditoria la
INNER JOIN usuarios u ON u.id_usuario = la.id_usuario
WHERE la.entidad = 'HistoriaUsuario'
  AND la.id_entidad = 1
  AND la.accion = 'CAMBIO_ESTADO'
ORDER BY la.fecha_evento ASC;

-- =====================================================================
-- Consulta 8 — Proyectos activos con presupuesto consumido (estimado)
-- =====================================================================
SELECT p.id_proyecto, p.nombre, p.presupuesto,
       SUM(rh.cantidad_horas * 12000) AS costo_horas_aprox
FROM proyectos p
INNER JOIN epicas e ON e.id_proyecto = p.id_proyecto
INNER JOIN historias_usuario h ON h.id_epica = e.id_epica
INNER JOIN tareas t ON t.id_historia = h.id_historia
INNER JOIN registro_horas rh ON rh.id_tarea = t.id_tarea
WHERE p.estado = 'ACTIVO'
  AND rh.aprobado = TRUE
GROUP BY p.id_proyecto, p.nombre, p.presupuesto;

-- =====================================================================
-- Consulta 9 — Comentarios recientes (últimos 7 días)
-- =====================================================================
SELECT c.id_comentario, c.texto, c.fecha_creacion,
       CONCAT(u.nombre,' ',u.apellido) AS autor,
       h.titulo AS historia
FROM comentarios c
INNER JOIN usuarios u ON u.id_usuario = c.id_autor
INNER JOIN historias_usuario h ON h.id_historia = c.id_historia
WHERE c.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY c.fecha_creacion DESC
LIMIT 20;

-- =====================================================================
-- Consulta 10 — Distribución de prioridades en el backlog
-- =====================================================================
SELECT prioridad,
       COUNT(*) AS cant_historias,
       ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) AS porcentaje
FROM historias_usuario
WHERE estado IN ('POR_HACER','EN_PROGRESO')
GROUP BY prioridad
ORDER BY prioridad;
