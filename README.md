# OMPRELA-Boards · Prototipo (TP1 + TP2 + TP3)

Sistema de gestión de proyectos y tareas para equipos de Desarrollo y Producto.
Prototipo desarrollado como parte de los Trabajos Prácticos N° 1, 2 y 3 de la materia
**Seminario de Práctica de Informática (INF275-11807)** — Universidad Siglo 21.

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
- Menú de consola interactivo (`MainConsola`) con 11 opciones.
- API REST con Spring Boot (`TicketController`) + frontend HTML/JS (`frontend/index.html`).

---

## Estructura del proyecto

```
prototipo_v3/
├── pom.xml                          # Configuración Maven + Spring Boot
├── frontend/
│   └── index.html                   # Frontend Kanban (consume la API REST)
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
    ├── service/
    │   └── TicketService.java       # Servicio polimórfico
    ├── view/
    │   └── MainConsola.java         # Menú de consola interactivo
    └── api/
        └── TicketController.java    # API REST (Spring Boot)
```

## Cómo ejecutar

### Opción 1: Menú de consola (sin dependencias externas)
```bash
cd src/main/java
javac -d ../../../build com/omprela/boards/**/*.java com/omprela/boards/*.java
java -cp ../../../build com.omprela.boards.view.MainConsola
```

### Opción 2: API REST con Spring Boot
```bash
mvn spring-boot:run
# La API queda disponible en http://localhost:8080/api/tickets
# Abrir frontend/index.html en el navegador
```

## Base de datos
```bash
mysql -u root -p < sql/01_create_omprela_boards.sql
```
