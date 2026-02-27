# Library Creator - Backend

**Library Creator** es una plataforma integral de creación literaria diseñada para potenciar la imaginación del autor mediante un entorno híbrido que fusiona la escritura tradicional con la asistencia de Inteligencia Artificial avanzada.

---

## Características principales

- 🤖 **Sección Artificial (Poly):** Chat inteligente parametrizable para generar y reinventar narrativas guiadas con IA.
- ✍️ **Sección Creativa:** Espacio de redacción manual con herramientas de edición especializadas.
- 📚 **Gestor Editorial:** Catalogar obras en estanterías virtuales.
- 📝 **Versionamiento:** Gestión de versiones de los textos.
- 📄 **Exportación:** Conversión de proyectos a PDF/Word.
- 💳 **Suscripciones:** Modelo escalable que adapta almacenamiento y potencia de IA.
- 🔐 **Autenticación Segura:** Sistema de login con verificación de correo y recuperación de contraseña.

---

## Estructura del Proyecto

```
com.libraryai.backend/
|-- App.java                          # Punto de entrada principal
|-- server/                           # Servidor HTTP (Sockets puros)
|   |-- ServerMain.java               # Acepta conexiones y maneja peticiones
|   |-- http/                         # Utilidades HTTP
|       |-- ApiRequest.java           # Manejo de peticiones
|       |-- ApiResponse.java          # Manejo de respuestas
|       `-- StaticFileHandler.java   # Servir archivos estáticos
|-- controller/                       # Controladores (endpoints HTTP)
|   |-- auth/                         # Autenticación y autorización
|   |   |-- LoginController.java      # Login de usuarios
|   |   `-- RecuperacionController.java # Recuperación de contraseña
|   |-- UserController.java           # CRUD de usuarios
|   |-- SettingsController.java       # Configuración de usuario
|   |-- UploadController.java         # Subida de archivos
|   `-- ai/                          # Controladores de IA
|       `-- AiController.java         # Integración con Gemini API
|-- service/                          # Lógica de negocio
|   |-- auth/                         # Servicios de autenticación
|   |   |-- LoginService.java         # Validación de login
|   |   `-- RecuperacionService.java  # Recuperación de contraseña
|   |-- UserService.java              # Gestión de usuarios
|   |-- EmailService.java             # Envío de correos
|   `-- ai/                          # Servicios de IA
|       `-- GeminiAI.java            # Cliente de Gemini API
|-- dao/                             # Acceso a base de datos
|   |-- auth/                         # DAOs de autenticación
|   |   |-- LoginDao.java             # Consultas de login
|   |   `-- RecuperacionDao.java     # Tokens de acceso
|   |-- UserDao.java                  # CRUD de usuarios
|   |-- UserRoleDao.java              # Asignación de roles
|   |-- chats/                        # DAOs de chat
|   |   `-- ChatDao.java              # Gestión de mensajes
|   `-- [otros DAOs...]               # Acceso a otras entidades
|-- models/                          # Entidades/POJOs
|   |-- User.java                     # Modelo de usuario
|   |-- AccessToken.java              # Modelo de tokens
|   `-- [otros modelos...]            # Otras entidades
|-- ai/                              # Integración con IA (Gemini)
|   |-- GeminiAI.java                # Cliente de Gemini API
|   `-- [otros servicios IA...]       # Componentes de IA
|-- config/                          # Configuración
|   |-- DatabaseConnection.java       # Conexión a base de datos
|   `-- [otras configuraciones...]    # Variables de entorno
|-- util/                            # Utilidades compartidas
|   |-- JwtUtil.java                  # Manejo de tokens JWT
|   `-- [otras utilidades...]         # Funciones helper
|-- middleware/                      # Middleware de servidor
|   `-- AuthMiddleware.java           # Autenticación de rutas
|-- routes/                          # Definición de rutas
|   `-- Routes.java                   # Configuración de endpoints
`-- db/                              # Scripts SQL
    `-- LC_v3-SQL.sql                 # Esquema completo de base de datos
```

---

## Tecnologías

| Tecnología        | Uso                          |
| ----------------- | ---------------------------- |
| Java 25           | Lenguaje principal           |
| Servidor HTTP     | Servidor HTTP sin frameworks |
| MySQL             | Base de datos                |
| Gson              | Serialización JSON           |
| JWT               | Autenticación y tokens       |
| BCrypt            | Hashing de contraseñas       |
| JavaMail          | Envío de correos             |
| Google Gemini API | Asistente de IA "Poly"       |
| Maven             | Gestión de dependencias      |

---

## Configuración

### Variables de entorno requeridas

Configura las siguientes variables en un archivo `.env` en la raíz del proyecto.

```ini
# Configuración de Base de Datos
DB_URL="jdbc:mysql://localhost:3306/libraryai_db"
DB_USER="root"
DB_PASSWD="tu_contraseña_segura"

# Seguridad (JWT)
# ¡IMPORTANTE! Debe tener al menos 32 caracteres (256 bits) para HS256.
JWT_KEY="clave_muy_segura_y_larga_para_firmar_tokens_jwt_min_32_chars"

# Configuración de Correo (para verificación y recuperación)
EMAIL_USER="tu_correo@gmail.com"
EMAIL_PASS="tu_contraseña_de_app_gmail"

# Inteligencia Artificial (Google Gemini)
GEMINI_API_KEY="tu_api_key_de_gemini"
GEMINI_MODEL="gemini-2.0-flash-exp"
```

---

## API Endpoints

### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/login` | Login de usuarios |
| `POST` | `/api/v1/usuarios` | Registro de nuevos usuarios |
| `POST` | `/api/v1/recuperar` | Solicitar recuperación de contraseña |
| `GET` | `/api/v1/recuperar/validar` | Validar token de recuperación |
| `PUT` | `/api/v1/recuperar/nueva` | Establecer nueva contraseña |

### Usuarios
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/usuarios` | Listar usuarios (Admin) |
| `GET` | `/api/v1/usuarios/id` | Obtener usuario por ID |
| `PUT` | `/api/v1/usuarios` | Actualizar usuario |
| `DELETE` | `/api/v1/usuarios` | Eliminar usuario |

### IA y Chat
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/ai/chat` | Enviar mensaje a Poly (IA) |
| `GET` | `/api/v1/ai/models` | Listar modelos disponibles |

### Archivos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/upload` | Subir archivos |
| `GET` | `/api/v1/files` | Listar archivos del usuario |

---

## Flujo de Autenticación

### 1. Registro de Usuario
```bash
POST /api/v1/usuarios
{
  "nombre": "Usuario Test",
  "correo": "usuario@correo.com", 
  "contraseña": "password123"
}
```
- ✅ Crea usuario con rol "Gratuito" por defecto
- ✅ Envía correo de verificación (opcional)
- ✅ Retorna mensaje de confirmación

### 2. Login con Verificación
```bash
POST /api/v1/login
{
  "correo": "usuario@correo.com",
  "contraseña": "password123"
}
```
- ✅ Valida credenciales
- ✅ Si correo no verificado, envía código de verificación
- ✅ Si correo verificado, genera token JWT

### 3. Verificación de Correo
```bash
GET /api/v1/recuperar/validar?token=UUID-GENERADO
```
- ✅ Valida token único
- ✅ Marca correo como verificado
- ✅ Permite login completo

---

## Instalación

### Prerrequisitos
- Java 25 o superior
- MySQL 8.0 o superior
- Maven 3.6 o superior

### Pasos de instalación

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/libraryai-creator_backend.git
   cd libraryai-creator_backend
   ```

2. **Configurar base de datos:**
   ```bash
   # Crear base de datos
   mysql -u root -p < src/main/resources/db/LC_v3-SQL.sql
   ```

3. **Configurar variables de entorno:**
   ```bash
   # Copiar y editar archivo .env
   cp .env.example .env
   # Editar .env con tus credenciales
   ```

4. **Instalar dependencias:**
   ```bash
   mvn clean install
   ```

5. **Ejecutar servidor:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.libraryai.backend.App"
   ```

6. **Verificar funcionamiento:**
   ```bash
   # El servidor iniciará en http://localhost:8080
   curl http://localhost:8080/api/v1/health
   ```

---

## Arquitectura

### Patrones Implementados
- **DAO Pattern:** Separación de acceso a datos
- **Service Layer:** Lógica de negocio centralizada
- **Controller Pattern:** Manejo de peticiones HTTP
- **Middleware Pattern:** Autenticación y autorización
- **JWT Authentication:** Tokens seguros para sesiones

### Seguridad
- **Contraseñas:** Hash con BCrypt
- **Tokens:** JWT con firma HS256
- **Correos:** Verificación opcional
- **Roles:** Sistema de permisos por roles

### Base de Datos
- **Motor:** MySQL 8.0
- **Índices:** Optimizados para rendimiento
- **Relaciones:** Integridad referencial completa
- **Vistas:** Consultas complejas predefinidas

---

## Desarrollo

### Estructura de Paquetes
- `controller/`: Endpoints HTTP y manejo de peticiones
- `service/`: Lógica de negocio y reglas de negocio
- `dao/`: Acceso a datos y consultas SQL
- `models/`: Entidades y POJOs
- `util/`: Utilidades compartidas y helpers
- `config/`: Configuración y variables de entorno
- `middleware/`: Filtros y middleware de servidor

### Convenciones
- **Nomenclatura:** CamelCase para clases, snake_case para DB
- **JSON:** Gson para serialización/deserialización
- **Errores:** Respuestas consistentes con status codes HTTP
- **Logs:** Salida estándar para debugging

---

## Testing

### Pruebas Manuales
```bash
# Test de registro
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test","correo":"test@example.com","contraseña":"password123"}'

# Test de login
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"test@example.com","contraseña":"password123"}'
```

### Archivos de Test
- `test_verificacion_login.html`: Test completo de flujo de autenticación
- Scripts de prueba en carpeta `/tests/`

---

## Planificación

Ver [ROADMAP.md](./ROADMAP.md) para conocer las tareas pendientes y el plan de desarrollo.

---

## Contribución

1. Fork del proyecto
2. Crear feature branch: `git checkout -b feature/nueva-funcionalidad`
3. Commit changes: `git commit -am 'Agregar nueva funcionalidad'`
4. Push branch: `git push origin feature/nueva-funcionalidad`
5. Submit Pull Request

---

## Autor

**Library Creator Team**
- Backend: Java puro con servidor HTTP propio
- Frontend: HTML/CSS/JavaScript
- IA: Google Gemini API
- Base de datos: MySQL

---

## Licencia

Este proyecto está bajo desarrollo privado.
