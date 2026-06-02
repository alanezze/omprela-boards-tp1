-- =====================================================================
-- OMPRELA-Boards - Esquema del prototipo POO (TP3)
-- Universidad Siglo 21 - Seminario de Práctica de Informática (INF275)
-- Alumno: Chavez Alan Ezequiel - Legajo VINF018147
-- =====================================================================
-- Este script crea una tabla unificada 'tickets' que refleja la jerarquía
-- de herencia del modelo POO (Ticket -> HistoriaUsuario / Tarea) usando la
-- estrategia "Single Table Inheritance": una sola tabla con una columna
-- discriminadora 'tipo' que indica la subclase concreta.
--
-- Ejecutar este script ANTES de correr el prototipo:
--   mysql -u root -p < 03_create_tickets_tp3.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS omprela_boards
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE omprela_boards;

-- Tabla unificada de tickets (herencia single-table)
CREATE TABLE IF NOT EXISTS tickets (
    id              INT NOT NULL AUTO_INCREMENT,
    tipo            VARCHAR(20)  NOT NULL,           -- 'HISTORIA' o 'TAREA' (discriminador)
    titulo          VARCHAR(150) NOT NULL,
    descripcion     TEXT,
    prioridad       INT          NOT NULL DEFAULT 3,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'POR_HACER',
    -- Campos propios de HistoriaUsuario (NULL para tareas):
    story_points    INT,
    -- Campos propios de Tarea (NULL para historias):
    horas_estimadas DECIMAL(6,2),
    horas_reales    DECIMAL(6,2) DEFAULT 0,
    -- Auditoría:
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tickets PRIMARY KEY (id),
    CONSTRAINT chk_tickets_tipo CHECK (tipo IN ('HISTORIA','TAREA')),
    CONSTRAINT chk_tickets_estado CHECK (estado IN
        ('POR_HACER','EN_PROGRESO','EN_REVISION','HECHO','CANCELADA')),
    CONSTRAINT chk_tickets_prioridad CHECK (prioridad BETWEEN 1 AND 5)
);

-- Tabla de auditoría de cambios de estado (RF12)
CREATE TABLE IF NOT EXISTS log_movimientos (
    id_log          INT NOT NULL AUTO_INCREMENT,
    id_ticket       INT NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20),
    fecha_evento    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_log PRIMARY KEY (id_log),
    CONSTRAINT fk_log_ticket FOREIGN KEY (id_ticket)
        REFERENCES tickets(id) ON DELETE CASCADE
);

-- Índice para acelerar el filtrado por estado (tablero Kanban)
CREATE INDEX idx_tickets_estado ON tickets(estado);

-- =====================================================================
-- Datos de prueba iniciales (se cargan solo si la tabla está vacía)
-- =====================================================================
INSERT INTO tickets (tipo, titulo, prioridad, estado, story_points)
SELECT * FROM (
    SELECT 'HISTORIA','Login de usuarios', 1, 'POR_HACER', 3 UNION ALL
    SELECT 'HISTORIA','CRUD de proyectos', 1, 'POR_HACER', 5 UNION ALL
    SELECT 'HISTORIA','Tablero Kanban',    1, 'POR_HACER', 8
) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM tickets);

INSERT INTO tickets (tipo, titulo, prioridad, estado, horas_estimadas)
SELECT * FROM (
    SELECT 'TAREA','Disenar tabla usuarios',        2, 'POR_HACER', 2.0 UNION ALL
    SELECT 'TAREA','Implementar endpoint POST login',1, 'POR_HACER', 4.0 UNION ALL
    SELECT 'TAREA','Validacion bcrypt',             3, 'POR_HACER', 3.0
) AS nuevos
WHERE (SELECT COUNT(*) FROM tickets) = 3;
