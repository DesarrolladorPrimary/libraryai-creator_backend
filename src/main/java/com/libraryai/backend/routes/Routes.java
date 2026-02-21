package com.libraryai.backend.routes;

// Importamos los controladores
import com.libraryai.backend.controller.UserController;
import com.libraryai.backend.controller.SettingsController;
import com.libraryai.backend.controller.ai.AiController;
import com.libraryai.backend.controller.auth.LoginController;
import com.libraryai.backend.controller.auth.RecuperacionController;
import com.libraryai.backend.controller.UploadController;
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

        // ========== RUTAS DE ARCHIVOS ==========
        router.post("/api/v1/upload/perfil",
                auth.proteger(UploadController.subirFotoPerfil(), "Gratuito", "Premium"));

        // ========== AQUÍ PUEDES AGREGAR MÁS RUTAS ==========
        // Ejemplo:
        // router.get("/api/v1/relatos", auth.proteger(RelatoController.list(), "Gratuito", "Premium"));

        // Retornamos el router con todas las rutas configuradas
        return router;
    }
}
