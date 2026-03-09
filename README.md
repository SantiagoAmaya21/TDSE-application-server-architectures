# Taller de Arquitecturas de Servidores de Aplicaciones

**Meta:** Prototipo mínimo que demuestra capacidades reflexivas de Java, carga de beans (POJOs) y derivación de una aplicación web a partir de ellos (IoC, reflexión).

## Descripción

Servidor web en Java tipo Apache que:

- Entrega páginas HTML e imágenes PNG (recursos estáticos).
- Provee un framework IoC para construir aplicaciones web a partir de POJOs.
- Atiende múltiples solicitudes **no concurrentes** (una por vez).
- Usa **reflexión** para descubrir componentes anotados (`@RestController`, `@GetMapping`, `@RequestParam`) y publicar servicios REST.

## Diseño y arquitectura

### Componentes principales

1. **Anotaciones**  
   - `@RestController`: marca una clase como componente REST; el framework la instancia y publica sus métodos anotados.  
   - `@GetMapping("/ruta")`: asocia un método que retorna `String` a una URI GET.  
   - `@RequestParam(value = "nombre", defaultValue = "valor")`: inyecta parámetros de consulta en el método.

2. **HttpServer**  
   Servidor HTTP mínimo (socket en el puerto 35000). Para cada GET:
   - Consulta primero al `RequestHandler` (rutas REST).
   - Si no hay ruta, sirve archivos estáticos desde `src/main/resources/static/` (HTML, PNG, etc.).

3. **ReflectionRequestHandler**  
   - Mantiene un mapa `ruta → (instancia, método)`.
   - Al registrar un controlador: instancia la clase por reflexión, recorre métodos con `@GetMapping`, obtiene la URI y guarda la invocación.
   - En `handle(path, queryParams)`: invoca el método pasando los parámetros según `@RequestParam` (reflexión sobre parámetros).

4. **ClassPathScanner**  
   Explora el classpath (directorio y JAR) buscando clases anotadas con `@RestController` en el paquete del framework, para no tener que listarlas en la línea de comandos.

5. **MicroSpringBoot**  
   Punto de entrada:
   - **Con argumentos:** carga solo las clases indicadas (ej. `co.edu.escuelaing.reflexionlab.controller.FirstWebService`).
   - **Sin argumentos:** usa `ClassPathScanner` para cargar todas las clases con `@RestController` bajo el paquete base.

Así se cumple la sugerencia: primera versión cargando POJOs por línea de comandos y versión final explorando el classpath.

### Flujo de una petición GET

1. `HttpServer` recibe la línea de petición y parsea path y query.
2. Llama a `RequestHandler.handle(path, queryParams)`.
3. Si devuelve un `String`, se envía como respuesta HTML.
4. Si devuelve `null`, se intenta servir un archivo estático (HTML/PNG).
5. Si no hay archivo, se responde 404.

## Requisitos

- Java 11+
- Maven 3.6+

## Instalación y uso

### Clonar y compilar

```bash
git clone <url-del-repositorio>
cd TDSE-application-server-architectures
mvn clean compile
```

### Ejecución

**Opción 1 – Carga por línea de comandos (primera versión)**  
Pasar las clases con `@RestController` como argumentos:

```bash
mvn exec:java -Dexec.mainClass="co.edu.escuelaing.reflexionlab.MicroSpringBoot" -Dexec.args="co.edu.escuelaing.reflexionlab.controller.FirstWebService"
```

O con `java` directamente:

```bash
java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.controller.FirstWebService
```

**Opción 2 – Escaneo del classpath (versión final)**  
Sin argumentos; se cargan todos los `@RestController` del paquete:

```bash
mvn compile exec:java -Dexec.mainClass="co.edu.escuelaing.reflexionlab.MicroSpringBoot"
```

El servidor queda en **http://localhost:35000**.

### Pruebas manuales

- `http://localhost:35000/` → mensaje del `HelloController`.
- `http://localhost:35000/greeting` → “Hola World” (default).
- `http://localhost:35000/greeting?name=Estudiante` → “Hola Estudiante”.
- `http://localhost:35000/hello` → mensaje de `FirstWebService` (si está cargado).
- `http://localhost:35000/index.html` → página estática.

## Tests automatizados

Se usan JUnit 5 para:

- **ReflectionRequestHandlerTest:** registro de controladores, invocación de `@GetMapping`, soporte de `@RequestParam` y valor por defecto, varios controladores.
- **ClassPathScannerTest:** comprobación de que el escáner encuentra las clases con `@RestController`.

Ejecución:

```bash
mvn test
```

## Estructura Maven

```
src/main/java/co/edu/escuelaing/reflexionlab/
  MicroSpringBoot.java                                     # Punto de entrada
  annotation/                                              # Anotaciones
    RestController.java, GetMapping.java, RequestParam.java
  server/                                                  # Servidor HTTP
    HttpServer.java, RequestHandler.java
  ioc/                                                     # IoC y reflexión
    ReflectionRequestHandler.java, ClassPathScanner.java
  controller/                                              # Controladores de ejemplo
    HelloController.java, GreetingController.java, FirstWebService.java
src/main/resources/static/
  index.html
src/test/java/co/edu/escuelaing/reflexionlab/
  ReflectionRequestHandlerTest.java, ClassPathScannerTest.java
pom.xml
```

## Despliegue en AWS

Para la evidencia de despliegue en AWS:

1. Empaquetar: `mvn package`.
2. Subir el JAR (o ejecutar en una instancia EC2 con Java 11) y ejecutar sin argumentos para cargar todos los controladores.
3. Abrir el puerto 35000 en el security group y acceder a `http://<IP-pública>:35000`.

## Referencias

- Taller: Meta protocolos de objetos, patrón IoC, reflexión.
- Ciclo de vida y código fuente gestionados con Maven; proyecto en GitHub; evidencia de ejecución en AWS según entregables.
