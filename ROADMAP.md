# 🗺️ ROADMAP - Library Creator Backend

> **Estado Actual:** Fase inicial de desarrollo  
> **Última Actualización:** 17 Enero 2026

---

## ✅ Componentes Implementados

### 🔧 Infraestructura Base

- [x] Servidor HTTP con sockets TCP (sin frameworks)
- [x] Sistema de Router personalizado
- [x] Clases `ApiRequest` y `ApiResponse` para manejo HTTP
- [x] Conexión a MySQL (`ConexionDB`)
- [x] Variables de entorno con dotenv

### 👤 Módulo de Usuarios

- [x] Modelo `Usuario` (POJO)
- [x] `UsuariosDao` - CRUD completo (guardar, buscarPorId, listarTodos, actualizar, eliminar)
- [x] `UserController` - Endpoints REST
- [x] `UserService` - Lógica de negocio

### 🔐 Autenticación

- [x] `LoginController` + `LoginService` + `LoginDao`
- [x] Generación de tokens JWT (`JwtUtil.tokenUsuario`)
- [x] Validación de tokens (`JwtUtil.validarToken`)
- [x] Hash de contraseñas con BCrypt

### 🤖 Integración con IA (Poly)

- [x] `GeminiAI` - Cliente para API de Gemini
- [x] `GeminiService` - Servicio de generación de texto
- [x] `AiController` - Endpoint `/api/v1/generar-historias`
- [x] Configuración de IA (`AIConfig`)

---

## ❌ Componentes Pendientes por Implementar

### 📚 Fase 1: Núcleo de Contenido (Alta Prioridad)

#### 1.1 Módulo de Relatos

| Componente | Archivo                 | Descripción                                                                 |
| ---------- | ----------------------- | --------------------------------------------------------------------------- |
| Modelo     | `Relato.java`           | Entidad con título, descripción, modoOrigen, FK a usuario/estantería/modelo |
| DAO        | `RelatoDao.java`        | CRUD + búsqueda por usuario + filtros                                       |
| Service    | `RelatoService.java`    | Lógica de creación y validaciones                                           |
| Controller | `RelatoController.java` | Endpoints CRUD `/api/v1/relatos`                                            |

#### 1.2 Módulo de Versiones de Relatos

| Componente | Archivo                        | Descripción                                   |
| ---------- | ------------------------------ | --------------------------------------------- |
| Modelo     | `RelatoVersion.java`           | Contenido, número versión, notas, esPublicada |
| DAO        | `RelatoVersionDao.java`        | Manejo de versiones por relato                |
| Service    | `RelatoVersionService.java`    | Control de versionamiento                     |
| Controller | `RelatoVersionController.java` | `/api/v1/relatos/{id}/versiones`              |

#### 1.3 Módulo de Estanterías (Gestor Editorial)

| Componente | Archivo                     | Descripción                       |
| ---------- | --------------------------- | --------------------------------- |
| Modelo     | `Estanteria.java`           | Categorías para organizar relatos |
| DAO        | `EstanteriaDao.java`        | CRUD de estanterías por usuario   |
| Service    | `EstanteriaService.java`    | Validaciones y lógica             |
| Controller | `EstanteriaController.java` | `/api/v1/estanterias`             |

---

### 💬 Fase 2: Chat con IA Mejorado (Media Prioridad)

#### 2.1 Historial de Mensajes

| Componente | Archivo               | Descripción                           |
| ---------- | --------------------- | ------------------------------------- |
| Modelo     | `MensajeChat.java`    | Emisor, contenido, orden, FK a relato |
| DAO        | `MensajeChatDao.java` | Persistir y recuperar conversaciones  |
| Service    | `ChatService.java`    | Manejo de sesiones de chat            |
| Controller | `ChatController.java` | `/api/v1/relatos/{id}/chat`           |

#### 2.2 Configuración de IA por Relato

| Componente | Archivo                   | Descripción                         |
| ---------- | ------------------------- | ----------------------------------- |
| Modelo     | `ConfiguracionIA.java`    | Estilo, creatividad, longitud, tono |
| DAO        | `ConfiguracionIADao.java` | Guardar preferencias por relato     |
| Service    | `ConfigIAService.java`    | Aplicar config a llamadas de Gemini |

---

### 💳 Fase 3: Suscripciones y Pagos

#### 3.1 Planes de Suscripción

| Componente | Archivo                   | Descripción                        |
| ---------- | ------------------------- | ---------------------------------- |
| Modelo     | `PlanSuscripcion.java`    | Nombre, almacenamiento max, precio |
| DAO        | `PlanSuscripcionDao.java` | Listar planes disponibles          |
| Controller | `PlanController.java`     | `/api/v1/planes` (solo lectura)    |

#### 3.2 Suscripciones de Usuario

| Componente | Archivo                      | Descripción                     |
| ---------- | ---------------------------- | ------------------------------- |
| Modelo     | `Suscripcion.java`           | FK usuario/plan, fechas, estado |
| DAO        | `SuscripcionDao.java`        | Gestionar suscripciones         |
| Service    | `SuscripcionService.java`    | Verificar límites, renovaciones |
| Controller | `SuscripcionController.java` | `/api/v1/suscripciones`         |

#### 3.3 Pagos

| Componente | Archivo               | Descripción                              |
| ---------- | --------------------- | ---------------------------------------- |
| Modelo     | `Pago.java`           | Monto, estado, referencia externa        |
| DAO        | `PagoDao.java`        | Registro de pagos                        |
| Service    | `PagoService.java`    | Integración con pasarela (Stripe/PayPal) |
| Controller | `PagoController.java` | `/api/v1/pagos`                          |

---

### 📁 Fase 4: Gestión de Archivos

#### 4.1 Archivos Subidos

| Componente | Archivo                  | Descripción                            |
| ---------- | ------------------------ | -------------------------------------- |
| Modelo     | `ArchivoSubido.java`     | Nombre, tipo, ruta, tamaño             |
| DAO        | `ArchivoDao.java`        | CRUD de metadatos                      |
| Service    | `ArchivoService.java`    | Subida/descarga real, límites por plan |
| Controller | `ArchivoController.java` | `/api/v1/archivos`                     |

#### 4.2 Exportación (PDF/Word)

| Componente | Archivo                 | Descripción                     |
| ---------- | ----------------------- | ------------------------------- |
| Service    | `ExportService.java`    | Convertir relatos a PDF/DOCX    |
| Controller | `ExportController.java` | `/api/v1/relatos/{id}/exportar` |

---

### 🔒 Fase 5: Seguridad y Roles

#### 5.1 Sistema de Roles y Permisos

| Componente | Archivo                          | Descripción                |
| ---------- | -------------------------------- | -------------------------- |
| Modelo     | `Rol.java`, `Permiso.java`       | Entidades de autorización  |
| DAO        | `RolDao.java`, `PermisoDao.java` | Gestión de roles           |
| Service    | `AutorizacionService.java`       | Verificar permisos por rol |

#### 5.2 Middleware de Autenticación

| Componente | Archivo               | Descripción                                      |
| ---------- | --------------------- | ------------------------------------------------ |
| Middleware | `AuthMiddleware.java` | Interceptor para validar JWT en rutas protegidas |

#### 5.3 Verificación de Correo

| Componente | Archivo               | Descripción                         |
| ---------- | --------------------- | ----------------------------------- |
| Service    | `EmailService.java`   | Envío de correos de verificación    |
| Modelo     | `TokenAcceso.java`    | Tokens de verificación/recuperación |
| DAO        | `TokenAccesoDao.java` | Gestión de tokens                   |

---

### 🤖 Fase 6: IA Avanzada

#### 6.1 Modelos de IA

| Componente | Archivo                | Descripción                              |
| ---------- | ---------------------- | ---------------------------------------- |
| Modelo     | `ModeloIA.java`        | Diferentes versiones/modelos disponibles |
| DAO        | `ModeloIADao.java`     | Listar modelos activos                   |
| Service    | `ModeloIAService.java` | Selección según plan del usuario         |

---

## 🛠️ Mejoras Técnicas Pendientes

### Servidor HTTP

- [ ] Manejo de CORS
- [ ] Rate limiting
- [ ] Compresión GZIP
- [ ] Logs estructurados

### Seguridad

- [ ] Refresh tokens
- [ ] Expiración y renovación automática de JWT
- [ ] Protección contra SQL Injection (usar PreparedStatement en todos los DAO)
- [ ] Validación de inputs en todos los endpoints

### Base de Datos

- [ ] Pool de conexiones
- [ ] Migrations/versioning del esquema

### Testing

- [ ] Tests unitarios para Services
- [ ] Tests de integración para DAO
- [ ] Tests E2E para Controllers

---

## 📊 Priorización Sugerida

```
Semana 1-2: Fase 1 (Relatos, Versiones, Estanterías)
    ↓
Semana 3-4: Fase 2 (Chat mejorado con historial)
    ↓
Semana 5-6: Fase 5.2 (Middleware Auth) + Mejoras técnicas
    ↓
Semana 7-8: Fase 4 (Archivos y Exportación)
    ↓
Semana 9-10: Fase 3 (Suscripciones y Pagos)
    ↓
Semana 11-12: Fase 5 completa + Fase 6
```

---

## 📝 Notas

- El esquema SQL ya tiene todas las tablas definidas, solo falta implementar los Model/DAO/Service/Controller correspondientes
- Seguir el patrón existente: Controller → Service → DAO
- Mantener las respuestas JSON consistentes con `ApiResponse`
