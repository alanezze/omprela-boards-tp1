-- =====================================================================
-- V002 - Indices secundarios
-- =====================================================================

CREATE INDEX idx_historias_estado ON historias_usuario(estado);
CREATE INDEX idx_historias_sprint ON historias_usuario(id_sprint);
CREATE INDEX idx_tareas_estado    ON tareas(estado);
CREATE INDEX idx_horas_usuario    ON registro_horas(id_usuario, fecha);
