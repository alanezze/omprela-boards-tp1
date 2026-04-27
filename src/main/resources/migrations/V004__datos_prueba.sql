-- =====================================================================
-- V004 - Datos de prueba (carga inicial)
-- Solo se ejecuta una vez (controlado por la tabla schema_migrations)
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
