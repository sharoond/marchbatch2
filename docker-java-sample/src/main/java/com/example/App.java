package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class App {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RootHandler());
        server.createContext("/users", new UsersHandler());
        server.setExecutor(null);
        server.start();

        System.out.printf("Server started: http://localhost:%d/%n", port);
        System.out.println("Available endpoints:");
        System.out.println("  GET  /                - Health check");
        System.out.println("  GET  /users           - Get all users");
        System.out.println("  GET  /users?id=<id>   - Get user by ID");
        System.out.println("  POST /users           - Create new user (JSON: {\"name\": \"...\", \"email\": \"...\"})");
        System.out.println("  PUT  /users?id=<id>   - Update user (JSON: {\"name\": \"...\", \"email\": \"...\"})");
        System.out.println("  DELETE /users?id=<id> - Delete user");
    }

    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Docker Java Sample - PostgreSQL Integration\n" +
                    "Connected to PostgreSQL at localhost:5433\n" +
                    "Use /users endpoint for database operations\n";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            try {
                if ("GET".equals(method)) {
                    handleGetRequest(exchange, query);
                } else if ("POST".equals(method)) {
                    handlePostRequest(exchange);
                } else if ("PUT".equals(method)) {
                    handlePutRequest(exchange, query);
                } else if ("DELETE".equals(method)) {
                    handleDeleteRequest(exchange, query);
                } else {
                    sendErrorResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }

        private void handleGetRequest(HttpExchange exchange, String query) throws IOException, SQLException {
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.split("=")[1]);
                User user = DatabaseConnection.getUserById(id);
                if (user != null) {
                    sendJsonResponse(exchange, 200, gson.toJson(user));
                } else {
                    sendErrorResponse(exchange, 404, "User not found");
                }
            } else {
                List<User> users = DatabaseConnection.getAllUsers();
                sendJsonResponse(exchange, 200, gson.toJson(users));
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException, SQLException {
            String body = readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            
            User newUser = DatabaseConnection.createUser(name, email);
            sendJsonResponse(exchange, 201, gson.toJson(newUser));
        }

        private void handlePutRequest(HttpExchange exchange, String query) throws IOException, SQLException {
            if (query == null || !query.startsWith("id=")) {
                sendErrorResponse(exchange, 400, "ID parameter required");
                return;
            }
            
            int id = Integer.parseInt(query.split("=")[1]);
            String body = readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            
            boolean updated = DatabaseConnection.updateUser(id, name, email);
            if (updated) {
                User updatedUser = DatabaseConnection.getUserById(id);
                sendJsonResponse(exchange, 200, gson.toJson(updatedUser));
            } else {
                sendErrorResponse(exchange, 404, "User not found");
            }
        }

        private void handleDeleteRequest(HttpExchange exchange, String query) throws IOException, SQLException {
            if (query == null || !query.startsWith("id=")) {
                sendErrorResponse(exchange, 400, "ID parameter required");
                return;
            }
            
            int id = Integer.parseInt(query.split("=")[1]);
            boolean deleted = DatabaseConnection.deleteUser(id);
            
            if (deleted) {
                sendJsonResponse(exchange, 200, "{\"message\": \"User deleted successfully\"}");
            } else {
                sendErrorResponse(exchange, 404, "User not found");
            }
        }

        private String readRequestBody(HttpExchange exchange) throws IOException {
            try (InputStream is = exchange.getRequestBody()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            JsonObject error = new JsonObject();
            error.addProperty("error", message);
            String response = gson.toJson(error);
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}

