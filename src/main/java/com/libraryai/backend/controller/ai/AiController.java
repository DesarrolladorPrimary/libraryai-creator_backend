package com.libraryai.backend.controller.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.libraryai.backend.server.http.ApiRequest;
import com.libraryai.backend.server.http.ApiResponse;
import com.libraryai.backend.service.ModerationService;
import com.libraryai.backend.service.SettingsService;
import com.libraryai.backend.service.ai.GeminiService;
import com.libraryai.backend.util.JwtUtil;
import com.sun.net.httpserver.HttpHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers HTTP para funciones de IA.
 */
public class AiController {

    /**
     * Handler para generar historias con IA.
     * Lee mensaje/instrucciones, delega al servicio y normaliza status.
     */
    public static HttpHandler generateStory() {
        return exchange -> {
            try {
                System.out.println("\n\nPetición de tipo: " + exchange.getRequestMethod() + " recibida del cliente\n");
                // Lee el body de la petición.
                ApiRequest request = new ApiRequest(exchange);
                String body = request.readBody();

                // Convierte el JSON del body a JsonObject.
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(body, JsonObject.class);

                String mensaje = "";
                String instrucciones = "";

                // Lee campos opcionales del payload.
                if (json.has("mensaje")) {
                    mensaje = json.get("mensaje").getAsString();
                }

                if (json.has("instrucciones")) {
                    instrucciones = json.get("instrucciones").getAsString();
                }

                int usuarioId = getUserIdFromToken(exchange.getRequestHeaders().getFirst("Authorization"));
                if (usuarioId <= 0) {
                    ApiResponse.error(exchange, 401, "Usuario no autenticado");
                    return;
                }

                JsonObject moderationResult = ModerationService.validateText(
                        mensaje + "\n" + instrucciones,
                        usuarioId,
                        "La solicitud contiene contenido NSFW o palabras bloqueadas. Ajusta el texto antes de continuar.",
                        "Solicitud de IA bloqueada por moderacion");
                if (moderationResult != null) {
                    ApiResponse.send(exchange, moderationResult.toString(), 400);
                    return;
                }

                String effectiveInstructions = buildPlanAwareInstructions(usuarioId, instrucciones);

                // Ejecuta la generación y obtiene respuesta.
                JsonObject responseJson = GeminiService.generateText(
                        mensaje,
                        effectiveInstructions,
                        buildModelCandidates(usuarioId));
                
                // Usa status del servicio o 200 por defecto.
                int code = responseJson.has("status") ? responseJson.get("status").getAsInt() : 200;
                if (code < 100 || code > 599) {
                    code = 500;
                }

                String response = responseJson.toString();
                ApiResponse.send(exchange, response, code);



            } catch (JsonIOException e) {
                e.printStackTrace();
                ApiResponse.error(exchange, 404, "Mensaje");
            }

        };
    }

    private static int getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return -1;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        JsonObject tokenInfo = JwtUtil.validateToken(token);

        if (tokenInfo.has("Mensaje") || !tokenInfo.has("Id")) {
            return -1;
        }

        return tokenInfo.get("Id").getAsInt();
    }

    private static String buildPlanAwareInstructions(int usuarioId, String instruccionesBase) {
        StringBuilder instructions = new StringBuilder();

        if (instruccionesBase != null && !instruccionesBase.trim().isEmpty()) {
            instructions.append(instruccionesBase.trim());
        }

        if (!SettingsService.isPremiumUser(usuarioId)) {
            if (instructions.length() > 0) {
                instructions.append("\n\n");
            }

            instructions.append(
                    "Limitacion de plan Gratuito: responde de forma breve y concreta. No superes aproximadamente 180 palabras y evita desarrollos extensos.");
        }

        return instructions.toString();
    }

    private static List<String> buildModelCandidates(int usuarioId) {
        JsonObject availableModels = SettingsService.getModeloDisponible(usuarioId);
        List<String> candidates = new ArrayList<>();

        if (!availableModels.has("status") || availableModels.get("status").getAsInt() != 200
                || !availableModels.has("modelos")) {
            return candidates;
        }

        JsonArray modelos = availableModels.getAsJsonArray("modelos");
        for (JsonElement item : modelos) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject modelo = item.getAsJsonObject();
            if (modelo.has("nombre")) {
                String name = modelo.get("nombre").getAsString().trim();
                if (!name.isBlank() && !candidates.contains(name)) {
                    candidates.add(name);
                }
            }
        }

        return candidates;
    }

}
