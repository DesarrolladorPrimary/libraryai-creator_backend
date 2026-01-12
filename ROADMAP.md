# 🗺️ Mapa de Aventura: Library AI Backend

Este es tu plan de batalla. Olvida las listas aburridas; esta es la secuencia lógica para construir un backend robusto sin perderte en el intento.

---

## ✅ Logros Desbloqueados (Completado)

> _Lo que ya funciona. ¡Buen trabajo!_

- [x] **El Motor:** Servidor HTTP nativo (`com.sun.net.httpserver`) funcionando.
- [x] **La Base:** Conexión a MySQL establecida.
- [x] **Identidad:** Registro de usuarios funcional (`UsuarioDao`, `Service`, `Controller`).
- [x] **El Oráculo:** Conexión inicial probada con Gemini API.

---

## 🚀 Nivel 1: Arquitectura de Elite (URGENTE)

_Objetivo: Limpiar el código actual para que programar lo demás sea fácil y rápido._

> _Actualmente `ServerMain` hace demasiado. Vamos a delegar._

1. **[ ] El Mensajero (`ApiRequest`)**

   - Crear una clase que envuelva `HttpExchange`.
   - **Misión:** Poder hacer `request.getBody()` y obtener un JSON limpio sin lidiar con `InputStream` manualmente.

2. **[ ] El Diplomático (`ApiResponse`)**

   - Crear utilidades para responder.
   - **Misión:** Responder con `ApiResponse.success(datos)` o `ApiResponse.error(code, "mensaje")` en una sola línea.

3. **[ ] La Torre de Control (`Router`)**
   - Crear un sistema para definir rutas tipo `router.get("/libros", controlador::listar)`.
   - **Misión:** Limpiar `ServerMain` para que solo tenga 3 líneas de configuración.

---

## 📚 Nivel 2: La Gran Biblioteca (Libros)

_Objetivo: Darle vida a la funcionalidad principal._

1. **[ ] El Manuscrito (Modelo `Libro`)**

   - Definir la clase POJO: `id`, `titulo`, `sinopsis`, `genero`, `estado` (borrador/terminado).

2. **[ ] Los Archivos (DAO de Libros)**

   - Implementar `insert`, `findAllByUsuario`, `findById`, `update`, `delete`.
   - **Reto:** Asegurar que un usuario solo vea _sus_ libros.

3. **[ ] La Ventanilla (`LibroController`)**
   - Conectar el Router con el DAO.
   - Endpoints: `POST /libros`, `GET /libros`.

---

## 🤖 Nivel 3: Despertando a Poly (IA)

_Objetivo: Hacer que la IA sea útil de verdad._

1. **[ ] El Canal de Comunicación (`ChatController`)**

   - Crear endpoint `POST /api/chat`.
   - Recibir mensaje del usuario -> Enviar a Gemini -> Devolver respuesta.

2. **[ ] Memoria de Pez (Contexto Básico)**

   - Hacer que Poly recuerde los últimos 3 mensajes para mantener una conversación fluida.

3. **[ ] El Asistente Creativo**
   - Crear un "System Prompt" especial para que Poly actúe como un experto escritor, no como un bot genérico.

---

## 📦 Nivel 4: Ordenando el Caos (Estanterías)

_Objetivo: Organización avanzada._

1. **[ ] El Estante (Modelo y Tabla)**
   - Crear tabla `estanterias` y modelo `Estanteria`.
2. **[ ] La Asociación**
   - Tabla intermedia `libro_estanteria` (relación muchos a muchos).
   - Poder añadir un libro a una estantería.

---

## 🛡️ Nivel 5: La Fortaleza (Seguridad)

_Objetivo: Proteger tu creación._

1. **[ ] El Guardián (Middleware de Auth)**
   - Crear una anotación o filtro que verifique si existe un usuario logueado antes de dejar pasar a `/libros`.
   - (Por ahora podemos usar un ID de usuario simulado en los headers).

---

## 📝 Notas del Desarrollador

- **Regla de Oro:** No pases al Nivel 2 sin terminar el Nivel 1. Una buena arquitectura te ahorrará horas de sufrimiento después.
- **Diversión:** Si te aburres del CRUD (Nivel 2), salta un rato al Nivel 3 (IA) para ver cosas mágicas, y luego vuelve.
