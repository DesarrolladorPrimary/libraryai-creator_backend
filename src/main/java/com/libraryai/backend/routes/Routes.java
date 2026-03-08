package com.libraryai.backend.routes;

// Importamos los controladores
import com.libraryai.backend.controller.UserController;
import com.libraryai.backend.controller.SettingsController;
import com.libraryai.backend.controller.ai.AiController;
import com.libraryai.backend.controller.auth.LoginController;
import com.libraryai.backend.controller.auth.RecuperacionController;
import com.libraryai.backend.controller.UploadController;
import com.libraryai.backend.controller.ShelfController;
import com.libraryai.backend.controller.StoryController;
import com.libraryai.backend.controller.chats.ChatController;
import com.libraryai.backend.middleware.AuthMiddleware;
// Importamos nuestro Router personalizado
import com.libraryai.backend.server.Router;
// HttpHandler es la interfaz que retornaremos
import com.sun.net.httpserver.HttpHandler;

/**
 * RUTAS - El archivo de configuración de rutas
 *
 * Aquí se definen TODAS las rutas de la aplicación.
 * Es como el "mapa" que le dice al servidor qué hacer con cada petición.
 *
 * Para agregar una nueva ruta:
 * 1. Importar el Controller correspondiente
 * 2. Usar router.get/post/put/delete(PATH, HANDLER)
 */
public class Routes {

    // Creamos una instancia del Router que almacenará todas las rutas
    Router router = new Router();

    /**
     * MÉTODO QUE REGISTRA TODAS LAS RUTAS
     *
     * Este método:
     * 1. Registra cada ruta con su método HTTP, path y handler
     * 2. Retorna el Router configurado para que el servidor lo use
     *
     * @return Router configurado con todas las rutas (es un HttpHandler)
     */
    public HttpHandler configureRoutes() {

        AuthMiddleware auth = new AuthMiddleware();

        // ========== RUTAS DE AUTH ==========
        router.post("/api/v1/login", LoginController.loginUser());

        // Rutas de recuperación de contraseña (sin auth, el usuario viene del correo)
        router.post("/api/v1/recuperar", RecuperacionController.solicitarRecuperacion());
        router.get("/api/v1/recuperar/validar", RecuperacionController.validarToken());
        router.put("/api/v1/recuperar/nueva", RecuperacionController.nuevaPassword());

        // Ruta de verificación de correo (sin auth)
        router.get("/api/v1/verificar", RecuperacionController.verificarCorreo());

        // ========== RUTAS DE USUARIOS ==========
        router.get("/api/v1/usuarios",
                auth.proteger(UserController.listUsers(), "Admin"));

        router.post("/api/v1/usuarios", UserController.createUser());

        // Abierta a Gratuito y Premium para que settings-user.html cargue el perfil
        router.get("/api/v1/usuarios/id",
                auth.proteger(UserController.getUserById(), "Gratuito", "Premium", "Admin"));

        router.put("/api/v1/usuarios/id",
                auth.proteger(UserController.updateUser(), "Gratuito", "Premium"));

        // Actualizar un solo campo del usuario
        router.put("/api/v1/usuarios/campo",
                auth.proteger(UserController.updateCampo(), "Gratuito", "Premium"));

        router.delete("/api/v1/usuarios/id",
                auth.proteger(UserController.deleteUser(), "Gratuito", "Premium", "Admin"));

        // ========== RUTAS DE IA ==========
        router.post("/api/v1/generar-historias",
                auth.proteger(AiController.generateStory(), "Gratuito", "Premium"));

        // ========== RUTAS DE CONFIGURACIONES ==========
        router.get("/api/v1/settings/instruccion-ia",
                auth.proteger(SettingsController.getInstruccionIA(), "Gratuito", "Premium"));

        router.put("/api/v1/settings/instruccion-ia",
                auth.proteger(SettingsController.updateInstruccionIA(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/suscripcion",
                auth.proteger(SettingsController.getSuscripcion(), "Gratuito", "Premium"));

        // NUEVAS RUTAS SEGÚN RF_32
        router.get("/api/v1/settings/version-ia",
                auth.proteger(SettingsController.getVersionIA(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/modelo-disponible",
                auth.proteger(SettingsController.getModeloDisponible(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/sistema",
                auth.proteger(SettingsController.getInfoSistema(), "Gratuito", "Premium"));

        // ========== RUTAS DE ARCHIVOS ==========
        router.post("/api/v1/upload/perfil",
                auth.proteger(UploadController.subirFotoPerfil(), "Gratuito", "Premium"));

        router.post("/api/v1/upload/relato",
                auth.proteger(UploadController.subirArchivoRelato(), "Gratuito", "Premium"));

        router.delete("/api/v1/upload/relato",
                auth.proteger(UploadController.eliminarArchivoRelato(), "Gratuito", "Premium"));

        // ========== RUTAS DE ESTANTERÍAS ==========
        router.get("/api/v1/estanterias",
                auth.proteger(ShelfController.listShelves(), "Gratuito", "Premium"));
        
        router.post("/api/v1/estanterias",
                auth.proteger(ShelfController.createShelf(), "Gratuito", "Premium"));
        
        router.put("/api/v1/estanterias",
                auth.proteger(ShelfController.updateShelf(), "Gratuito", "Premium"));
        
        router.delete("/api/v1/estanterias",
                auth.proteger(ShelfController.deleteShelf(), "Gratuito", "Premium"));

        // ========== RUTAS DE RELATOS ========== 
        router.post("/api/v1/stories",
                auth.proteger(StoryController.createStory(), "Gratuito", "Premium"));
        
        router.get("/api/v1/stories",
                auth.proteger(StoryController.getUserStories(), "Gratuito", "Premium"));
        
        router.get("/api/v1/stories/stats",
                auth.proteger(StoryController.getUserStoryStats(), "Gratuito", "Premium"));
        
        router.get("/api/v1/stories/{id}",
                auth.proteger(StoryController.getStoryById(), "Gratuito", "Premium"));

        router.get("/api/v1/stories/{id}/configuracion-ia",
                auth.proteger(StoryController.getAIConfiguration(), "Gratuito", "Premium"));
        
        router.put("/api/v1/stories/{id}",
                auth.proteger(StoryController.updateStory(), "Gratuito", "Premium"));

        router.put("/api/v1/stories/{id}/configuracion-ia",
                auth.proteger(StoryController.updateAIConfiguration(), "Gratuito", "Premium"));

        router.post("/api/v1/stories/{id}/export",
                auth.proteger(StoryController.exportStory(), "Gratuito", "Premium"));
        
        router.delete("/api/v1/stories/{id}",
                auth.proteger(StoryController.deleteStory(), "Gratuito", "Premium"));

        // ========== RUTAS DE CHAT ==========
        router.post("/api/v1/chat/message",
                auth.proteger(ChatController.sendMessage(), "Gratuito", "Premium"));
        
        router.get("/api/v1/chat/{relatoId}",
                auth.proteger(ChatController.getChatHistory(), "Gratuito", "Premium"));
        
        router.get("/api/v1/chat/{relatoId}/stats",
                auth.proteger(ChatController.getChatStats(), "Gratuito", "Premium"));
        
        router.delete("/api/v1/chat/{relatoId}/clear",
                auth.proteger(ChatController.clearChatHistory(), "Gratuito", "Premium"));

        // ========== AQUÍ PUEDES AGREGAR MÁS RUTAS ==========
        // Ejemplo:
        // router.get("/api/v1/relatos", auth.proteger(RelatoController.list(), "Gratuito", "Premium"));

        // Retornamos el router con todas las rutas configuradas
        return router;
    }
}
