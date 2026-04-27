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
│   └── 01_create_omprela_boards.sql   # Script DDL + datos de prueba
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
│   │   └── DBConnection.java          # Gestor de conexión Singleton
│   └── view/
│       └── MainConsola.java           # Vista de consola del prototipo
└── README.md
```

## Cómo ejecutarlo

1. **Crear la base de datos**
   Levantar MySQL 8 y ejecutar el script:
   ```bash
   mysql -u root -p < sql/01_create_omprela_boards.sql
   ```
   Esto crea la base `omprela_boards`, todas las tablas, restricciones,
   índices, vistas y carga datos de prueba.

2. **Configurar credenciales**
   Editar `DBConnection.java` y ajustar usuario/contraseña a los locales:
   ```java
   private static final String USUARIO = "root";
   private static final String PASSWORD = "tu_password";
   ```

3. **Compilar y ejecutar**
   Agregar `mysql-connector-j-8.x.x.jar` al classpath y ejecutar la clase
   `com.omprela.boards.view.MainConsola`.

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
