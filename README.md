# Library Creator Backend

Backend principal de **Library Creator**, una plataforma de escritura asistida que combina creación manual, asistencia con IA, biblioteca de relatos y administración de planes y usuarios.

Este repositorio contiene el servidor HTTP, la lógica de negocio, el acceso a datos y los scripts SQL base. El frontend vive en el repositorio hermano `../Proyecto_Personal-frontend`.

## Estado actual del proyecto

El sistema ya cubre estos flujos principales:
- Registro, login, verificación de correo y recuperación de contraseña.
- Chat con Poly usando Gemini.
- Canvas creativo para escritura manual.
- Biblioteca con borradores y documentos exportados.
- Relatos asociados a varias estanterías mediante tabla relacional.
- Gestión dinámica de planes desde admin.
- Moderación de contenido y auditoría básica.
- Exportación a Word/PDF y control de cuota de almacenamiento.

## Arquitectura general

El backend sigue una estructura clásica por capas:

1. `Routes` recibe y registra endpoints.
2. `Controller` traduce la request HTTP al caso de uso.
3. `Service` aplica reglas de negocio.
4. `Dao` consulta o persiste en MySQL por JDBC.
5. `Models` representan entidades y payloads internos.

También hay una capa de utilidades para JWT, exportación de documentos, extracción de texto y parsing HTTP simple.

## Estructura relevante

```text
src/main/java/com/libraryai/backend/
├── App.java
├── ai/
├── config/
├── controller/
├── dao/
├── middleware/
├── models/
├── routes/
├── seeders/
├── server/
├── service/
└── util/

src/main/resources/db/
├── schema/
│   └── LC_v3_SQL_final.sql
├── seed/
│   ├── LC_reset_datos_demo.sql
│   ├── LC_demo_seed_completo.sql
│   └── credenciales_demo_seed.txt
└── queries/
    └── pruebas.sql
```

## Documentación interna

Para entender el proyecto más rápido, revisa primero:
- [GUIA_CODIGO_BACKEND.md](./GUIA_CODIGO_BACKEND.md)
- [LIBRERIAS_Y_DRIVERS_USADOS.txt](./LIBRERIAS_Y_DRIVERS_USADOS.txt)
- `../Proyecto_Personal-frontend/GUIA_CODIGO_FRONTEND.md`

## Tecnologías usadas

### Backend
- Java 25
- Maven
- MySQL
- JDBC directo
- Gson
- JWT (`jjwt`)
- jBCrypt
- JavaMail
- Google GenAI SDK
- Apache PDFBox
- Apache POI
- `com.sun.net.httpserver`

### Frontend relacionado
- HTML + CSS + JavaScript modular
- Vite
- Toastify JS
- SweetAlert2

## Variables de entorno

Configura un archivo `.env` en la raíz del backend con valores como estos:

```ini
DB_URL="jdbc:mysql://localhost:3306/LibraryAI_DB"
DB_USER="root"
DB_PASSWD="tu_password"

JWT_KEY="clave_larga_y_segura_de_al_menos_32_caracteres"

EMAIL_USER="tu_correo"
EMAIL_PASS="tu_clave_de_app"

GEMINI_API_KEY="tu_api_key"
GEMINI_MODEL="gemini-2.5-flash"
GEMINI_FREE_MODEL="gemini-2.5-flash"
GEMINI_PREMIUM_MODEL="gemini-2.5-pro"

FRONTEND_BASE_URL="http://localhost:5500/public"
```

## Base de datos

### Esquema actual
- `src/main/resources/db/schema/LC_v3_SQL_final.sql`

### Scripts útiles
- `src/main/resources/db/seed/LC_reset_datos_demo.sql`: limpia datos demo y reinicia IDs.
- `src/main/resources/db/seed/LC_demo_seed_completo.sql`: carga datos demo consistentes con el esquema actual.
- `src/main/resources/db/seed/credenciales_demo_seed.txt`: credenciales de usuarios demo.
- `src/main/resources/db/queries/pruebas.sql`: consultas de apoyo para revisar estado de datos.

### Flujo recomendado para entorno demo

```bash
mysql -uroot -p LibraryAI_DB < src/main/resources/db/schema/LC_v3_SQL_final.sql
mysql -uroot -p LibraryAI_DB < src/main/resources/db/seed/LC_reset_datos_demo.sql
mysql -uroot -p LibraryAI_DB < src/main/resources/db/seed/LC_demo_seed_completo.sql
```

## Cómo ejecutar el backend

### Requisitos
- Java 25
- Maven 3.6+
- MySQL 8+

### Compilar

```bash
mvn -q -DskipTests compile
```

### Ejecutar

```bash
mvn -q exec:java
```

Punto de entrada:
- `src/main/java/com/libraryai/backend/App.java`

## Frontend relacionado

El frontend se encuentra en:
- `/home/karys/Desktop/Library-Creator/Proyecto_Personal-frontend`

Comandos típicos:

```bash
cd ../Proyecto_Personal-frontend
npm install
npm run dev
```

Build de distribución:

```bash
npm run build
```

## Módulos principales

### Usuario y autenticación
- Registro, login y validación de sesión por JWT.
- Verificación de correo.
- Recuperación y cambio de contraseña.

### Poly / IA
- Chat con persistencia por relato.
- Parámetros de estilo y tono.
- Moderación de entradas.
- Manejo explícito de errores de cuota de Gemini.

### Creativo
- Borradores manuales.
- Historial local de edición.
- Exportación a biblioteca.

### Biblioteca
- Lectura de documentos exportados.
- Reapertura de borradores editables.
- Filtro por estanterías.
- Control de cuota de almacenamiento.

### Administración
- Dashboard con estadísticas.
- Gestión de usuarios.
- Gestión de planes.
- Historial de moderación.

## Notas de diseño

- El backend no usa framework web como Spring; trabaja con `HttpServer` y utilidades propias.
- El acceso a datos no usa ORM; toda la persistencia pasa por JDBC y DAO.
- Las estanterías ya no pertenecen al usuario; los relatos se relacionan a ellas por tabla puente.
- Los planes ya no están fijos a solo dos tarjetas en UI; el sistema soporta catálogo dinámico.

## Siguiente lectura recomendada

Si vas a entrar al código, este orden ayuda bastante:
1. `src/main/java/com/libraryai/backend/App.java`
2. `src/main/java/com/libraryai/backend/routes/Routes.java`
3. `src/main/java/com/libraryai/backend/controller/story/StoryController.java`
4. `src/main/java/com/libraryai/backend/service/story/StoryService.java`
5. `src/main/java/com/libraryai/backend/dao/story/StoryDao.java`
6. `src/main/java/com/libraryai/backend/service/chat/ChatService.java`
7. `src/main/java/com/libraryai/backend/dao/settings/SettingsDao.java`
8. `src/main/java/com/libraryai/backend/dao/admin/AdminDao.java`

## Verificación rápida

Comandos útiles para validar que el repo está sano:

```bash
mvn -q -DskipTests compile
```

Y del lado del frontend:

```bash
cd ../Proyecto_Personal-frontend
npm run build
```
