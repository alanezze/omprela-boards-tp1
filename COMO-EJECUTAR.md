# Cómo ejecutar OMPRELA-Boards con MySQL (TP3)

## Requisitos
- Java 17 o superior (tenés Java 21 ✓)
- MySQL corriendo en localhost (tenés root / clave123 ✓)
- El driver mysql-connector-j en la carpeta `lib/`

## ¡Bootstrap automático!
**NO hace falta correr ningún script SQL a mano.** Al iniciar la app,
el programa se conecta a MySQL, crea la base `omprela_boards` si no existe,
crea las tablas y carga 6 tickets de ejemplo automáticamente. En arranques
posteriores detecta que ya hay datos y no los duplica.

---

## PASO 1 — Driver MySQL en lib/
Asegurate de tener el archivo `mysql-connector-j-9.x.x.jar` en la carpeta `lib/`.
(Descarga: https://dev.mysql.com/downloads/connector/j/ → Platform Independent)

## PASO 2 — Compilar
En PowerShell, parado en la carpeta `prototipo_v3`:

```powershell
javac -cp "lib/*" -d build (Get-ChildItem -Recurse -Filter *.java -Path src\main\java\com\omprela\boards | Where-Object { $_.FullName -notmatch '\\api\\' } | ForEach-Object { $_.FullName })
```

## PASO 3 — Ejecutar
```powershell
java -cp "build;lib/*" com.omprela.boards.view.MainConsola
```

Vas a ver:
```
[bootstrap] Inicializando base de datos...
[OK] Base de datos lista. 6 tickets en la base.

----------- MENU PRINCIPAL -----------
...
```

---

## Verificar que persiste de verdad
1. Opción **6** → mové el ticket 1 a EN_PROGRESO.
2. Salí con **0**.
3. Volvé a entrar: el ticket 1 sigue en EN_PROGRESO (se guardó en MySQL).
4. Desde MySQL podés mirar las tablas:
   ```
   mysql -u root -p -e "USE omprela_boards; SELECT * FROM tickets;"
   mysql -u root -p -e "USE omprela_boards; SELECT * FROM log_movimientos;"
   ```

---

## Si tu MySQL usa otro puerto/password
Editá `src/main/java/com/omprela/boards/util/DBConnection.java`
y cambiá las constantes HOST, PUERTO, USUARIO o PASSWORD.

---

## Opción más fácil: VS Code
1. Instalá la extensión "Extension Pack for Java" de Microsoft.
2. Abrí la carpeta `prototipo_v3` en VS Code.
3. Andá al panel "Java Projects" → Referenced Libraries → botón + → elegí el .jar de lib/.
4. Abrí `MainConsola.java` y hacé click en Run ▶ arriba del main.

## El script SQL manual (opcional)
Si preferís crear la base a mano en vez del bootstrap automático, podés correr:
```
mysql -u root -p < sql/03_create_tickets_tp3.sql
```
Pero NO es necesario: el bootstrap lo hace solo.
