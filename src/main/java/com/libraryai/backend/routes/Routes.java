package com.libraryai.backend.routes;

// Importamos los controladores
import com.libraryai.backend.controller.UserController;
import com.libraryai.backend.controller.AdminController;
import com.libraryai.backend.controller.LibraryController;
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
 */
public class Routes {

    Router router = new Router();

    public HttpHandler configureRoutes() {

        AuthMiddleware auth = new AuthMiddleware();

        router.post("/api/v1/login", LoginController.loginUser());

        router.post("/api/v1/recuperar", RecuperacionController.solicitarRecuperacion());
        router.get("/api/v1/recuperar/validar", RecuperacionController.validarToken());
        router.put("/api/v1/recuperar/nueva", RecuperacionController.nuevaPassword());

        router.get("/api/v1/verificar", RecuperacionController.verificarCorreo());

        router.get("/api/v1/usuarios",
                auth.proteger(UserController.listUsers(), "Admin"));

        router.post("/api/v1/usuarios", UserController.createUser());

        router.get("/api/v1/usuarios/id",
                auth.proteger(UserController.getUserById(), "Gratuito", "Premium", "Admin"));

        router.put("/api/v1/usuarios/id",
                auth.proteger(UserController.updateUser(), "Gratuito", "Premium"));

        router.put("/api/v1/usuarios/campo",
                auth.proteger(UserController.updateCampo(), "Gratuito", "Premium"));

        router.delete("/api/v1/usuarios/id",
                auth.proteger(UserController.deleteUser(), "Gratuito", "Premium", "Admin"));

        router.get("/api/v1/admin/users",
                auth.proteger(AdminController.listUsers(), "Admin"));

        router.put("/api/v1/admin/users/status",
                auth.proteger(AdminController.updateUserStatus(), "Admin"));

        router.put("/api/v1/admin/users/role",
                auth.proteger(AdminController.updateUserRole(), "Admin"));

        router.get("/api/v1/admin/stats",
                auth.proteger(AdminController.getStats(), "Admin"));

        router.get("/api/v1/admin/plans",
                auth.proteger(AdminController.getPlans(), "Admin"));

        router.get("/api/v1/admin/payments",
                auth.proteger(AdminController.getPayments(), "Admin"));

        router.post("/api/v1/generar-historias",
                auth.proteger(AiController.generateStory(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/instruccion-ia",
                auth.proteger(SettingsController.getInstruccionIA(), "Gratuito", "Premium"));

        router.put("/api/v1/settings/instruccion-ia",
                auth.proteger(SettingsController.updateInstruccionIA(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/suscripcion",
                auth.proteger(SettingsController.getSuscripcion(), "Gratuito", "Premium"));

        router.put("/api/v1/settings/suscripcion/simular",
                auth.proteger(SettingsController.simulateSuscripcion(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/version-ia",
                auth.proteger(SettingsController.getVersionIA(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/modelo-disponible",
                auth.proteger(SettingsController.getModeloDisponible(), "Gratuito", "Premium"));

        router.get("/api/v1/settings/sistema",
                auth.proteger(SettingsController.getInfoSistema(), "Gratuito", "Premium"));

        router.post("/api/v1/upload/perfil",
                auth.proteger(UploadController.subirFotoPerfil(), "Gratuito", "Premium"));

        router.post("/api/v1/upload/relato",
                auth.proteger(UploadController.subirArchivoRelato(), "Gratuito", "Premium"));

        router.delete("/api/v1/upload/relato",
                auth.proteger(UploadController.eliminarArchivoRelato(), "Gratuito", "Premium"));

        router.get("/api/v1/library/documents",
                auth.proteger(LibraryController.listDocuments(), "Gratuito", "Premium"));

        router.get("/api/v1/library/documents/download",
                auth.proteger(LibraryController.downloadDocument(), "Gratuito", "Premium"));

        router.delete("/api/v1/library/documents",
                auth.proteger(LibraryController.deleteDocument(), "Gratuito", "Premium"));

        router.get("/api/v1/estanterias",
                auth.proteger(ShelfController.listShelves(), "Gratuito", "Premium"));

        router.post("/api/v1/estanterias",
                auth.proteger(ShelfController.createShelf(), "Gratuito", "Premium"));

        router.put("/api/v1/estanterias",
                auth.proteger(ShelfController.updateShelf(), "Gratuito", "Premium"));

        router.delete("/api/v1/estanterias",
                auth.proteger(ShelfController.deleteShelf(), "Gratuito", "Premium"));

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

        router.post("/api/v1/chat/message",
                auth.proteger(ChatController.sendMessage(), "Gratuito", "Premium"));

        router.get("/api/v1/chat/{relatoId}",
                auth.proteger(ChatController.getChatHistory(), "Gratuito", "Premium"));

        router.get("/api/v1/chat/{relatoId}/stats",
                auth.proteger(ChatController.getChatStats(), "Gratuito", "Premium"));

        router.delete("/api/v1/chat/{relatoId}/clear",
                auth.proteger(ChatController.clearChatHistory(), "Gratuito", "Premium"));

        return router;
    }
}
