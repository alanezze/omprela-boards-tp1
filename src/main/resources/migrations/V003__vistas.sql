-- =====================================================================
-- V003 - Vistas analiticas
-- =====================================================================

CREATE OR REPLACE VIEW v_backlog_priorizado AS
SELECT p.id_proyecto, p.nombre AS proyecto, e.titulo AS epica,
       h.id_historia, h.titulo AS historia, h.prioridad, h.story_points,
       h.estado, h.id_sprint
FROM proyectos p
INNER JOIN epicas e ON e.id_proyecto = p.id_proyecto
INNER JOIN historias_usuario h ON h.id_epica = e.id_epica
ORDER BY p.id_proyecto, h.prioridad ASC, h.id_historia ASC;

CREATE OR REPLACE VIEW v_velocity_proyecto AS
SELECT s.id_proyecto, s.numero AS sprint, s.estado,
       COALESCE(SUM(h.story_points), 0) AS puntos_completados
FROM sprints s
LEFT JOIN historias_usuario h
       ON h.id_sprint = s.id_sprint
      AND h.estado = 'HECHO'
GROUP BY s.id_proyecto, s.id_sprint, s.numero, s.estado
ORDER BY s.id_proyecto, s.numero;
