package co.edu.escuelaing.reflexionlab.ioc;

import java.lang.annotation.Annotation;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Explores the classpath (or a given package) to find classes annotated with a given annotation.
 * Used to discover @RestController components without specifying them on the command line.
 */
public final class ClassPathScanner {

    private ClassPathScanner() { }

    /**
     * Finds all classes under the given base package (and sub-packages) that are
     * annotated with the given annotation.
     */
    public static <A extends Annotation> Set<Class<?>> findAnnotatedClasses(Class<A> annotationClass, String basePackage) {
        Set<Class<?>> result = new HashSet<>();
        String packagePath = basePackage.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            java.util.Enumeration<URL> resources = cl.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File dir = new File(url.toURI());
                    scanDirectory(dir, basePackage, result, c -> c.isAnnotationPresent(annotationClass));
                } else if ("jar".equals(url.getProtocol())) {
                    scanJar(url, packagePath, basePackage, result, c -> c.isAnnotationPresent(annotationClass));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error scanning classpath for @" + annotationClass.getSimpleName(), e);
        }
        return result;
    }

    private static void scanDirectory(File dir, String packageName, Set<Class<?>> result, Predicate<Class<?>> filter) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f, packageName + "." + f.getName(), result, filter);
            } else if (f.getName().endsWith(".class")) {
                String className = packageName + "." + f.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (filter.test(clazz)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    // skip
                }
            }
        }
    }

    private static void scanJar(URL url, String packagePath, String packageName, Set<Class<?>> result, Predicate<Class<?>> filter) {
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            try (JarFile jar = conn.getJarFile()) {
                String prefix = packagePath.endsWith("/") ? packagePath : packagePath + "/";
                java.util.Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.endsWith(".class") || !name.startsWith(prefix)) continue;
                    String className = name.replace("/", ".").replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (filter.test(clazz)) result.add(clazz);
                    } catch (ClassNotFoundException | NoClassDefFoundError ignored) { }
                }
            }
        } catch (Exception ignored) { }
    }
}
