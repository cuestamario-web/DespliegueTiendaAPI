package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.Producto;
import model.Usuario;
import service.ProductoService;
import service.UsuarioService;

import static spark.Spark.*;

public class TiendaApi {

    // 1. Herramientas que usan todas las rutas
    private static final Gson gson = new Gson();
    private static final UsuarioService usuarioService = new UsuarioService();
    private static final ProductoService productoService = new ProductoService();

    // 2. Punto de arranque de la API
    public static void iniciar() {
        int puerto = obtenerPuerto();
        port(puerto);
        configurar();
        rutasUsuarios();
        rutasProductos();
        System.out.println("API corriendo en el puerto " + puerto);
    }

    // Railway (y cualquier PaaS) dice en que puerto escuchar con la variable PORT.
    // En el computador no existe, entonces se usa 4567 como siempre.
    private static int obtenerPuerto() {
        String puerto = System.getenv("PORT");
        if (puerto != null) {
            return Integer.parseInt(puerto);
        }
        return 4567;
    }

    // 3. Reglas para todas las rutas
    private static void configurar() {

        before((req, res) -> res.type("application/json"));

        get("/health", (req, res) -> gson.toJson("API funcionando"));

        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson("Ruta no encontrada");
        });

        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson("Error interno: " + e.getMessage()));
        });
    }

    // 4. Rutas de USUARIOS
    private static void rutasUsuarios() {

        // GET: listar todos
        get("/usuarios", (req, res) -> {
            return gson.toJson(usuarioService.listar());
        });

        // GET: buscar uno por id
        get("/usuarios/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Usuario usuario = buscarUsuario(id);
            if (usuario == null) {
                res.status(404);
                return gson.toJson("Usuario no encontrado");
            }
            return gson.toJson(usuario);
        });

        // POST: crear
        post("/usuarios", (req, res) -> {
            Usuario usuario = gson.fromJson(req.body(), Usuario.class);
            usuarioService.crearUsuario(usuario);
            res.status(201);
            return gson.toJson(usuario);
        });

        // PATCH: cambiar solo los campos que lleguen
        patch("/usuarios/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Usuario actual = buscarUsuario(id);
            if (actual == null) {
                res.status(404);
                return gson.toJson("Usuario no encontrado");
            }

            JsonObject cambios = gson.fromJson(req.body(), JsonObject.class);
            String nombre = actual.getNombre();
            String email = actual.getEmail();
            if (cambios.has("nombre")) {
                nombre = cambios.get("nombre").getAsString();
            }
            if (cambios.has("email")) {
                email = cambios.get("email").getAsString();
            }

            usuarioService.actualizar(id, nombre, email);
            return gson.toJson(new Usuario(id, nombre, email));
        });

        // DELETE: eliminar
        delete("/usuarios/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if (buscarUsuario(id) == null) {
                res.status(404);
                return gson.toJson("Usuario no encontrado");
            }
            usuarioService.eliminar(id);
            return gson.toJson("Usuario eliminado");
        });
    }

    // 5. Rutas de PRODUCTOS (mismo molde)
    private static void rutasProductos() {

        get("/productos", (req, res) -> {
            return gson.toJson(productoService.listar());
        });

        get("/productos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Producto producto = buscarProducto(id);
            if (producto == null) {
                res.status(404);
                return gson.toJson("Producto no encontrado");
            }
            return gson.toJson(producto);
        });

        post("/productos", (req, res) -> {
            Producto producto = gson.fromJson(req.body(), Producto.class);
            productoService.crear(producto);
            res.status(201);
            return gson.toJson(producto);
        });

        patch("/productos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Producto actual = buscarProducto(id);
            if (actual == null) {
                res.status(404);
                return gson.toJson("Producto no encontrado");
            }

            JsonObject cambios = gson.fromJson(req.body(), JsonObject.class);
            String nombre = actual.getNombre();
            double precio = actual.getPrecio();
            if (cambios.has("nombre")) {
                nombre = cambios.get("nombre").getAsString();
            }
            if (cambios.has("precio")) {
                precio = cambios.get("precio").getAsDouble();
            }

            productoService.actualizar(id, nombre, precio);
            return gson.toJson(new Producto(id, nombre, precio));
        });

        delete("/productos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if (buscarProducto(id) == null) {
                res.status(404);
                return gson.toJson("Producto no encontrado");
            }
            productoService.eliminar(id);
            return gson.toJson("Producto eliminado");
        });
    }

    // 6. Ayudantes: buscar por id usando el service
    private static Usuario buscarUsuario(int id) {
        for (Usuario u : usuarioService.listar()) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    private static Producto buscarProducto(int id) {
        for (Producto p : productoService.listar()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
}
