# Sistema de migraciones

Este documento describe cómo funciona el sistema de migraciones de base
de datos del prototipo OMPRELA-Boards y cómo extenderlo.

## Objetivo

Que la base `omprela_boards` y todo su esquema (tablas, índices, vistas,
datos de prueba) se creen automáticamente la primera vez que se ejecuta
la aplicación, sin requerir pasos manuales con `mysql` ni edición del
código fuente para cambiar credenciales.

## Componentes

| Archivo | Rol |
|---------|-----|
| `src/main/resources/db.properties` | Credenciales y URL de MySQL |
| `src/main/resources/migrations/migrations.list` | Lista ordenada de scripts a aplicar |
| `src/main/resources/migrations/V001__schema.sql` | DDL de las nueve tablas + log de auditoría |
| `src/main/resources/migrations/V002__indices.sql` | Índices secundarios |
| `src/main/resources/migrations/V003__vistas.sql` | Vistas analíticas (`v_backlog_priorizado`, `v_velocity_proyecto`) |
| `src/main/resources/migrations/V004__datos_prueba.sql` | Carga inicial (clientes, usuarios, proyectos, etc.) |
| `src/main/java/com/omprela/boards/util/DBConnection.java` | Singleton JDBC; crea la base y dispara las migraciones |
| `src/main/java/com/omprela/boards/util/MigrationRunner.java` | Lee, parsea y aplica los scripts SQL |

## Flujo de inicialización

```
MainConsola.main()
   └── DBConnection.getConnection()
         └── (primera vez)
               ├── carga db.properties
               ├── carga driver com.mysql.cj.jdbc.Driver
               ├── abre conexión sin DB seleccionada
               ├── CREATE DATABASE IF NOT EXISTS omprela_boards
               └── MigrationRunner.aplicar()
                     ├── CREATE TABLE IF NOT EXISTS schema_migrations
                     ├── SELECT version FROM schema_migrations
                     └── para cada archivo en migrations.list NO aplicado:
                           ├── parte el SQL en sentencias
                           ├── ejecuta cada sentencia (transacción)
                           └── INSERT INTO schema_migrations (version)
```

A partir del segundo arranque, `schema_migrations` ya contiene todas las
versiones y el `MigrationRunner` simplemente reporta `[migrations]
esquema al dia`.

## Tabla `schema_migrations`

```sql
CREATE TABLE schema_migrations (
    version      VARCHAR(100) NOT NULL PRIMARY KEY,
    aplicada_en  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

La columna `version` guarda el nombre del archivo SQL aplicado
(ej. `V001__schema.sql`). El `PRIMARY KEY` garantiza que un script no se
ejecute dos veces.

## Convenciones de nombres

```
V<numero>__<descripcion>.sql
```

- `V` mayúscula seguida de número (con padding a 3 dígitos: `V001`,
  `V010`, `V123`).
- Doble guión bajo `__` separa número de descripción.
- Descripción en `snake_case` y minúsculas, sin tildes.
- Extensión `.sql`.

Ejemplos:
- `V005__agrega_tabla_notificaciones.sql`
- `V006__crea_indice_compuesto_horas.sql`

## Cómo agregar una migración

1. Crear el archivo bajo `src/main/resources/migrations/`. Por ejemplo
   `V005__agrega_columna_avatar_usuario.sql`:
   ```sql
   ALTER TABLE usuarios
       ADD COLUMN avatar_url VARCHAR(255) NULL AFTER email;
   ```
2. Agregar el nombre del archivo al final de `migrations.list`:
   ```
   V001__schema.sql
   V002__indices.sql
   V003__vistas.sql
   V004__datos_prueba.sql
   V005__agrega_columna_avatar_usuario.sql
   ```
3. Compilar y ejecutar `MainConsola`. La nueva migración se aplica una
   sola vez y queda registrada.

## Reglas para escribir migraciones

- **Una migración nunca se modifica una vez aplicada en algún entorno.**
  Si hay un error o un cambio, hay que crear una nueva migración que lo
  corrija. Esto mantiene la historia reproducible.
- **Idempotencia defensiva**: usar `IF NOT EXISTS` / `CREATE OR REPLACE`
  cuando el motor lo permita. Aunque `schema_migrations` evita la doble
  ejecución, la idempotencia ayuda en casos donde alguien tocó la base a
  mano.
- **Datos de prueba**: si una migración inserta datos, asumir que solo
  corre en una base nueva. No agregar `INSERT IGNORE` salvo que se
  necesite explícitamente.
- **Transacción por archivo**: cada migración se aplica con `autoCommit
  = false`. Si una sentencia falla, toda la migración se revierte y el
  arranque aborta con el mensaje del error. MySQL **no** revierte DDL
  (CREATE/ALTER/DROP) dentro de una transacción, así que un fallo a
  mitad de un DDL puede dejar la base en estado inconsistente; en ese
  caso, dropear la base y arrancar de cero es la salida más limpia para
  el prototipo.
- **No usar `USE <database>`** dentro de las migraciones. La conexión
  ya está apuntando a `omprela_boards`.
- **No usar delimitadores personalizados** (`DELIMITER $$`) ni
  comentarios `/* ... */` multilínea: el parser SQL del
  `MigrationRunner` no los soporta. Si en el futuro se necesitan
  triggers o stored procedures complejos, hay que extender
  `MigrationRunner.separarSentencias`.

## Configuración de credenciales

Editar `src/main/resources/db.properties`:

```properties
db.host=localhost
db.port=3306
db.name=omprela_boards
db.user=root
db.password=root
db.timezone=America/Argentina/Buenos_Aires
```

Si se cambia `db.name`, la próxima ejecución crea **otra** base con ese
nombre y aplica todas las migraciones desde cero. Útil para tener una
base aparte en CI o para tests manuales.

## Troubleshooting

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| `No se encontro /db.properties en el classpath` | `src/main/resources/` no está marcado como carpeta de recursos en el IDE | En IntelliJ: botón derecho sobre `resources` → *Mark Directory as → Resources Root*. En Eclipse: Build Path → Source Folder. |
| `Driver JDBC de MySQL no encontrado` | Falta `mysql-connector-j-x.x.x.jar` en el classpath | Agregar el JAR como librería del proyecto |
| `Access denied for user 'root'@'localhost'` | Credenciales mal configuradas | Ajustar `db.user` y `db.password` en `db.properties` |
| `Table 'xxx' already exists` durante la primera migración | La base ya tiene tablas creadas con el script legado pero no existe `schema_migrations` | `DROP DATABASE omprela_boards;` y volver a iniciar |
| `Public Key Retrieval is not allowed` | Política de auth de MySQL 8 con la URL JDBC | Ya está cubierto por `allowPublicKeyRetrieval=true` en la URL; verificar que se esté tomando la URL nueva (recompilar) |

## Decisiones de diseño

- **¿Por qué no usar Flyway o Liquibase?** Para el prototipo de la
  cátedra, agregar una dependencia externa (y un build tool como Maven)
  excede el alcance. El `MigrationRunner` propio cubre el caso de uso
  con ~150 líneas y deja el patrón claro para la versión final, donde
  sí se va a usar Flyway sobre Spring Boot.
- **¿Por qué un archivo `migrations.list` y no escanear el directorio?**
  Escanear recursos del classpath es complejo cuando el código corre
  desde un JAR (los `Files.list()` no funcionan sobre `jar://`). Un
  archivo de listado explícito funciona igual desde el IDE y empacado.
- **¿Por qué se separó el SQL original en cuatro archivos?** Para que
  cada migración tenga una responsabilidad clara (esquema, índices,
  vistas, datos) y se pueda agregar una nueva sin tocar las anteriores.
  El script `sql/01_create_omprela_boards.sql` se conserva como
  referencia histórica del DDL completo.
