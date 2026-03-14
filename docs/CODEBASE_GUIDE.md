# Codebase Guide

## Propósito

Esta guía documenta la estructura actual del backend para que el código sea más navegable sin depender de leer clase por clase.

## Capas principales

### `controller`

- Expone endpoints HTTP.
- Valida parámetros mínimos de entrada.
- Traduce respuestas del servicio a `ApiResponse`.

### `service`

- Contiene la lógica de negocio.
- Orquesta DAOs, moderación, permisos, cuotas y composición de respuesta.
- Aquí viven las reglas de RF.

### `dao`

- Encapsula acceso a base de datos.
- Ejecuta consultas SQL y devuelve `JsonObject` o `JsonArray`.
- No debería contener reglas de negocio complejas.

### `models`

- Representa entidades del dominio usadas en persistencia o transporte interno.

### `config`

- Resuelve variables de entorno, conexión a BD y configuración de IA.

### `seeders`

- Garantiza datos mínimos para instalaciones nuevas:
  - roles
  - modelos IA
  - palabras prohibidas

## Flujos clave

### Autenticación

1. `LoginController` recibe el payload.
2. `LoginService` valida formato, correo verificado y contraseña.
3. `JwtUtil` emite el token con `correo`, `rol` e `id`.

### Poly

1. `ChatController` y `AiController` reciben peticiones del chat y de ayudas IA.
2. `ChatService` resuelve relato, contexto, instrucciones y modelos candidatos.
3. `GeminiService` intenta generar respuesta con el catálogo permitido por plan.
4. La respuesta puede actualizar chat, canvas o ambos.

### Relatos

1. `StoryController` delega creación, lectura, actualización, borrado y exportación.
2. `StoryService` valida:
  - ownership
  - modo de origen
  - moderación
  - estantería
  - modelo IA
  - cuota de almacenamiento
3. `StoryDao`, `StoryVersionDao` y `UploadedFileDao` persisten estado y exportados.

### Configuración y planes

1. `SettingsController` expone instrucción IA, suscripción, modelos y versión IA.
2. `SettingsService` traduce plan activo a modelo efectivo y catálogo disponible.
3. `SettingsDao` concentra la consulta de suscripción y catálogo de modelos.

## Archivos más críticos

- `service/ChatService.java`: flujo principal de Poly y construcción de prompts.
- `service/StoryService.java`: reglas de negocio de relatos y biblioteca.
- `dao/SettingsDao.java`: suscripción, catálogo de modelos y fallback por plan.
- `service/SettingsService.java`: resolución del modelo efectivo para cada usuario.

## Convenciones usadas en la limpieza actual

- Las validaciones repetidas se extraen a helpers privados.
- Las respuestas de error se centralizan cuando no cambian la forma del contrato.
- La lógica de negocio se mantiene en `service`; el `controller` no decide reglas.
- Los cambios de clean code deben preservar endpoints, payloads y claves JSON existentes.
