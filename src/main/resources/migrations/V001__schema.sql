-- =====================================================================
-- V001 - Esquema base de OMPRELA-Boards
-- Crea las nueve tablas de dominio + tabla de auditoria
-- =====================================================================

CREATE TABLE IF NOT EXISTS clientes (
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

CREATE TABLE IF NOT EXISTS usuarios (
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

CREATE TABLE IF NOT EXISTS proyectos (
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

CREATE TABLE IF NOT EXISTS epicas (
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

CREATE TABLE IF NOT EXISTS sprints (
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

CREATE TABLE IF NOT EXISTS historias_usuario (
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

CREATE TABLE IF NOT EXISTS tareas (
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

CREATE TABLE IF NOT EXISTS registro_horas (
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

CREATE TABLE IF NOT EXISTS comentarios (
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

CREATE TABLE IF NOT EXISTS log_auditoria (
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
