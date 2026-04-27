-- =====================================================================
-- OMPRELA-Boards - Script de creacion de base de datos
-- Universidad Siglo 21 - Seminario de Practica de Informatica (INF275)
-- Alumno: Chavez Alan Ezequiel - Legajo VINF018147
-- Motor: MySQL 8.0
-- =====================================================================

DROP DATABASE IF EXISTS omprela_boards;
CREATE DATABASE omprela_boards CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE omprela_boards;

-- =====================================================================
-- TABLA: clientes
-- =====================================================================
CREATE TABLE clientes (
    id_cliente       INT NOT NULL AUTO_INCREMENT,
    razon_social     VARCHAR(150) NOT NULL,
    contacto         VARCHAR(100),
    email            VARCHAR(120) NOT NULL,
    telefono         VARCHAR(30),
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_clientes PRIMARY KEY (id_cliente),
    CONSTRAINT uq_clientes_email UNIQUE (email)
);

-- =====================================================================
-- TABLA: usuarios
-- Roles posibles: DESARROLLADOR, TECH_LEAD, PRODUCT_OWNER, PRODUCT_MANAGER, ADMINISTRADOR
-- =====================================================================
CREATE TABLE usuarios (
    id_usuario       INT NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(80)  NOT NULL,
    apellido         VARCHAR(80)  NOT NULL,
    email            VARCHAR(120) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    rol              VARCHAR(30)  NOT NULL,
    fecha_ingreso    DATE NOT NULL,
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN
        ('DESARROLLADOR','TECH_LEAD','PRODUCT_OWNER','PRODUCT_MANAGER','ADMINISTRADOR'))
);

-- =====================================================================
-- TABLA: proyectos
-- =====================================================================
CREATE TABLE proyectos (
    id_proyecto         INT NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(120) NOT NULL,
    descripcion         TEXT,
    fecha_inicio        DATE NOT NULL,
    fecha_fin_estimada  DATE,
    estado              VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    presupuesto         DECIMAL(14,2),
    id_cliente          INT NOT NULL,
    CONSTRAINT pk_proyectos PRIMARY KEY (id_proyecto),
    CONSTRAINT fk_proyectos_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),
    CONSTRAINT chk_proyectos_estado CHECK (estado IN
        ('ACTIVO','PAUSADO','FINALIZADO','CANCELADO'))
);

-- =====================================================================
-- TABLA: epicas
-- =====================================================================
CREATE TABLE epicas (
    id_epica          INT NOT NULL AUTO_INCREMENT,
    titulo            VARCHAR(150) NOT NULL,
    descripcion       TEXT,
    prioridad         INT NOT NULL DEFAULT 3,
    objetivo_negocio  VARCHAR(255),
    estado            VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    id_proyecto       INT NOT NULL,
    CONSTRAINT pk_epicas PRIMARY KEY (id_epica),
    CONSTRAINT fk_epicas_proyecto FOREIGN KEY (id_proyecto)
        REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT chk_epicas_prioridad CHECK (prioridad BETWEEN 1 AND 5)
);

-- =====================================================================
-- TABLA: sprints
-- =====================================================================
CREATE TABLE sprints (
    id_sprint        INT NOT NULL AUTO_INCREMENT,
    numero           INT NOT NULL,
    objetivo         VARCHAR(255),
    fecha_inicio     DATE NOT NULL,
    fecha_fin        DATE NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    velocity         DECIMAL(6,2),
    id_proyecto      INT NOT NULL,
    CONSTRAINT pk_sprints PRIMARY KEY (id_sprint),
    CONSTRAINT fk_sprints_proyecto FOREIGN KEY (id_proyecto)
        REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT chk_sprints_estado CHECK (estado IN
        ('PLANIFICADO','ACTIVO','CERRADO')),
    CONSTRAINT chk_sprints_fechas CHECK (fecha_fin >= fecha_inicio)
);

-- =====================================================================
-- TABLA: historias_usuario (tickets)
-- =====================================================================
CREATE TABLE historias_usuario (
    id_historia          INT NOT NULL AUTO_INCREMENT,
    titulo               VARCHAR(150) NOT NULL,
    descripcion          TEXT,
    criterios_aceptacion TEXT,
    story_points         INT,
    prioridad            INT NOT NULL DEFAULT 3,
    estado               VARCHAR(20) NOT NULL DEFAULT 'POR_HACER',
    fecha_creacion       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre         DATETIME,
    id_epica             INT NOT NULL,
    id_sprint            INT,
    id_usuario_asignado  INT,
    CONSTRAINT pk_historias PRIMARY KEY (id_historia),
    CONSTRAINT fk_historias_epica FOREIGN KEY (id_epica)
        REFERENCES epicas(id_epica),
    CONSTRAINT fk_historias_sprint FOREIGN KEY (id_sprint)
        REFERENCES sprints(id_sprint),
    CONSTRAINT fk_historias_usuario FOREIGN KEY (id_usuario_asignado)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT chk_historias_estado CHECK (estado IN
        ('POR_HACER','EN_PROGRESO','EN_REVISION','HECHO','CANCELADA')),
    CONSTRAINT chk_historias_prioridad CHECK (prioridad BETWEEN 1 AND 5),
    CONSTRAINT chk_historias_points CHECK (story_points IS NULL OR story_points > 0)
);

-- =====================================================================
-- TABLA: tareas
-- =====================================================================
CREATE TABLE tareas (
    id_tarea             INT NOT NULL AUTO_INCREMENT,
    titulo               VARCHAR(150) NOT NULL,
    descripcion          TEXT,
    horas_estimadas      DECIMAL(6,2),
    horas_reales         DECIMAL(6,2) DEFAULT 0,
    estado               VARCHAR(20) NOT NULL DEFAULT 'POR_HACER',
    id_historia          INT NOT NULL,
    id_usuario_asignado  INT,
    CONSTRAINT pk_tareas PRIMARY KEY (id_tarea),
    CONSTRAINT fk_tareas_historia FOREIGN KEY (id_historia)
        REFERENCES historias_usuario(id_historia) ON DELETE CASCADE,
    CONSTRAINT fk_tareas_usuario FOREIGN KEY (id_usuario_asignado)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT chk_tareas_estado CHECK (estado IN
        ('POR_HACER','EN_PROGRESO','EN_REVISION','HECHO'))
);

-- =====================================================================
-- TABLA: registro_horas
-- =====================================================================
CREATE TABLE registro_horas (
    id_registro      INT NOT NULL AUTO_INCREMENT,
    fecha            DATE NOT NULL,
    cantidad_horas   DECIMAL(5,2) NOT NULL,
    descripcion      VARCHAR(255),
    aprobado         BOOLEAN NOT NULL DEFAULT FALSE,
    id_usuario       INT NOT NULL,
    id_tarea         INT,
    id_historia      INT,
    CONSTRAINT pk_registro_horas PRIMARY KEY (id_registro),
    CONSTRAINT fk_horas_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_horas_tarea FOREIGN KEY (id_tarea)
        REFERENCES tareas(id_tarea),
    CONSTRAINT fk_horas_historia FOREIGN KEY (id_historia)
        REFERENCES historias_usuario(id_historia),
    CONSTRAINT chk_horas_cantidad CHECK (cantidad_horas > 0 AND cantidad_horas <= 24)
);

-- =====================================================================
-- TABLA: comentarios
-- =====================================================================
CREATE TABLE comentarios (
    id_comentario     INT NOT NULL AUTO_INCREMENT,
    texto             TEXT NOT NULL,
    fecha_creacion    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editado           BOOLEAN NOT NULL DEFAULT FALSE,
    id_autor          INT NOT NULL,
    id_historia       INT NOT NULL,
    CONSTRAINT pk_comentarios PRIMARY KEY (id_comentario),
    CONSTRAINT fk_coment_autor FOREIGN KEY (id_autor)
        REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_coment_historia FOREIGN KEY (id_historia)
        REFERENCES historias_usuario(id_historia) ON DELETE CASCADE
);

-- =====================================================================
-- TABLA: log_auditoria (audita transiciones de estado y cambios criticos)
-- =====================================================================
CREATE TABLE log_auditoria (
    id_log           INT NOT NULL AUTO_INCREMENT,
    fecha_evento     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entidad          VARCHAR(40) NOT NULL,
    id_entidad       INT NOT NULL,
    accion           VARCHAR(40) NOT NULL,
    valor_anterior   VARCHAR(255),
    valor_nuevo      VARCHAR(255),
    id_usuario       INT NOT NULL,
    CONSTRAINT pk_log PRIMARY KEY (id_log),
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

-- =====================================================================
-- INDICES
-- =====================================================================
CREATE INDEX idx_historias_estado ON historias_usuario(estado);
CREATE INDEX idx_historias_sprint ON historias_usuario(id_sprint);
CREATE INDEX idx_tareas_estado    ON tareas(estado);
CREATE INDEX idx_horas_usuario    ON registro_horas(id_usuario, fecha);

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================
INSERT INTO clientes (razon_social, contacto, email, telefono) VALUES
('Distribuidora Norte S.A.', 'Lucia Romero', 'lromero@dnorte.com', '011-4555-1234'),
('Cooperativa AgroSur',      'Martin Gomez', 'mgomez@agrosur.coop', '0291-456-7788'),
('OMPRELA Software',         'Interno',      'interno@omprela.com.ar', NULL);

INSERT INTO usuarios (nombre, apellido, email, password_hash, rol, fecha_ingreso) VALUES
('Alan',     'Chavez',    'achavez@omprela.com.ar', '$2a$10$placeholderhash01', 'TECH_LEAD',       '2024-02-01'),
('Maria',    'Lopez',     'mlopez@omprela.com.ar',  '$2a$10$placeholderhash02', 'DESARROLLADOR',   '2024-05-15'),
('Carlos',   'Diaz',      'cdiaz@omprela.com.ar',   '$2a$10$placeholderhash03', 'DESARROLLADOR',   '2024-08-20'),
('Sofia',    'Fernandez', 'sfernandez@omprela.com.ar','$2a$10$placeholderhash04', 'PRODUCT_MANAGER', '2024-03-10'),
('Pablo',    'Suarez',    'psuarez@omprela.com.ar', '$2a$10$placeholderhash05', 'PRODUCT_OWNER',   '2025-01-08'),
('Laura',    'Gimenez',   'lgimenez@omprela.com.ar','$2a$10$placeholderhash06', 'ADMINISTRADOR',   '2023-09-01');

INSERT INTO proyectos (nombre, descripcion, fecha_inicio, fecha_fin_estimada, estado, presupuesto, id_cliente) VALUES
('Portal de pedidos B2B',     'Plataforma web para que distribuidoras realicen pedidos online', '2026-01-15', '2026-08-30', 'ACTIVO',     1850000.00, 1),
('App de trazabilidad agro',  'Aplicacion movil para seguimiento de lotes',                    '2026-02-01', '2026-10-15', 'ACTIVO',     2400000.00, 2),
('OMPRELA-Boards (interno)',  'Sistema interno de gestion de proyectos y tareas',              '2026-03-01', '2026-12-15', 'ACTIVO',      950000.00, 3);

INSERT INTO epicas (titulo, descripcion, prioridad, objetivo_negocio, estado, id_proyecto) VALUES
('Modulo de catalogo y carrito',   'Permitir buscar productos y armar pedidos',           1, 'Aumentar ventas online',         'EN_PROGRESO', 1),
('Modulo de pagos',                'Integracion con pasarelas de pago',                   2, 'Reducir cobranza manual',        'ABIERTA',     1),
('Captura de datos en planta',     'Lectura de QR y carga offline',                       1, 'Trazabilidad de lotes',          'EN_PROGRESO', 2),
('Tablero Kanban (vista Dev)',     'Tablero arrastrable con columnas configurables',      1, 'Operatividad del equipo dev',    'EN_PROGRESO', 3),
('Backlog priorizado (vista Prod)','Vista jerarquica de epicas, historias y tareas',      1, 'Planificacion del PM',           'ABIERTA',     3);

INSERT INTO sprints (numero, objetivo, fecha_inicio, fecha_fin, estado, velocity, id_proyecto) VALUES
(1, 'Setup inicial y autenticacion',         '2026-03-03', '2026-03-16', 'CERRADO', 18.0, 3),
(2, 'CRUD de proyectos y tickets',           '2026-03-17', '2026-03-30', 'CERRADO', 21.5, 3),
(3, 'Tablero Kanban operativo',              '2026-03-31', '2026-04-13', 'ACTIVO',  NULL, 3);

INSERT INTO historias_usuario (titulo, descripcion, criterios_aceptacion, story_points, prioridad, estado, id_epica, id_sprint, id_usuario_asignado) VALUES
('Login de usuarios',           'Iniciar sesion con email y contrasena',  'Login valido / invalido / bloqueo tras 5 intentos',  3, 1, 'HECHO',       4, 1, 2),
('CRUD de proyectos',           'Alta, baja, modificacion y consulta',    'Validaciones de campos / paginacion en listado',     5, 1, 'HECHO',       4, 2, 3),
('CRUD de tickets',             'ABM de historias de usuario',            'Asignacion / cambio de estado / comentarios',        8, 1, 'HECHO',       4, 2, 2),
('Tablero Kanban basico',       'Visualizar tickets por columnas',        'Drag and drop entre columnas activas',               5, 1, 'EN_PROGRESO', 4, 3, 3),
('Registro de horas por ticket','Cargar horas dedicadas',                 'Suma diaria / validacion 24h',                       3, 2, 'POR_HACER',   4, 3, 2);

INSERT INTO tareas (titulo, descripcion, horas_estimadas, horas_reales, estado, id_historia, id_usuario_asignado) VALUES
('Disenar tabla usuarios',     'DDL en MySQL',                  2.0, 2.0, 'HECHO',       1, 1),
('Endpoint POST /login',       'Controller + service',          4.0, 5.0, 'HECHO',       1, 2),
('Validacion bcrypt',          'Hashing de password',           3.0, 2.5, 'HECHO',       1, 2),
('Modelo Proyecto + DAO',      'Clase Proyecto y ProyectoDAO',  5.0, 4.5, 'HECHO',       2, 3),
('Vista listado de proyectos', 'Listado paginado',              4.0, 3.0, 'HECHO',       2, 3),
('Modelo Historia + DAO',      'Clase Historia y HistoriaDAO',  6.0, 5.5, 'HECHO',       3, 2),
('Componente Kanban',          'Drag and drop',                 8.0, 4.0, 'EN_PROGRESO', 4, 3);

INSERT INTO registro_horas (fecha, cantidad_horas, descripcion, aprobado, id_usuario, id_tarea, id_historia) VALUES
('2026-03-04', 2.0, 'DDL inicial', TRUE, 1, 1, 1),
('2026-03-05', 5.0, 'Endpoint login', TRUE, 2, 2, 1),
('2026-03-06', 2.5, 'Hashing bcrypt', TRUE, 2, 3, 1),
('2026-03-18', 4.5, 'Proyecto DAO', TRUE, 3, 4, 2),
('2026-03-19', 3.0, 'Listado paginado', TRUE, 3, 5, 2),
('2026-03-25', 5.5, 'Historia DAO', TRUE, 2, 6, 3),
('2026-04-02', 4.0, 'Avance Kanban', FALSE, 3, 7, 4);

INSERT INTO comentarios (texto, id_autor, id_historia) VALUES
('Recordar validar formato de email antes del hash', 1, 1),
('Listo, agregue test unitario para login fallido', 2, 1),
('La paginacion deberia ser por defecto 20 items', 4, 2);

-- Vista util: backlog priorizado por proyecto
CREATE OR REPLACE VIEW v_backlog_priorizado AS
SELECT p.id_proyecto, p.nombre AS proyecto, e.titulo AS epica,
       h.id_historia, h.titulo AS historia, h.prioridad, h.story_points,
       h.estado, h.id_sprint
FROM proyectos p
INNER JOIN epicas e ON e.id_proyecto = p.id_proyecto
INNER JOIN historias_usuario h ON h.id_epica = e.id_epica
ORDER BY p.id_proyecto, h.prioridad ASC, h.id_historia ASC;

-- Vista util: velocity historica por proyecto
CREATE OR REPLACE VIEW v_velocity_proyecto AS
SELECT s.id_proyecto, s.numero AS sprint, s.estado,
       COALESCE(SUM(h.story_points), 0) AS puntos_completados
FROM sprints s
LEFT JOIN historias_usuario h
       ON h.id_sprint = s.id_sprint
      AND h.estado = 'HECHO'
GROUP BY s.id_proyecto, s.id_sprint, s.numero, s.estado
ORDER BY s.id_proyecto, s.numero;

COMMIT;

-- Verificacion de carga
SELECT 'clientes'           AS tabla, COUNT(*) AS registros FROM clientes
UNION ALL SELECT 'usuarios',           COUNT(*) FROM usuarios
UNION ALL SELECT 'proyectos',          COUNT(*) FROM proyectos
UNION ALL SELECT 'epicas',             COUNT(*) FROM epicas
UNION ALL SELECT 'sprints',            COUNT(*) FROM sprints
UNION ALL SELECT 'historias_usuario',  COUNT(*) FROM historias_usuario
UNION ALL SELECT 'tareas',             COUNT(*) FROM tareas
UNION ALL SELECT 'registro_horas',     COUNT(*) FROM registro_horas
UNION ALL SELECT 'comentarios',        COUNT(*) FROM comentarios;
