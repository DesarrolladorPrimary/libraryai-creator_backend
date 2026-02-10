package com.libraryai.backend.routes;

// Importamos el controlador que tiene los handlers de usuarios
import com.libraryai.backend.controller.UserController;
import com.libraryai.backend.controller.ai.AiController;
import com.libraryai.backend.controller.auth.LoginController;
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
 * 2. Usar rutas.addroute(MÉTODO, PATH, HANDLER)
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

        // ========== RUTAS DE USUARIOS ==========
        // GET /api/v1/usuarios � Lista todos los usuarios
        // Cuando alguien haga GET a esta ruta, se ejecuta listarUsuarios()
        router.get("/api/v1/usuarios",
                auth.proteger(UserController.listUsers(), "Admin"));

        router.post("/api/v1/usuarios", UserController.createUser());

        // GET /api/v1/usuarios/id?id=X � Obtiene un usuario espec�fico por ID
        // Ejemplo: GET /api/v1/usuarios/id?id=5
        router.get("/api/v1/usuarios/id",
                auth.proteger(UserController.getUserById(), "Admin"));

        router.put("/api/v1/usuarios/id",
                auth.proteger(UserController.updateUser(), "Gratuito", "Premium"));

        router.delete("/api/v1/usuarios/id",
                auth.proteger(UserController.deleteUser(), "Gratuito", "Premium", "Admin"));

        // ========== RUTAS DE IA ==========
        router.post("/api/v1/generar-historias",
                auth.proteger(AiController.generateStory(), "Gratuito", "Premium"));

        // ========== AQUÍ PUEDES AGREGAR MÁS RUTAS ==========
        // Ejemplo para futuros controllers:
        // rutas.addroute("GET", "/api/v1/libros", LibroController.listarLibros());
        // rutas.addroute("POST", "/api/v1/login", AuthController.login());

        // Retornamos el router con todas las rutas configuradas
        // Este router será usado por el servidor en ServerMain
        return router;
    }

}
