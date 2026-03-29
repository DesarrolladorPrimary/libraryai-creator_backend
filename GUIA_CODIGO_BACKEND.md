# Guia de codigo backend

Este documento resume que hace cada archivo importante del backend para que puedas ubicarte rapido al leer el proyecto.

## Flujo general
- `src/main/java/com/libraryai/backend/App.java`: punto de arranque; carga configuracion, seeders y levanta el servidor.
- `src/main/java/com/libraryai/backend/routes/Routes.java`: registra los endpoints y decide que controlador atiende cada ruta.
- `src/main/java/com/libraryai/backend/middleware/AuthMiddleware.java`: protege rutas por sesion y rol.

## Configuracion y servidor
- `config/AIConfig.java`: concentra nombres base de modelos y banderas de integracion con Gemini.
- `config/DatabaseConnection.java`: crea conexiones JDBC a MySQL.
- `server/ServerMain.java`: inicia el `HttpServer` y monta rutas/handlers.
- `server/Router.java`: resuelve coincidencias entre request y rutas registradas.
- `server/StaticFileHandler.java`: sirve archivos estaticos cuando hace falta.
- `server/http/ApiRequest.java`: wrapper interno de la request HTTP.
- `server/http/ApiResponse.java`: helper para responder JSON y codigos HTTP.

## Capa AI
- `ai/GeminiAI.java`: llamada concreta al proveedor Gemini y manejo de errores del proveedor.
- `service/ai/GeminiService.java`: orquesta intento de modelos, fallbacks y contrato comun para respuestas IA.
- `controller/ai/AiController.java`: endpoints de ayuda IA fuera del chat de Poly.
- `dao/ai/AIConfigurationDao.java`: persiste la configuracion IA por relato.

## Controladores
- `controller/admin/AdminController.java`: endpoints del panel admin; usuarios, planes, stats y moderacion.
- `controller/auth/LoginController.java`: login y validacion inicial de acceso.
- `controller/auth/RecuperacionController.java`: flujo de recuperacion de contraseña.
- `controller/chats/ChatController.java`: endpoints del chat Poly.
- `controller/library/LibraryController.java`: lectura de biblioteca, documentos y descargas.
- `controller/settings/SettingsController.java`: perfil, planes, modelos disponibles e instrucciones IA.
- `controller/shelf/ShelfController.java`: CRUD de estanterias.
- `controller/story/StoryController.java`: CRUD de relatos, versionado y exportacion.
- `controller/upload/UploadController.java`: subida de archivos fuente al sistema.
- `controller/user/UserController.java`: registro, perfil y gestion base del usuario.

## Servicios de negocio
- `service/admin/AdminService.java`: valida y coordina operaciones del panel admin.
- `service/auth/LoginService.java`: autentica usuario, genera token y controla acceso.
- `service/auth/RecuperacionService.java`: genera/verifica tokens de recuperacion y actualiza contraseña.
- `service/chat/ChatService.java`: corazon de Poly; recibe mensajes, valida moderacion, llama a IA y guarda conversacion.
- `service/email/EmailService.java`: construye y envia correos transaccionales.
- `service/library/LibraryService.java`: entrega los datos que alimentan la biblioteca del usuario.
- `service/moderation/ModerationService.java`: revisa texto contra reglas y registra eventos bloqueados.
- `service/settings/SettingsService.java`: compone la informacion de settings y suscripcion para la UI.
- `service/shelf/ShelfService.java`: reglas de negocio para estanterias.
- `service/story/StoryService.java`: ciclo de vida del relato; creacion, edicion, versionado, exportacion y borrado.
- `service/user/UserService.java`: reglas de registro, validacion de nombre/correo, foto, password y baja de cuenta.

## DAO / acceso a datos
- `dao/admin/AdminDao.java`: consultas agregadas de admin, resumenes, auditorias y gestion de planes/usuarios.
- `dao/auth/LoginDao.java`: busca usuarios y roles para autenticacion.
- `dao/auth/RecuperacionDao.java`: persiste tokens y trazas del flujo de recuperacion.
- `dao/chats/ChatDao.java`: inserta, lista y elimina mensajes de chat.
- `dao/email/EmailDao.java`: registra correos enviados/fallidos para auditoria.
- `dao/file/UploadedFileDao.java`: maneja archivos subidos/exportados y su impacto en cuota.
- `dao/moderation/ModerationDao.java`: consulta palabras prohibidas y guarda logs de moderacion.
- `dao/settings/SettingsDao.java`: consultas de perfil, plan, modelos y pagos del usuario.
- `dao/shelves/ShelfDao.java`: persistencia de estanterias y relaciones ligadas.
- `dao/story/StoryDao.java`: persistencia del relato y sus relaciones con estanterias.
- `dao/story/StoryVersionDao.java`: historial de versiones del relato.
- `dao/user/UserDao.java`: persistencia principal del usuario y limpiezas profundas.
- `dao/user/UserRoleDao.java`: asignacion de roles al usuario.

## Modelos
- `models/AIConfiguration.java`: modelo de configuracion IA por relato.
- `models/AIModel.java`: representa un modelo IA del catalogo.
- `models/AccessToken.java`: token persistido para recuperacion/verificacion.
- `models/ChatMessage.java`: mensaje del chat Poly.
- `models/Email.java`: registro de correo transaccional.
- `models/Payment.java`: pago asociado a una suscripcion.
- `models/Permission.java`: modelo de permiso fino; hoy mas preparatorio que operativo.
- `models/Plan.java`: catalogo de planes disponibles.
- `models/Role.java`: rol del sistema.
- `models/Shelf.java`: categoria o estanteria.
- `models/Story.java`: relato/borrador principal.
- `models/StoryVersion.java`: version guardada de un relato.
- `models/Subscription.java`: suscripcion de un usuario a un plan.
- `models/UploadedFile.java`: archivo fisico o exportado registrado en DB.
- `models/User.java`: entidad principal del usuario.

## Seeders
- `seeders/SeedAIModels.java`: asegura el catalogo base de modelos IA.
- `seeders/SeedForbiddenWords.java`: asegura palabras prohibidas iniciales.
- `seeders/SeedRoles.java`: asegura roles base del sistema.

## Utilidades
- `util/DocumentExportBuilder.java`: construye archivos Word/PDF exportables.
- `util/DocumentTextExtractor.java`: extrae texto desde archivos cargados por el usuario.
- `util/JwtUtil.java`: genera y valida JWT.
- `util/QueryParams.java`: parsea query params de requests simples.

## Apoyos de base de datos
- `src/main/resources/db/schema/LC_v3_SQL_final.sql`: esquema principal actual.
- `src/main/resources/db/seed/LC_reset_datos_demo.sql`: limpieza para entorno demo.
- `src/main/resources/db/seed/LC_demo_seed_completo.sql`: datos demo base.
- `src/main/resources/db/queries/pruebas.sql`: consultas utiles para revisar estados de la DB.
