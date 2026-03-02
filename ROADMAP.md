# 🗺️ ROADMAP - Library Creator (Proyecto Completo)

> **Estado Actual:** Desarrollo activo (frontend + backend)  
> **Última Actualización:** 1 Marzo 2026  
> **Versión:** v3.0 - Roadmap unificado del proyecto

---

## 📌 Alcance del Roadmap

Este documento ahora funciona como **roadmap único del proyecto completo**.

Incluye:
- Backend (API, seguridad, datos, IA y servicios)
- Frontend (auth, feed, settings, biblioteca y experiencia de usuario)

Su objetivo es dejar claro:
- qué ya existe y está funcional,
- qué está en estado MVP o prototipo,
- y qué sigue pendiente por implementar.

---

## 📊 Resumen de Progreso

| Módulo | Estado | Progreso | Última Actualización |
|---------|--------|----------|---------------------|
| 🔧 Infraestructura | ✅ Completado | 100% | Ene 2026 |
| 👤 Usuarios | ✅ Completado | 100% | Ene 2026 |
| 🔐 Autenticación | ✅ Completado | 100% | Feb 2026 |
| 📧 Correo/Verificación | ✅ Completado | 100% | Feb 2026 |
| 🤖 IA (Gemini) | ✅ Completado | 100% | Ene 2026 |
| 📚 Relatos | 🔄 MVP Funcional | 85% | Mar 2026 |
| 💬 Chat | 🔄 MVP Funcional | 80% | Mar 2026 |
| 🖥️ Frontend Base (Auth/UI) | 🔄 MVP Funcional | 85% | Mar 2026 |
| ⚙️ Frontend App (Feed/Settings/Biblioteca) | 🔄 MVP Funcional | 80% | Mar 2026 |
| ✍️ Frontend Creativo/Admin | 🔄 En Prototipo | 35% | Mar 2026 |
| 📁 Archivos | ⏳ Planeado | 0% | - |
| 💳 Suscripciones | ⏳ Planeado | 0% | - |
| 🔒 Roles Avanzados | ✅ Completado | 100% | Feb 2026 |

---

## 🎯 Módulos y Tareas Detalladas

### 🔧 MÓDULO 1: INFRAESTRUCTURA ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 01 Ene 2026  
**Fecha de Finalización:** 15 Ene 2026

#### ✅ Tareas Completadas:
- [x] **SER-001:** Servidor HTTP con sockets TCP (sin frameworks)
- [x] **SER-002:** Sistema de Router personalizado
- [x] **SER-003:** Clases ApiRequest y ApiResponse para manejo HTTP
- [x] **SER-004:** Conexión a MySQL (DatabaseConnection)
- [x] **SER-005:** Variables de entorno con dotenv
- [x] **SER-006:** Middleware básico de logging
- [x] **SER-007:** Manejo de errores centralizado
- [x] **SER-008:** Soporte para rutas dinámicas en el router

#### 📁 Archivos Clave:
- `server/ServerMain.java`
- `server/http/ApiRequest.java`
- `server/http/ApiResponse.java`
- `config/DatabaseConnection.java`

---

### 👤 MÓDULO 2: USUARIOS ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 10 Ene 2026  
**Fecha de Finalización:** 20 Ene 2026

#### ✅ Tareas Completadas:
- [x] **USR-001:** Modelo Usuario (POJO completo)
- [x] **USR-002:** UsuarioDao - CRUD completo
- [x] **USR-003:** UserController - Endpoints REST
- [x] **USR-004:** UserService - Lógica de negocio
- [x] **USR-005:** Validación de datos de entrada
- [x] **USR-006:** Manejo de errores específicos
- [x] **USR-007:** Asignación automática de roles

#### 📁 Archivos Clave:
- `models/User.java`
- `dao/UserDao.java`
- `controller/UserController.java`
- `service/UserService.java`

#### 🌐 Endpoints Implementados:
- `GET /api/v1/usuarios` - Listar usuarios
- `GET /api/v1/usuarios/id?id={id}` - Obtener usuario
- `POST /api/v1/usuarios` - Crear usuario
- `PUT /api/v1/usuarios?id={id}` - Actualizar usuario
- `PUT /api/v1/usuarios/campo?id={id}` - Actualizar un campo específico
- `DELETE /api/v1/usuarios/id?id={id}` - Eliminar usuario

---

### 🔐 MÓDULO 3: AUTENTICACIÓN Y SEGURIDAD ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 15 Ene 2026  
**Fecha de Finalización:** 27 Feb 2026

#### ✅ Tareas Completadas:
- [x] **AUTH-001:** LoginController + LoginService + LoginDao
- [x] **AUTH-002:** Generación de tokens JWT (JwtUtil)
- [x] **AUTH-003:** Validación de tokens JWT
- [x] **AUTH-004:** Hash de contraseñas con BCrypt
- [x] **AUTH-005:** Integración de roles en JWT
- [x] **AUTH-006:** Middleware de autenticación (AuthMiddleware)
- [x] **AUTH-007:** Verificación de correo al registro/login
- [x] **AUTH-008:** Sistema de recuperación de contraseña
- [x] **AUTH-009:** Tokens de acceso con expiración
- [x] **AUTH-010:** Manejo de sesiones seguras
- [x] **AUTH-011:** Validaciones reforzadas de credenciales y contraseñas

#### 📁 Archivos Clave:
- `controller/auth/LoginController.java`
- `service/auth/LoginService.java`
- `dao/auth/LoginDao.java`
- `util/JwtUtil.java`
- `middleware/AuthMiddleware.java`
- `service/EmailService.java`
- `dao/auth/RecuperacionDao.java`
- `service/auth/RecuperacionService.java`
- `controller/auth/RecuperacionController.java`

#### 🌐 Endpoints Implementados:
- `POST /api/v1/login` - Login de usuarios
- `POST /api/v1/recuperar` - Solicitar recuperación
- `GET /api/v1/recuperar/validar` - Validar token
- `PUT /api/v1/recuperar/nueva` - Nueva contraseña

---

### 🔒 MÓDULO 4: ROLES Y PERMISOS ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 20 Ene 2026  
**Fecha de Finalización:** 25 Feb 2026

#### ✅ Tareas Completadas:
- [x] **ROL-001:** Modelo Rol y Permiso
- [x] **ROL-002:** RolDao y PermisoDao
- [x] **ROL-003:** Sistema de asignación de roles
- [x] **ROL-004:** Verificación de permisos por rol
- [x] **ROL-005:** Integración con JWT
- [x] **ROL-006:** Roles por defecto (Admin, Usuario, Premium)

#### 📁 Archivos Clave:
- `models/Rol.java`
- `models/Permiso.java`
- `dao/RolDao.java`
- `dao/PermisoDao.java`
- `dao/UserRoleDao.java`

---

### 📧 MÓDULO 5: CORREO Y VERIFICACIÓN ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 01 Feb 2026  
**Fecha de Finalización:** 27 Feb 2026

#### ✅ Tareas Completadas:
- [x] **EMAIL-001:** Configuración SMTP con Gmail
- [x] **EMAIL-002:** EmailService con plantillas HTML
- [x] **EMAIL-003:** Verificación de correo al registro
- [x] **EMAIL-004:** Verificación de correo al login
- [x] **EMAIL-005:** Recuperación de contraseña
- [x] **EMAIL-006:** Tokens únicos con expiración
- [x] **EMAIL-007:** Manejo de errores de envío
- [x] **EMAIL-008:** Logging de correos enviados

#### 📁 Archivos Clave:
- `service/EmailService.java`
- `dao/auth/RecuperacionDao.java`
- `models/AccessToken.java`

---

### 🤖 MÓDULO 6: INTELIGENCIA ARTIFICIAL ✅

**Estado:** `COMPLETADO`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 10 Ene 2026  
**Fecha de Finalización:** 20 Ene 2026

#### ✅ Tareas Completadas:
- [x] **AI-001:** Cliente para Google Gemini API
- [x] **AI-002:** GeminiService para generación de texto
- [x] **AI-003:** AiController con endpoints
- [x] **AI-004:** Configuración de parámetros de IA
- [x] **AI-005:** Manejo de errores y rate limiting
- [x] **AI-006:** Integración con sistema de usuarios

#### 📁 Archivos Clave:
- `ai/GeminiAI.java`
- `service/ai/GeminiService.java`
- `controller/ai/AiController.java`

#### 🌐 Endpoints Implementados:
- `POST /api/v1/generar-historias` - Generación de historias con Gemini

---

### 📚 MÓDULO 7: RELATOS Y CONTENIDO 🔄

**Estado:** `MVP FUNCIONAL`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 20 Feb 2026  
**Fecha Estimada de Finalización:** 20 Mar 2026

#### ✅ Tareas Completadas:
- [x] **REL-001:** Modelo Relato
- [x] **REL-002:** RelatoDao con CRUD operativo
- [x] **REL-003:** Estructura de base de datos
- [x] **REL-004:** RelatoService - Lógica de negocio
- [x] **REL-005:** RelatoController - Endpoints REST
- [x] **REL-006:** Seguridad por usuario vía JWT en operaciones críticas

#### ⏳ Tareas Pendientes:
- [ ] **REL-007:** Versionamiento de relatos
- [ ] **REL-008:** Búsqueda y filtrado
- [ ] **REL-009:** Categorización
- [ ] **REL-010:** Refinar contratos para frontend y documentación final

#### 📁 Archivos Clave:
- `models/Relato.java`
- `dao/RelatoDao.java`
- `service/StoryService.java`
- `controller/StoryController.java`
- `models/RelatoVersion.java` (pendiente)

---

### 💬 MÓDULO 8: CHAT Y MENSAJES 🔄

**Estado:** `MVP FUNCIONAL`  
**Responsable:** Backend Team  
**Fecha de Inicio:** 25 Feb 2026  
**Fecha Estimada de Finalización:** 25 Mar 2026

#### ✅ Tareas Completadas:
- [x] **CHAT-001:** Estructura de base de datos
- [x] **CHAT-002:** Modelo MensajeChat
- [x] **CHAT-003:** ChatDao operativo
- [x] **CHAT-004:** ChatService - Gestión de conversaciones
- [x] **CHAT-005:** ChatController - Endpoints REST
- [x] **CHAT-006:** Persistencia básica de conversaciones
- [x] **CHAT-007:** Seguridad por usuario vía JWT en operaciones críticas

#### ⏳ Tareas Pendientes:
- [ ] **CHAT-008:** Historial de chat más completo
- [ ] **CHAT-009:** Contexto de conversación extendido
- [ ] **CHAT-010:** Integración más profunda con Gemini
- [ ] **CHAT-011:** Refinar contratos para frontend y documentación final

#### 📁 Archivos Clave:
- `models/MensajeChat.java`
- `dao/chats/ChatDao.java`
- `service/chats/ChatService.java`
- `controller/chats/ChatController.java`

---

### 📁 MÓDULO 9: ARCHIVOS Y ALMACENAMIENTO ⏳

**Estado:** `PLANEADO`  
**Prioridad:** Media  
**Fecha Estimada de Inicio:** 25 Mar 2026  
**Fecha Estimada de Finalización:** 15 Abr 2026

#### ⏳ Tareas Pendientes:
- [ ] **FILE-001:** Modelo ArchivoSubido
- [ ] **FILE-002:** ArchivoDao - CRUD de metadatos
- [ ] **FILE-003:** FileService - Subida/descarga
- [ ] **FILE-004:** FileController - Endpoints
- [ ] **FILE-005:** Límites por plan de suscripción
- [ ] **FILE-006:** Validación de tipos de archivo
- [ ] **FILE-007:** Almacenamiento físico
- [ ] **FILE-008:** Exportación PDF/DOCX

#### 📁 Archivos por Crear:
- `models/ArchivoSubido.java`
- `dao/ArchivoDao.java`
- `service/ArchivoService.java`
- `controller/ArchivoController.java`

---

### 💳 MÓDULO 10: SUSCRIPCIONES Y PAGOS ⏳

**Estado:** `PLANEADO`  
**Prioridad:** Baja  
**Fecha Estimada de Inicio:** 20 Abr 2026  
**Fecha Estimada de Finalización:** 30 May 2026

#### ⏳ Tareas Pendientes:
- [ ] **SUB-001:** Modelo PlanSuscripcion
- [ ] **SUB-002:** Modelo Suscripcion
- [ ] **SUB-003:** Modelo Pago
- [ ] **SUB-004:** DAOs para suscripciones
- [ ] **SUB-005:** Integración con pasarela de pago
- [ ] **SUB-006:** Verificación de límites por plan
- [ ] **SUB-007:** Renovaciones automáticas

---

### 🖥️ MÓDULO 11: FRONTEND BASE (AUTH/UI) 🔄

**Estado:** `MVP FUNCIONAL`  
**Responsable:** Frontend Team  
**Fecha de Inicio:** 10 Feb 2026  
**Fecha Estimada de Finalización:** 10 Mar 2026

#### ✅ Tareas Completadas:
- [x] **FE-001:** Pantallas base de inicio, login y registro
- [x] **FE-002:** Integración de login con JWT real
- [x] **FE-003:** Flujo de recuperación de contraseña
- [x] **FE-004:** Verificación de correo desde la UI
- [x] **FE-005:** Guard de acceso para pantallas protegidas
- [x] **FE-006:** Manejo defensivo de sesión y token inválido
- [x] **FE-007:** Base URL unificada para la API
- [x] **FE-008:** Sistema base de idiomas en la interfaz

#### ⏳ Tareas Pendientes:
- [ ] **FE-009:** Traducir todos los mensajes dinámicos y notificaciones
- [ ] **FE-010:** Cerrar validaciones visuales finas y estados de error

#### 📁 Áreas Cubiertas:
- `public/index.html`
- `public/auth/*`
- `src/scripts/utils/*`
- `src/scripts/pages/login.js`
- `src/scripts/pages/regist.js`
- `src/scripts/pages/recuperar.js`

---

### ⚙️ MÓDULO 12: FRONTEND APP (FEED / SETTINGS / BIBLIOTECA) 🔄

**Estado:** `MVP FUNCIONAL`  
**Responsable:** Frontend Team  
**Fecha de Inicio:** 15 Feb 2026  
**Fecha Estimada de Finalización:** 20 Mar 2026

#### ✅ Tareas Completadas:
- [x] **APP-001:** Feed principal navegable
- [x] **APP-002:** Perfil de usuario conectado al backend
- [x] **APP-003:** Cambio de contraseña y eliminación de cuenta
- [x] **APP-004:** Instrucciones persistentes para Poly
- [x] **APP-005:** Visualización del plan actual
- [x] **APP-006:** Subida de foto de perfil
- [x] **APP-007:** Estanterías con crear, listar, renombrar y eliminar
- [x] **APP-008:** Selector de idioma en settings generales

#### ⏳ Tareas Pendientes:
- [ ] **APP-009:** Pulir UX de biblioteca y navegación entre estanterías
- [ ] **APP-010:** Unificar traducciones de texto dinámico restante
- [ ] **APP-011:** Validación manual completa de flujos en navegador

#### 📁 Áreas Cubiertas:
- `public/feed/feed-main.html`
- `public/feed/settings/*`
- `public/feed/biblioteca/*`
- `src/scripts/pages/feed.js`
- `src/scripts/pages/settings/*`
- `src/scripts/pages/biblioteca/*`

---

### ✍️ MÓDULO 13: FRONTEND CREATIVO / POLY / ADMIN 🔄

**Estado:** `EN PROTOTIPO`  
**Responsable:** Frontend Team  
**Fecha de Inicio:** 20 Feb 2026  
**Fecha Estimada de Finalización:** 30 Abr 2026

#### ✅ Tareas Completadas:
- [x] **UX-001:** Maquetas visuales de Poly y creador creativo
- [x] **UX-002:** Maquetas visuales del panel admin
- [x] **UX-003:** Navegación básica hacia módulos futuros

#### ⏳ Tareas Pendientes:
- [ ] **UX-004:** Integración real del chat de Poly con backend
- [ ] **UX-005:** Integración real del creador manual
- [ ] **UX-006:** Historial y gestión visual de relatos
- [ ] **UX-007:** Login admin real con control por rol
- [ ] **UX-008:** Dashboard admin con datos reales

#### 📁 Áreas Cubiertas:
- `public/feed/poly/*`
- `public/feed/creative/*`
- `public/admin/*`

---

## 🚀 PRÓXIMAS TAREAS PRIORITARIAS

### 📅 Semana del 1-7 Marzo 2026:
1. **REL-007:** Iniciar versionamiento de relatos
2. **REL-008:** Definir búsqueda y filtrado
3. **CHAT-008:** Completar historial de conversación
4. **APP-009:** Pulir UX de biblioteca y navegación entre estanterías

### 📅 Semana del 8-14 Marzo 2026:
1. **REL-009:** Categorización y metadatos
2. **CHAT-009:** Contexto de conversación
3. **CHAT-010:** Mejorar integración con Gemini
4. **FE-009:** Traducir mensajes dinámicos y notificaciones

### 📅 Semana del 15-21 Marzo 2026:
1. **REL-010:** Cerrar contratos para frontend
2. **CHAT-011:** Cerrar contratos para frontend
3. **UX-004:** Integrar Poly con backend real
4. **FILE-001:** Iniciar módulo de archivos

---

## 🎯 HITOS PRINCIPALES

| Hito | Fecha Estimada | Estado | Descripción |
|------|----------------|---------|-------------|
| 🎯 MVP Básico | 1 Mar 2026 | ✅ | Auth + Feed + Settings + Biblioteca + Backend base |
| 🎯 Versión 1.0 | 30 Abr 2026 | 🔄 | Relatos + Chat + Poly conectados de punta a punta |
| 🎯 Versión 2.0 | 30 Jun 2026 | ⏳ | Archivos + Exportación + mejora UX |
| 🎯 Versión 3.0 | 30 Sep 2026 | ⏳ | Suscripciones + Pagos + Admin funcional |

---

## 📊 MÉTRICAS DE PROGRESO

### 📈 Estadísticas Actuales:
- **Total de Módulos:** 13
- **Módulos Completados:** 6 (46%)
- **Módulos en Progreso / Prototipo:** 5 (39%)
- **Módulos Planeados:** 2 (15%)
- **Total de Tareas:** 100+
- **Tareas Completadas:** 67 (67%)

### 🏆 Logros Recientes:
- ✅ **1 Mar 2026:** Frontend base y settings quedaron integrados con el backend
- ✅ **1 Mar 2026:** Relatos y chat quedaron en estado MVP funcional
- ✅ **1 Mar 2026:** Router actualizado con soporte de rutas dinámicas
- ✅ **1 Mar 2026:** Seguridad por usuario reforzada con JWT en controladores
- ✅ **27 Feb 2026:** Completado sistema de verificación de correo

---

## 🔧 MEJORAS TÉCNICAS PENDIENTES

### 🚀 Servidor HTTP:
- [ ] **SRV-001:** Manejo de CORS
- [ ] **SRV-002:** Rate limiting por IP
- [ ] **SRV-003:** Compresión GZIP
- [ ] **SRV-004:** Logs estructurados (JSON)
- [ ] **SRV-005:** Health checks

### 🔐 Seguridad:
- [ ] **SEC-001:** Refresh tokens
- [ ] **SEC-002:** Expiración y renovación automática de JWT
- [ ] **SEC-003:** Protección contra ataques comunes
- [ ] **SEC-004:** Validación avanzada de inputs

### 🗄️ Base de Datos:
- [ ] **DB-001:** Pool de conexiones (HikariCP)
- [ ] **DB-002:** Migrations/versioning del schema
- [ ] **DB-003:** Índices optimizados
- [ ] **DB-004:** Backup automático

### 🧪 Testing:
- [ ] **TEST-001:** Tests unitarios para Services
- [ ] **TEST-002:** Tests de integración para DAOs
- [ ] **TEST-003:** Tests E2E para Controllers
- [ ] **TEST-004:** Pipeline de CI/CD

---

## 📋 REQUERIMIENTOS FUTUROS

### 🔄 Autenticación por Correo (2FA):
- **Estado:** Por definir
- **Prioridad:** Alta
- **Descripción:** Sistema de autenticación de dos factores vía correo
- **Tareas estimadas:** 8-10 tareas
- **Tiempo estimado:** 2-3 semanas

### 📱 API REST Completa:
- **Estado:** Planeado
- **Prioridad:** Media
- **Descripción:** Documentación completa con Swagger/OpenAPI

### 🌐 Frontend Integration:
- **Estado:** En progreso
- **Prioridad:** Media
- **Descripción:** API alineada para consumo frontend en autenticación, settings y estanterías

### 🖥️ Frontend de Producto:
- **Estado:** En progreso
- **Prioridad:** Alta
- **Descripción:** Falta cerrar Poly, creador creativo, panel admin y pruebas de UX completas

---

## 📝 NOTAS Y DECISIONES

### 🏗️ Arquitectura:
- **Patrón:** Controller → Service → DAO
- **Frontend:** HTML + CSS + JavaScript modular por pantallas
- **Respuestas:** JSON consistentes con ApiResponse
- **Autenticación:** JWT con roles integrados
- **Base de datos:** MySQL con relaciones completas
- **Routing:** Soporte para rutas estáticas y dinámicas

### 🔧 Tecnologías:
- **Backend:** Java 25 puro (sin frameworks)
- **Servidor:** HTTP sockets personalizados
- **IA:** Google Gemini API
- **Email:** JavaMail con Gmail SMTP

### 📋 Convenciones:
- **Nomenclatura:** CamelCase (clases), snake_case (DB)
- **JSON:** Gson para serialización
- **Errores:** Códigos HTTP estándar
- **Logs:** Salida estándar para debugging

---

## 📞 CONTACTO Y COORDINACIÓN

### 👥 Equipo:
- **Backend Lead:** [Asignar]
- **Frontend Lead:** [Asignar]
- **DevOps:** [Asignar]
- **QA:** [Asignar]

### 📅 Reuniones:
- **Sprint Planning:** Lunes 9:00 AM
- **Daily Standup:** Diario 10:00 AM
- **Sprint Review:** Viernes 4:00 PM
- **Retrospective:** Viernes 5:00 PM

---

## 🔄 ACTUALIZACIÓN DEL ROADMAP

**Frecuencia:** Semanal  
**Responsable:** Tech Lead  
**Formato:** Markdown en Git  
**Ubicación:** `./ROADMAP.md`

**Para actualizar:**
1. Marcar tareas completadas con `[x]`
2. Actualizar fechas y estados
3. Mover tareas entre secciones según progreso
4. Actualizar métricas y estadísticas
5. Commit con mensaje descriptivo

---

*Última actualización: 1 Marzo 2026 por el equipo del proyecto*
