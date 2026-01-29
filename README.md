# Library Creator - Backend

**Library Creator** es una plataforma integral de creación literaria diseñada para potenciar la imaginación del autor mediante un entorno híbrido que fusiona la escritura tradicional con la asistencia de Inteligencia Artificial avanzada.

---

## Características principales

- Sección Artificial (Poly): Chat inteligente parametrizable para generar y reinventar narrativas guiadas con IA.
- Sección Creativa: Espacio de redacción manual con herramientas de edición especializadas.
- Gestor Editorial: Catalogar obras en estanterías virtuales.
- Versionamiento: Gestión de versiones de los textos.
- Exportación: Conversión de proyectos a PDF/Word.
- Suscripciones: Modelo escalable que adapta almacenamiento y potencia de IA.

---

## Estructura del Proyecto

```
com.libraryai.backend/
|-- App.java                     # Punto de entrada principal
|-- server/                      # Servidor HTTP (Sockets puros)
|   |-- ServerMain.java          # Acepta conexiones y maneja peticiones
|-- controller/                  # Controladores (endpoints HTTP)
|-- service/                     # Lógica de negocio
|-- dao/                         # Acceso a base de datos
|   |-- UsuariosDao.java         # CRUD de usuarios
|-- models/                      # Entidades/POJOs
|   |-- Usuario.java             # Modelo de usuario
|-- ai/                          # Integración con IA (Gemini)
|   |-- GeminiAI.java            # Cliente de Gemini API
|-- config/                      # Configuración
|   |-- ConexionDB.java          # Conexión a base de datos
|-- util/                        # Utilidades compartidas
`-- db/                          # Scripts SQL
    `-- LC_v3-SQL.sql            # Esquema de base de datos
```

---

## Tecnologías

| Tecnología            | Uso                          |
| --------------------- | ---------------------------- |
| Java 25               | Lenguaje principal           |
| Servidor Https        | Servidor HTTP sin frameworks |
| MySQL                 | Base de datos                |
| Gson                  | Serialización JSON           |
| Google Gemini API     | Asistente de IA "Poly"       |
| Maven                 | Gestión de dependencias      |

---

## Configuración

### Variables de entorno requeridas

```bash
DB_URL=jdbc:mysql://localhost:3306/LibraryAI_DB
DB_USER=tu_usuario
DB_PASSWD=tu_contrasena
GOOGLE_API_KEY=tu_api_key_de_gemini
```

### Instalación

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/tu-usuario/libraryai-creator_backend.git
   ```

2. Instalar dependencias:

   ```bash
   mvn install
   ```

3. Configurar variables de entorno (ver sección anterior).

4. Ejecutar:

   ```bash
   mvn exec:java -Dexec.mainClass="com.libraryai.backend.App"
   ```

---

## Planificación

Ver [ROADMAP.md](./ROADMAP.md) para conocer las tareas pendientes y el plan de desarrollo.

---

## Autor

**Library Creator Team**

---

## Licencia

Este proyecto está bajo desarrollo privado.
