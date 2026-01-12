package com.libraryai.backend.router;

// Importamos el controlador que tiene los handlers de usuarios
import com.libraryai.backend.controller.UserController;
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
public class Rutas {

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
    public HttpHandler ruts() {

        // ========== RUTAS DE USUARIOS ==========

        // GET /api/v1/usuarios → Lista todos los usuarios
        // Cuando alguien haga GET a esta ruta, se ejecuta listarUsuarios()
        router.get("/api/v1/usuarios", UserController.listarUsuarios());
        // GET /api/v1/usuarios/id?id=X → Obtiene un usuario específico por ID
        // Ejemplo: GET /api/v1/usuarios/id?id=5
        router.get("/api/v1/usuarios/id", UserController.obtenerUsuarioId());

        router.post("/api/v1/usuarios", UserController.crearUsuario());

        router.put("/api/v1/usuarios/id", UserController.actualizarUsuario());

        router.delete("/api/v1/usuarios/id", UserController.eliminarUsuario());


        // ========== AQUÍ PUEDES AGREGAR MÁS RUTAS ==========
        // Ejemplo para futuros controllers:
        // rutas.addroute("GET", "/api/v1/libros", LibroController.listarLibros());
        // rutas.addroute("POST", "/api/v1/login", AuthController.login());

        // Retornamos el router con todas las rutas configuradas
        // Este router será usado por el servidor en ServerMain
        return router;
    }

}
