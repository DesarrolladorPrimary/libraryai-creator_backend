# Roadmap Backend – Library Creator (modo novato, sin perderse)

## REGLA GENERAL
No avanzamos al siguiente punto hasta que el actual:
- funcione
- esté probado (Postman / curl)
- lo entiendas

---

## FASE 0 – Preparación (una sola vez)
Objetivo: que la BD tenga lo mínimo para que el backend no falle.

- [ ] Insertar roles:
  - ADMIN
  - GRATUITO
  - PREMIUM

- [ ] Insertar modelos IA:
  - Modelo "low" (para gratuito)
  - Modelo "full" (para premium)

- [ ] Crear usuario ADMIN inicial
  - correo
  - contraseña hasheada
  - rol ADMIN

Resultado:
✔ La base deja de estar “muerta”
✔ Puedes loguearte como admin

---

## FASE A – AUTENTICACIÓN (EMPEZAMOS AQUÍ)

### A1. Registro
Objetivo: crear usuarios correctamente.

- Endpoint: POST /auth/register
- Hace:
  - valida correo y contraseña
  - hashea contraseña
  - crea usuario con rol GRATUITO
- Respuestas:
  - 201 si ok
  - 409 si correo existe
  - 400 si input inválido

✔ Usuario creado en BD

---

### A2. Login
Objetivo: obtener JWT válido.

- Endpoint: POST /auth/login
- Hace:
  - busca usuario
  - verifica contraseña
  - obtiene rol real desde BD
  - genera JWT con:
    - userId
    - correo
    - rol
    - expiración

- Respuestas:
  - 200 + token
  - 401 si credenciales malas

✔ Token válido con rol dentro

---

### A3. Middleware de autenticación
Objetivo: proteger el backend.

- Rutas públicas:
  - /auth/login
  - /auth/register

- Todas las demás:
  - requieren Authorization: Bearer <token>

- Si:
  - no hay token → 401
  - token inválido → 401
  - token válido → se inyecta UserContext

✔ Backend ya no está abierto

---

### A4. Permisos
Objetivo: control real de acceso.

- Rol ADMIN:
  - endpoints admin

- Rol GRATUITO / PREMIUM:
  - endpoints normales

- Si no autorizado:
  - 403

✔ Seguridad básica completa

---

## FASE B – CONFIGURACIÓN IA (GLOBAL)

### B1. Defaults al registrar
Objetivo: que ningún usuario tenga valores null.

Al registrar usuario, crear:
- tono: neutral
- creatividad: medio
- longitud: corta
- estilo: narrativo
- instrucciones IA: vacío

✔ El frontend siempre recibe datos

---

### B2. Settings IA
Objetivo: que funcione la pantalla de Configuración.

- GET /ai/settings
- PATCH /ai/settings

Validar valores permitidos.

✔ Configuración persistente de Poly

---

## FASE C – CHATS + POLY

### C1. Chats
- crear chat
- listar chats
- renombrar
- eliminar

✔ UI de chats funcional

---

### C2. Mensajes
- enviar mensaje
- validar contenido
- calcular config efectiva
- decidir modelo por plan
- llamar IA
- guardar historial
- responder

✔ Poly conversa de verdad

---

## FASE D – EXPORTAR + BIBLIOTECA
(Después, no ahora)

---

## FASE E – JAVASCRIPT MÍNIMO
(Después, no ahora)

---

## ORDEN REAL DE TRABAJO
1. Seed (roles, modelos, admin)
2. Registro
3. Login
4. Middleware JWT
5. Permisos
6. Settings IA
7. Chats
8. Mensajes
9. Frontend JS

---

## NORMA FINAL
❌ No saltar pasos  
❌ No hacer frontend sin API  
✔ Un endpoint bien hecho > diez a medias
