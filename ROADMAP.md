# ROADMAP - Library Creator (Proyecto Completo)

> **Estado actual:** MVP funcional en frontend + backend  
> **Última actualización:** 10 Marzo 2026  
> **Versión:** v3.1

---

## Alcance del roadmap

Este roadmap cubre el estado real del proyecto completo:
- Backend: API, seguridad, base de datos, IA y servicios
- Frontend: auth, feed, settings, biblioteca, Poly, Creativo y admin

Su función es evidenciar:
- qué ya existe y está operativo,
- qué está en estado MVP funcional,
- y qué sigue faltando para cerrar producto.

---

## Reglas de producto ya fijadas

- **Relato = borrador editable**
- **Libro = resultado final convertido/exportado desde Biblioteca**
- **Poly y Creativo generan y editan borradores**
- **Biblioteca es el punto de conversión a libro y descarga final**
- **Suscripciones y pagos son simulados en base de datos; no hay pasarela real en esta fase**

---

## Resumen de progreso

| Módulo | Estado | Progreso | Nota |
|--------|--------|----------|------|
| Infraestructura | ✅ Completado | 100% | Servidor, router, DB, dotenv y rutas dinámicas |
| Usuarios | ✅ Completado | 100% | CRUD, roles base y validaciones |
| Autenticación y seguridad | ✅ Completado | 100% | JWT, recuperación, guards y validaciones |
| Correo y verificación | ✅ Completado | 100% | Verificación y recuperación por correo |
| IA base (Gemini) | 🔄 MVP funcional | 90% | Integrada y operativa, aún requiere más QA de calidad |
| Relatos | 🔄 MVP funcional | 90% | CRUD, guardado, versionado y biblioteca |
| Chat / Poly | 🔄 MVP funcional | 85% | Chat persistente, archivos fuente y parámetros por relato |
| Archivos / Biblioteca | 🔄 MVP funcional | 80% | Upload, descarga, exportación y organización |
| Suscripciones simuladas | 🔄 MVP funcional | 70% | Cambio Gratuito/Premium simulado en BD |
| Frontend base (Auth/UI) | 🔄 MVP funcional | 90% | Auth, sesión, idioma base y estados principales |
| Frontend app (Feed/Settings/Biblioteca) | 🔄 MVP funcional | 90% | Perfil, plan, estanterías, biblioteca y guardado |
| Frontend Poly / Creativo / Admin | 🔄 MVP funcional | 80% | Flujos reales conectados, falta pulido y pruebas |
| Testing / QA | ⏳ Pendiente fuerte | 20% | Compilación validada, falta suite real |

---

## Estado por módulo

### 1. Infraestructura

**Estado:** `COMPLETADO`

#### Ya implementado
- Servidor HTTP propio con sockets TCP
- Router personalizado con soporte de rutas dinámicas
- `ApiRequest` y `ApiResponse`
- Conexión MySQL con variables de entorno
- Middleware base y manejo de errores

#### Pendiente técnico
- Health checks
- Rate limiting
- Logs estructurados
- Pool de conexiones

---

### 2. Usuarios, Auth, Roles y Correo

**Estado:** `COMPLETADO`

#### Ya implementado
- Registro, login y recuperación de contraseña
- Verificación de correo
- JWT con roles
- Roles base: Gratuito, Premium y Admin
- Guards y validaciones más robustas
- Cambio de contraseña y eliminación de cuenta

#### Pendiente técnico
- Mejoras de QA y pruebas automáticas
- Ajustes finos de mensajes y estados visuales

---

### 3. IA base (Gemini)

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- Cliente Gemini configurado desde entorno
- Integración real en servicios de IA
- Uso desde `Poly` y ayudas del creador manual
- Configuración persistente por relato e instrucciones permanentes del usuario
- Fallback controlado cuando Gemini falla

#### Falta por cerrar
- Afinar calidad de prompts y evitar repeticiones
- Validación repetible de calidad en casos reales
- Mejor trazabilidad cuando cae al fallback

---

### 4. Relatos

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- CRUD completo de relatos
- Relatos por usuario con control de ownership
- Relación con estanterías
- Guardado desde Poly y Creativo
- Versionado en `RelatoVersion` al guardar contenido y exportar
- Conversión desde borrador a libro desde biblioteca

#### Falta por cerrar
- Búsqueda y filtrado real
- Categorización
- Historial visible de versiones y restauración
- QA más fuerte sobre contratos

---

### 5. Chat / Poly

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- Crear, abrir, renombrar y eliminar chats
- Modelo `1 relato = 1 chat`
- Persistencia de mensajes en base de datos
- Parámetros IA por relato
- Carga de archivos fuente al contexto
- Guardado del borrador y envío a biblioteca
- Conversión final a libro desde biblioteca, no desde el canvas

#### Falta por cerrar
- QA end-to-end con Gemini real en varios escenarios
- Mejor gestión visual de múltiples archivos fuente
- Pulido de UX en estados de error/carga
- Ajuste fino de prompts y continuidad narrativa

---

### 6. Archivos y Biblioteca

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- `ArchivoUsuario` y relaciones con relatos
- Subida de archivos fuente para IA
- Descarga y eliminación de documentos exportados
- Biblioteca separando `Borradores` y `Libros`
- Estanterías con crear, listar, renombrar y eliminar
- Selección de estantería al convertir un borrador en libro
- Exportación final desde biblioteca

#### Falta por cerrar
- Mejor visualización/gestión de varios archivos fuente
- Persistencia total y validación final de todos los formatos exportados
- Más pruebas sobre cuota por almacenamiento
- Pulido de la UI de biblioteca

---

### 7. Suscripciones simuladas

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- Visualización de plan actual
- Cambio simulado `Gratuito -> Premium -> Gratuito`
- Persistencia en BD de `Suscripcion`, `Pago` y `UsuarioRol`
- Emisión de token nuevo cuando cambia el plan
- Lectura en frontend y admin del estado del plan
- Restricciones base por almacenamiento y modelos

#### Falta por cerrar
- Aplicar más límites funcionales por plan en producto
- Mejor visibilidad admin del historial simulado
- QA del flujo completo en navegador

#### Fuera de alcance actual
- Pasarela de pago real
- Cobro en producción
- Renovaciones automáticas reales

---

### 8. Frontend base

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- Inicio, login, registro y recuperación
- Sesión JWT robusta
- Protección de vistas
- Base URL centralizada para API
- Idioma base en la UI

#### Falta por cerrar
- Traducir más mensajes dinámicos
- QA visual de errores y estados límite

---

### 9. Frontend app (Feed, Settings, Biblioteca)

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- Feed principal navegable
- Perfil conectado al backend
- Cambio de contraseña y eliminación de cuenta
- Instrucciones persistentes para Poly
- Subida de foto de perfil
- Estanterías reales
- Biblioteca con borradores y libros diferenciados
- Conversión a libro desde biblioteca

#### Falta por cerrar
- Pulido visual adicional en biblioteca
- Más pruebas manuales en escenarios límite
- Traducciones restantes

---

### 10. Frontend Poly / Creativo / Admin

**Estado:** `MVP FUNCIONAL`

#### Ya implementado
- `Poly` conectado al backend
- `Creativo` conectado a relatos reales
- Herramientas base de redacción en el creador manual
- Admin con login real y paneles conectados
- Biblioteca como punto final de conversión/exportación

#### Falta por cerrar
- Pulido de UX en Poly, Creativo y Admin
- Mejor consistencia visual entre módulos
- QA end-to-end repetible
- Ajustes finos del rol Premium dentro de la experiencia

---

## Próximas tareas prioritarias

### Prioridad 1: QA real del producto
1. Probar de punta a punta: auth, Poly, Creativo, Biblioteca y suscripciones simuladas
2. Validar Gemini en uso real con archivos y parámetros
3. Corregir regresiones de UX antes de seguir abriendo módulos

### Prioridad 2: Cierre de relatos y biblioteca
1. Búsqueda y filtrado de relatos
2. Historial visible de versiones y restauración
3. Mejor gestión de archivos fuente y documentos exportados

### Prioridad 3: Premium/Admin
1. Reforzar límites funcionales por plan
2. Mejorar panel admin sobre suscripciones simuladas y pagos fake
3. Cerrar métricas y conteos reales en admin

### Prioridad 4: Testing
1. Smoke tests de auth, relatos, chat, upload y exportación
2. Tests de integración para servicios y DAOs críticos
3. Base de QA repetible para no romper el MVP

---

## Hitos actualizados

| Hito | Estado | Descripción |
|------|--------|-------------|
| MVP base | ✅ | Auth + Feed + Settings + Biblioteca + backend operativo |
| MVP de IA | ✅ | Poly y Creativo conectados a relatos reales |
| Biblioteca funcional | ✅ | Borradores y libros diferenciados, exportación desde biblioteca |
| Suscripción simulada | ✅ | Cambio de plan persistido en BD sin pasarela real |
| Cierre de producto v1 | 🔄 | Pendiente QA, búsqueda, versionado visible y pulido UX |

---

## Riesgos actuales

- Falta de pruebas automáticas reales
- Dependencia de QA manual para validar Gemini y flujos complejos
- Pendientes de producto más ligados a cierre y pulido que a arquitectura

---

## Decisiones importantes vigentes

- Arquitectura backend: `Controller -> Service -> DAO`
- Frontend: HTML + CSS + JavaScript modular por pantallas
- Base de datos: MySQL
- IA: Google Gemini
- `Relato` es borrador editable
- `Libro` es resultado final convertido/exportado desde biblioteca
- Los pagos reales no forman parte de esta fase; se usan flujos simulados persistidos en BD

---

*Última actualización: 10 Marzo 2026*
