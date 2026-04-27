# OMPRELA-Boards · Prototipo

Sistema de gestión de proyectos y tareas para equipos de Desarrollo y Producto.
Prototipo desarrollado como parte del Trabajo Práctico N° 1 de la materia
**Seminario de Práctica de Informática (INF275-11807)** — Universidad Siglo 21.

**Alumno:** Chavez Alan Ezequiel · Legajo VINF018147
**Docente:** Pablo Alejandro Virgolini

---

## Alcance del prototipo

Esta primera versión es un **módulo operacional** (Kendall y Kendall, 2011)
que cubre las funcionalidades centrales de gestión sobre las que se construirá
el sistema final:

- ABM de proyectos (CU02 — Crear proyecto).
- ABM de historias de usuario (CU05 — Crear historia).
- Listado del tablero Kanban por sprint (CU09 — Consultar tablero).
- Movimiento de tickets entre estados con validación de transiciones legales y
  registro en log de auditoría (CU10 — Mover ticket de estado).

No incluye autenticación, vista web, reportes ni notificaciones por email; estos
módulos se desarrollarán en las siguientes iteraciones.

## Tecnologías

| Capa             | Tecnología                                |
|------------------|--------------------------------------------|
| Lenguaje         | Java 17                                    |
| Persistencia     | MySQL 8.0                                  |
| Acceso a datos   | JDBC (mysql-connector-j 8.x)               |
| Patrón           | MVC (Modelo · Vista/Controlador · Servicio · DAO) |
| IDE recomendado  | IntelliJ IDEA / Eclipse / NetBeans         |

## Estructura del proyecto

```
prototipo/
├── sql/
│   └── 01_create_omprela_boards.sql       # Script DDL legado (referencia)
├── src/main/resources/
│   ├── db.properties                      # Credenciales y URL de MySQL
│   └── migrations/
│       ├── migrations.list                # Orden de aplicación
│       ├── V001__schema.sql               # Tablas
│       ├── V002__indices.sql              # Índices secundarios
│       ├── V003__vistas.sql               # Vistas analíticas
│       └── V004__datos_prueba.sql         # Carga inicial
├── src/main/java/com/omprela/boards/
│   ├── model/         # Entidades de dominio
│   │   ├── Proyecto.java
│   │   └── HistoriaUsuario.java
│   ├── dao/           # Acceso a datos vía JDBC
│   │   ├── ProyectoDAO.java
│   │   └── HistoriaUsuarioDAO.java
│   ├── service/       # Reglas de negocio
│   │   ├── ProyectoService.java
│   │   └── HistoriaUsuarioService.java
│   ├── util/
│   │   ├── DBConnection.java              # Gestor de conexión Singleton
│   │   └── MigrationRunner.java           # Aplica migraciones al iniciar
│   └── view/
│       └── MainConsola.java               # Vista de consola del prototipo
└── README.md
```

## Cómo ejecutarlo

> **Bootstrap automático.** No hace falta correr el script SQL a mano:
> al iniciar `MainConsola`, la aplicación se conecta a MySQL, crea la base
> `omprela_boards` si no existe y aplica las migraciones pendientes. Cada
> migración se registra en la tabla `schema_migrations` y se ejecuta una
> sola vez.

1. **Levantar MySQL 8** y asegurarse de que el usuario configurado tenga
   permiso para crear bases de datos.

2. **Configurar credenciales**
   Editar [`src/main/resources/db.properties`](src/main/resources/db.properties):
   ```properties
   db.host=localhost
   db.port=3306
   db.name=omprela_boards
   db.user=root
   db.password=tu_password
   ```
   Ya no es necesario tocar `DBConnection.java`.

3. **Compilar y ejecutar**
   Agregar `mysql-connector-j-8.x.x.jar` al classpath, incluir
   `src/main/resources/` como carpeta de recursos (los IDEs lo hacen por
   defecto) y ejecutar la clase `com.omprela.boards.view.MainConsola`.

   En el primer arranque verás algo como:
   ```
   [migrations] aplicando V001__schema.sql
   [migrations] aplicando V002__indices.sql
   [migrations] aplicando V003__vistas.sql
   [migrations] aplicando V004__datos_prueba.sql
   [migrations] 4 migracion(es) aplicadas correctamente
   [OK] Conexion a MySQL establecida
   ```
   En arranques posteriores:
   ```
   [migrations] esquema al dia (4 migraciones registradas)
   ```

### Agregar nuevas migraciones

1. Crear `src/main/resources/migrations/V005__descripcion.sql`.
2. Agregar el nombre del archivo al final de `migrations.list`.
3. En el próximo arranque la migración se aplica automáticamente.

Convenciones, reglas y troubleshooting completos en
[`docs/migraciones.md`](docs/migraciones.md).

### Script SQL legado

[`sql/01_create_omprela_boards.sql`](sql/01_create_omprela_boards.sql) se
conserva como referencia (incluye el `DROP DATABASE` original). **No** se
necesita ejecutarlo manualmente. Si previamente lo corriste a mano, dropeá
la base antes del primer arranque para que el sistema de migraciones
quede limpio:
```sql
DROP DATABASE omprela_boards;
```

## Modelo de datos

El esquema implementa las nueve entidades del diagrama de dominio del documento
del TP1: `clientes`, `usuarios`, `proyectos`, `epicas`, `sprints`,
`historias_usuario`, `tareas`, `registro_horas` y `comentarios`. Adicionalmente
se incluye una tabla `log_auditoria` para registrar las transiciones de estado
de los tickets, dando cumplimiento al RF12 (auditoría de cambios).

Las restricciones `CHECK` y las claves foráneas garantizan la integridad
referencial y de dominio. Se incluyen dos vistas (`v_backlog_priorizado` y
`v_velocity_proyecto`) que serán consumidas por la vista Producto del sistema.

## Próximos pasos

- Migrar la capa de presentación a Spring Boot + Thymeleaf.
- Incorporar Spring Security con JWT y `bcrypt` para CU01 (Autenticar usuario).
- Implementar el módulo de notificaciones (SMTP) para CU16.
- Construir los dashboards de métricas (velocity, burndown, lead time, throughput).

## Referencia

Kendall, K. E., y Kendall, J. E. (2011). *Análisis y diseño de sistemas* (8.ª ed.).
Pearson Education.
