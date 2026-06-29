# OMPRELA-Boards · Prototipo (TP1 + TP2 + TP3 + TP4)

Sistema de gestión de proyectos y tareas para equipos de Desarrollo y Producto.
Prototipo desarrollado como parte de los Trabajos Prácticos N° 1, 2, 3 y 4 (Integrador Final)
de la materia **Seminario de Práctica de Informática (INF275-11807)** — Universidad Siglo 21.

**Alumno:** Chavez Alan Ezequiel · Legajo VINF018147
**Docente:** Pablo Alejandro Virgolini

---

## Novedades del TP3 (POO)

El TP3 refactoriza el modelo aplicando los 4 pilares de la programación orientada a objetos:

- **Abstracción:** clase abstracta `Ticket` + interfaces `Auditable`, `Priorizable`, `Notificable`.
- **Herencia:** `HistoriaUsuario` y `Tarea` extienden de `Ticket`; jerarquía de excepciones.
- **Polimorfismo:** métodos `getTipo()` y `calcularEsfuerzo()` sobrescritos; genéricos.
- **Encapsulamiento:** atributos privados con validaciones en los setters.

Incluye además:
- Manejo de excepciones propias (`OmprelaException` y subclases).
- Estructuras de datos propias: `Pila<T>` (LIFO) y `Cola<T>` (FIFO).
- Algoritmos: Quicksort, burbuja, búsqueda binaria y lineal.
- Menú de consola interactivo (`MainConsola`).
- API REST con Spring Boot (`TicketController`) + frontend HTML/JS (`frontend/index.html`).

---

## Novedades del TP4 (Integrador Final)

El TP4 cierra el integrador incorporando manejo de archivos y arreglos nativos,
y agrega dos opciones nuevas al menú de consola (10 y 11):

- **Arreglos nativos** (`reporte/ReporteBacklog.java`): genera estadísticas del
  backlog (tickets por estado, esfuerzo por prioridad, cuello de botella) usando
  `int[]` y `double[]` de tamaño fijo, de forma **complementaria** a los `ArrayList`
  que maneja el resto del sistema.
- **Manejo de archivos** (`archivo/ExportadorArchivos.java`): exporta el backlog a
  CSV con `java.io` (`FileWriter`/`PrintWriter`), registra eventos en un log en modo
  *append* y vuelve a leer el archivo con `BufferedReader`. Todo con *try-with-resources*
  y manejo de `IOException`. Los archivos se generan en la carpeta `salida/`.
- **Patrón DAO** como patrón central: `dao/TicketDAO.java` aísla el acceso a MySQL,
  complementado con Singleton (`DBConnection`), MVC (capas model/view/service) y
  bootstrap automático de la base (`util/BootstrapDB.java`).
- **Persistencia en tablas separadas**: las historias se guardan en `historias_usuario`
  y las tareas en `tareas` (esquema relacional del TP1). Como cada tabla tiene su propio
  id, las operaciones por ticket se identifican por **tipo + id** (en el menú se pide
  `H` o `T`). Los cambios de estado se auditan en `log_auditoria`.

### Bootstrap automático (crea toda la base desde cero)

`BootstrapDB` ejecuta el script canónico [sql/01_create_omprela_boards.sql](sql/01_create_omprela_boards.sql)
como **única fuente de verdad**: al arrancar, si la base aún no existe, crea **todas** las
tablas (clientes, usuarios, proyectos, épicas, sprints, historias_usuario, tareas,
registro_horas, comentarios, log_auditoria + vistas) y carga el **seeder** completo.
Es **idempotente**: si la base ya estaba inicializada, no toca los datos.

> Conexión configurable por system property (útil para tests): `-Domprela.db`, `-Domprela.user`,
> `-Domprela.password`, `-Domprela.host`, `-Domprela.port`, y `-Domprela.sql` para la ruta del script.

---

## Estructura del proyecto

```
prototipo_v4/
├── pom.xml                          # Configuración Maven + Spring Boot
├── frontend/
│   └── index.html                   # Frontend Kanban (consume la API REST)
├── lib/                             # Driver mysql-connector-j (JDBC)
├── sql/
│   ├── 01_create_omprela_boards.sql # Script DDL + datos de prueba (TP1)
│   └── 02_consultas_tp2.sql         # 10 consultas SQL (TP2)
└── src/main/java/com/omprela/boards/
    ├── model/
    │   ├── Ticket.java              # Clase ABSTRACTA base
    │   ├── HistoriaUsuario.java     # extends Ticket implements Notificable
    │   ├── Tarea.java               # extends Ticket
    │   ├── Proyecto.java            # implements Auditable
    │   ├── interfaces/              # Auditable, Priorizable, Notificable
    │   └── excepciones/             # OmprelaException + 3 subclases
    ├── algoritmos/
    │   ├── AlgoritmosTickets.java   # Quicksort, burbuja, búsquedas
    │   ├── Pila.java                # Pila genérica LIFO
    │   └── Cola.java                # Cola genérica FIFO
    ├── dao/
    │   └── TicketDAO.java           # Acceso a datos MySQL (patrón DAO)
    ├── util/
    │   ├── DBConnection.java        # Conexión JDBC (Singleton)
    │   └── BootstrapDB.java         # Bootstrap automático de la base
    ├── reporte/
    │   └── ReporteBacklog.java      # Estadísticas con ARREGLOS NATIVOS (TP4)
    ├── archivo/
    │   └── ExportadorArchivos.java  # Exportación CSV/log con java.io (TP4)
    ├── service/
    │   └── TicketService.java       # Servicio polimórfico
    ├── view/
    │   └── MainConsola.java         # Menú de consola interactivo
    └── api/
        └── TicketController.java    # API REST (Spring Boot)
```

## Cómo ejecutar

> Guía detallada paso a paso (driver MySQL, compilación en PowerShell, etc.) en
> [COMO-EJECUTAR.md](COMO-EJECUTAR.md).

**Requisitos:** Java 17+ (probado con 21), MySQL en `localhost` y el driver
`mysql-connector-j` en `lib/`. La app hace *bootstrap* automático de la base
(crea `omprela_boards`, tablas y datos de ejemplo al iniciar).

### Opción 1: Menú de consola (PowerShell, dentro de `prototipo_v4`)
```powershell
javac -cp "lib/*" -d build (Get-ChildItem -Recurse -Filter *.java -Path src\main\java\com\omprela\boards | Where-Object { $_.FullName -notmatch '\\api\\' } | ForEach-Object { $_.FullName })
java -cp "build;lib/*" com.omprela.boards.view.MainConsola
```
El menú incluye las opciones del TP4:
- **Opción 10** — Reporte del backlog (arreglos nativos `int[]`/`double[]`).
- **Opción 11** — Exportar backlog a CSV (`java.io`); genera `salida/backlog.csv` y `salida/eventos.log`.

### Opción 2: API REST con Spring Boot
```bash
mvn spring-boot:run
# La API queda disponible en http://localhost:8080/api/tickets
# Abrir frontend/index.html en el navegador
```
