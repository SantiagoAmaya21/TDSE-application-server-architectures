package co.edu.escuelaing.reflexionlab.ioc;

import co.edu.escuelaing.reflexionlab.annotation.GetMapping;
import co.edu.escuelaing.reflexionlab.annotation.RequestParam;
import co.edu.escuelaing.reflexionlab.annotation.RestController;
import co.edu.escuelaing.reflexionlab.server.RequestHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Request handler that uses reflection to invoke @GetMapping methods on @RestController beans.
 * Supports @RequestParam for query parameters with optional defaultValue.
 */
public class ReflectionRequestHandler implements RequestHandler {

    private final Map<String, MethodInvocation> routeToMethod = new HashMap<>();

    public void registerController(Class<?> controllerClass) throws ReflectiveOperationException {
        if (!controllerClass.isAnnotationPresent(RestController.class)) {
            return;
        }
        Object instance = controllerClass.getDeclaredConstructor().newInstance();
        for (Method method : controllerClass.getDeclaredMethods()) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping == null) continue;
            if (method.getReturnType() != String.class) continue;
            String path = normalizePath(getMapping.value());
            routeToMethod.put(path, new MethodInvocation(instance, method));
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) return "/" + path;
        return path;
    }

    @Override
    public String handle(String path, Map<String, String> queryParams) {
        path = normalizePath(path);
        MethodInvocation inv = routeToMethod.get(path);
        if (inv == null) return null;
        try {
            return inv.invoke(queryParams);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error invoking " + inv.method, e);
        }
    }

    private static class MethodInvocation {
        final Object instance;
        final Method method;

        MethodInvocation(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
            method.setAccessible(true);
        }

        String invoke(Map<String, String> queryParams) throws ReflectiveOperationException {
            Parameter[] params = method.getParameters();
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                RequestParam rp = params[i].getAnnotation(RequestParam.class);
                if (rp != null) {
                    String value = queryParams.get(rp.value());
                    if (value == null) value = rp.defaultValue();
                    args[i] = value;
                }
            }
            return (String) method.invoke(instance, args);
        }
    }
}
