# Cómo ejecutar OMPRELA-Boards — Integrador Final (TP4)

## Requisitos
- Java 17 o superior (probado con Java 21)
- MySQL corriendo en localhost (root / clave123 por defecto)
- El driver mysql-connector-j en la carpeta `lib/`

## Bootstrap automático
NO hace falta correr scripts SQL a mano. Al iniciar, la app crea la base
`omprela_boards`, las tablas y los datos de ejemplo automáticamente.

## Driver MySQL
Descargá el driver y ponelo en `lib/`:
  https://dev.mysql.com/downloads/connector/j/  (Platform Independent)
o por consola:
  Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar" -OutFile "lib\mysql-connector-j-8.4.0.jar"

## Compilar (PowerShell, dentro de prototipo_v4)
javac -cp "lib/*" -d build (Get-ChildItem -Recurse -Filter *.java -Path src\main\java\com\omprela\boards | Where-Object { $_.FullName -notmatch '\\api\\' } | ForEach-Object { $_.FullName })

## Ejecutar
java -cp "build;lib/*" com.omprela.boards.view.MainConsola

## Novedades del TP4 en el menú
- Opción 10: Reporte del backlog (usa ARREGLOS NATIVOS int[]/double[])
- Opción 11: Exportar backlog a CSV (usa java.io: FileWriter/BufferedReader)
  Genera salida/backlog.csv y salida/eventos.log

## Patrón de diseño
El proyecto aplica DAO como patrón central (TicketDAO aísla el acceso a MySQL),
complementado con Singleton (DBConnection), MVC (capas model/view/service) y
Strategy (algoritmos de ordenación sobre la interfaz Priorizable).
