# 🗺️ ROADMAP - Library Creator Backend

Este documento contiene el plan de desarrollo y las tareas pendientes del proyecto.

---

## ✅ Completado

- [x] Estructura base del servidor HTTP con Sockets
- [x] Modelo de Usuario
- [x] DAO de Usuarios (registro en DB)
- [x] Conexión a base de datos MySQL
- [x] Integración básica con Gemini API
- [x] Organización modular del proyecto

---

## 🔨 En Progreso

### Fase 1: Refactorización del Servidor

- [ ] Crear clase `HttpRequest` para parsear peticiones
- [ ] Crear clase `HttpResponse` para construir respuestas
- [ ] Crear clase `Router` para mapear rutas a controladores
- [ ] Separar lógica de `ServerMain` en componentes

---

## 📋 Pendiente

### Fase 2: Controladores y Servicios

- [ ] `UsuarioController` - Endpoints de usuarios
- [ ] `UsuarioService` - Lógica de negocio de usuarios
- [ ] `LibroController` - Endpoints de libros
- [ ] `LibroService` - Lógica de negocio de libros
- [ ] `ChatController` - Endpoints de interacción con Poly

### Fase 3: Modelos y DAOs

- [ ] Modelo `Libro`
- [ ] Modelo `Estanteria`
- [ ] Modelo `Suscripcion`
- [ ] `LibroDao` - CRUD de libros
- [ ] `EstanteriaDao` - CRUD de estanterías

### Fase 4: Funcionalidades Core

- [ ] Sistema de autenticación (login/registro)
- [ ] Gestión de sesiones
- [ ] CRUD completo de libros
- [ ] Gestión de estanterías virtuales
- [ ] Versionamiento de textos

### Fase 5: Integración IA (Poly)

- [ ] Servicio de chat con Gemini
- [ ] Parametrización del asistente
- [ ] Historial de conversaciones
- [ ] Generación de narrativas guiadas

### Fase 6: Exportación

- [ ] Exportar a PDF
- [ ] Exportar a Word

### Fase 7: Suscripciones

- [ ] Modelo de planes de suscripción
- [ ] Límites de almacenamiento por plan
- [ ] Límites de uso de IA por plan

---

## 🌐 API Endpoints Planificados

### Usuarios

| Método | Ruta             | Descripción       |
| ------ | ---------------- | ----------------- |
| `POST` | `/usuarios`      | Registrar usuario |
| `POST` | `/login`         | Iniciar sesión    |
| `GET`  | `/usuarios/{id}` | Obtener perfil    |

### Libros

| Método   | Ruta           | Descripción               |
| -------- | -------------- | ------------------------- |
| `POST`   | `/libros`      | Crear libro               |
| `GET`    | `/libros`      | Listar libros del usuario |
| `GET`    | `/libros/{id}` | Obtener libro             |
| `PUT`    | `/libros/{id}` | Actualizar libro          |
| `DELETE` | `/libros/{id}` | Eliminar libro            |

### Estanterías

| Método | Ruta                       | Descripción               |
| ------ | -------------------------- | ------------------------- |
| `POST` | `/estanterias`             | Crear estantería          |
| `GET`  | `/estanterias`             | Listar estanterías        |
| `POST` | `/estanterias/{id}/libros` | Añadir libro a estantería |

### Chat IA (Poly)

| Método | Ruta              | Descripción           |
| ------ | ----------------- | --------------------- |
| `POST` | `/chat`           | Enviar mensaje a Poly |
| `GET`  | `/chat/historial` | Obtener historial     |

---

## 📝 Notas

- El servidor está construido sin frameworks (Sockets puros) con propósito educativo
- La prioridad actual es refactorizar el servidor antes de agregar más funcionalidades
