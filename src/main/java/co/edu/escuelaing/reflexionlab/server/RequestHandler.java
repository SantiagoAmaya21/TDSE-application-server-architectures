package co.edu.escuelaing.reflexionlab.server;

import java.util.Map;

/**
 * Handles HTTP GET requests for REST routes. Returns the response body as String
 * or null if the path is not a registered route (so the server can try static files).
 */
public interface RequestHandler {
    /**
     * @param path        request path (e.g. "/greeting")
     * @param queryParams parsed query parameters
     * @return response body string, or null if not handled
     */
    String handle(String path, Map<String, String> queryParams);
}
