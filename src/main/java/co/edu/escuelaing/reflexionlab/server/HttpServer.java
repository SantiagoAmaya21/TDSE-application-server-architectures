package co.edu.escuelaing.reflexionlab.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal HTTP server. Serves static files (HTML, PNG) and delegates GET requests
 * to a request handler (REST routes). Handles one request at a time (non-concurrent).
 */
public class HttpServer {

    private static final int DEFAULT_PORT = 35000;
    private static final String STATIC_PREFIX = "static/";
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
    }

    private final int port;
    private final RequestHandler requestHandler;

    public HttpServer(int port, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on http://localhost:" + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleConnection(clientSocket);
                }
            }
        }
    }

    private void handleConnection(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null) return;

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            sendError(out, 400, "Bad Request");
            return;
        }

        String method = parts[0];
        String pathWithQuery = parts[1];
        String path = pathWithQuery.contains("?") ? pathWithQuery.substring(0, pathWithQuery.indexOf('?')) : pathWithQuery;
        Map<String, String> queryParams = new HashMap<>();
        if (pathWithQuery.contains("?")) {
            String qs = pathWithQuery.substring(pathWithQuery.indexOf('?') + 1).split(" ")[0];
            queryParams = parseQueryString(qs);
        }

        // Consume remaining headers until blank line
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) { }

        if (!"GET".equals(method)) {
            sendError(out, 405, "Method Not Allowed");
            return;
        }

        String response = requestHandler.handle(path, queryParams);
        if (response != null) {
            sendOk(out, response, "text/html");
            return;
        }

        // Static file from classpath, then filesystem
        String resourcePath = path.equals("/") ? "index.html" : path.replaceFirst("^/", "");
        String classpathResource = STATIC_PREFIX + resourcePath;
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (resourceStream != null) {
            byte[] body = resourceStream.readAllBytes();
            resourceStream.close();
            String ext = resourcePath.contains(".") ? resourcePath.substring(resourcePath.lastIndexOf('.') + 1) : "";
            String contentType = MIME_TYPES.getOrDefault(ext.toLowerCase(), "application/octet-stream");
            sendOk(out, body, contentType);
        } else {
            Path filePath = Path.of("src/main/resources/static", resourcePath);
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                byte[] body = Files.readAllBytes(filePath);
                String ext = filePath.getFileName().toString().contains(".")
                        ? filePath.getFileName().toString().substring(filePath.getFileName().toString().lastIndexOf('.') + 1)
                        : "";
                String contentType = MIME_TYPES.getOrDefault(ext.toLowerCase(), "application/octet-stream");
                sendOk(out, body, contentType);
            } else {
                sendError(out, 404, "Not Found");
            }
        }
    }

    private Map<String, String> parseQueryString(String qs) {
        Map<String, String> params = new HashMap<>();
        if (qs == null || qs.isEmpty()) return params;
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = decode(pair.substring(0, eq));
                String value = eq < pair.length() - 1 ? decode(pair.substring(eq + 1)) : "";
                params.put(key, value);
            }
        }
        return params;
    }

    private static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private void sendOk(OutputStream out, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        sendOk(out, bytes, contentType);
    }

    private void sendOk(OutputStream out, byte[] body, String contentType) throws IOException {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private void sendError(OutputStream out, int code, String message) throws IOException {
        String body = "<html><body><h1>" + code + " " + message + "</h1></body></html>";
        String header = "HTTP/1.1 " + code + " " + message + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static int getDefaultPort() {
        return DEFAULT_PORT;
    }
}
