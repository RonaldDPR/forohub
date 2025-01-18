# ForoHub

**ForoHub** es una API REST construida con **Spring Boot**, **Spring Security (JWT)**, **Flyway** y **MySQL**, cuyo propósito es brindar un sistema de foros en línea, permitiendo la creación, edición, detalle y eliminación lógica de Tópicos. Asimismo, soporta autenticación de usuarios mediante **tokens JWT**.

---

## Tabla de Contenidos

1. [Características Principales](#características-principales)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Requisitos](#requisitos)
4. [Configuración Inicial](#configuración-inicial)
5. [Migraciones de Base de Datos (Flyway)](#migraciones-de-base-de-datos-flyway)
6. [Ejecución de la Aplicación](#ejecución-de-la-aplicación)
7. [Seguridad y Autenticación (JWT)](#seguridad-y-autenticación-jwt)
8. [Entidades Principales](#entidades-principales)
9. [Endpoints Principales](#endpoints-principales)
10. [Eliminación Lógica de Tópicos](#eliminación-lógica-de-tópicos)
11. [Validaciones y Manejador Global de Errores](#validaciones-y-manejador-global-de-errores)
12. [Ejemplos de Peticiones (Insomnia/Postman)](#ejemplos-de-peticiones-insomniapostman)
13. [Contribución](#contribución)
14. [Licencia](#licencia)

---

## 1. Características Principales

- **CRUD de Tópicos** (crear, leer, actualizar, eliminar).
- **Eliminación Lógica** (no se borra físicamente el registro).
- **Autenticación de Usuarios** mediante tokens **JWT**.
- **Migraciones con Flyway** para mantener la estructura de la base de datos.
- **Validaciones** de campos con `@Valid` y **manejo global de errores**.
- **Spring Data JPA** como capa de persistencia, conectado a **MySQL**.

---

## 2. Arquitectura del Proyecto

El proyecto se organiza en paquetes principales:

```plaintext
com.foro.forohub
 ├── domain
 │    ├── topico
 │    │    ├── Topico.java
 │    │    ├── TopicoRepository.java
 │    │    ├── DTOs (clases/records)
 │    ├── usuario
 │    │    ├── Usuario.java
 │    │    ├── UsuarioRepository.java
 │    │    ├── DTOs (clases/records)
 ├── infra
 │    ├── security
 │    │    ├── SecurityConfigurations.java
 │    │    ├── SecurityFilter.java
 │    │    ├── TokenService.java
 │    │    ├── UserSpring.java
 │    │    └── AutenticacionService.java
 │    └── errores
 │         └── TratadorDeErrores.java   // Global error handler
 ├── controller
 │    ├── TopicoController.java
 │    └── AutenticacionController.java
 ├── ForoHubApplication.java

```

Los paquetes están divididos por dominio (topico, usuario) y por infraestructura (security, errores). Dentro de controller se ubican los controladores REST. Flyway utiliza src/main/resources/db/migration para los scripts de creación de tablas.


## 3. Requisitos
•	Java 17 o superior.
•	MySQL (o cualquier motor compatible con JDBC si adaptas la configuración).
•	Maven (para compilar y gestionar dependencias).
Opcionalmente:
•	IntelliJ IDEA o Spring Tools Suite para desarrollar con mayor comodidad.
•	Insomnia / Postman para probar los endpoints.
 
## 4. Configuración Inicial
### 1.	Clona el repositorio en tu máquina local:
```bash
git clone https://github.com/tu-usuario/foro-hub.git
```
### 2.	Crea la base de datos (por ejemplo, foro_hub) en tu servidor MySQL:

```sql
CREATE DATABASE foro_hub;
USE foro_hub;
```
### 3.	Ajusta en el archivo src/main/resources/application.properties tus credenciales y URL de MySQL. En lugar de poner valores fijos, puedes referirte a variables de entorno, por ejemplo:
```bash
spring.application.name=foro_hub

spring.datasource.url=${DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USERNAME}
spring.datasource.password=${DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update

# Logs SQL en consola (opcional)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Configuración Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Secret para JWT
api.security.secret=${JWT_SECRET}
```

### ¿Cómo usarlo?
1.	Define en tu sistema (o en tu hosting, contenedor Docker, etc.) las variables de entorno:
o	DATASOURCE_URL: URL de conexión a tu base de datos, ej. jdbc:mysql://localhost:3306/foro_hub.
o	DATASOURCE_USERNAME: Usuario para conectarse a la base de datos.
o	DATASOURCE_PASSWORD: Contraseña correspondiente.
o	JWT_SECRET: Cadena secreta para firmar el token JWT.
2.	Inicia la aplicación. Spring Boot sustituirá automáticamente ${...} con el valor de la variable de entorno que corresponda.
De esta manera, no expones tus credenciales ni tu clave JWT en el repositorio, y puedes cambiar configuraciones entre entornos (local, producción, staging) sin modificar el archivo application.properties.
 
## 5. Migraciones de Base de Datos (Flyway)
El proyecto incluye archivos de migración en src/main/resources/db/migration:
```plaintext
•	V1__create_table_usuarios.sql
•	V2__create_table_topicos.sql
```
Flyway ejecuta estos scripts al arrancar la aplicación. Así se crean las tablas:
•	usuarios: Para almacenar datos de usuario (nombre, correo, contraseña hasheada, activo).
•	topicos: Para los tópicos (título, mensaje, fecha, autor, activo).
 
## 6. Ejecución de la Aplicación
Desde la raíz del proyecto:
```bash
mvn spring-boot:run
```
o dentro de tu IDE, ejecuta la clase principal ForoHubApplication.
Si la conexión a la base de datos es correcta y Flyway migra todo sin problemas, verás la aplicación corriendo en:
```arduino
http://localhost:8080
``` 
## 7. Seguridad y Autenticación (JWT)
•	Se usa Spring Security en modo stateless (no se guardan sesiones).
•	El endpount /auth es público (no requiere token).
•	Para el resto de endpoints, debes enviar el header Authorization: Bearer <TU_TOKEN_JWT>.
•	El token JWT se genera con TokenService.java, firmándose con la clave configurada en application.properties (api.security.secret).
Flujo de Autenticación
1.	Login: Envías email y contrasena vía POST /auth.
2.	Spring verifica tus credenciales con AutenticacionService.
3.	Si son correctas, devuelve un token JWT (tipo Bearer).
4.	Para consumir cualquier otro endpoint (por ejemplo GET /topicos), incluyes el header Authorization: Bearer <token>.
 
## 8. Entidades Principales
### 1.	Usuario
o	id, nombre, correoElectronico, contrasena, activo.
o	Se recomienda almacenar la contraseña en BCrypt.
### 2.	Topico
o	id, titulo, mensaje, fechaCreacion, status, id_autor, activo.
o	Enlazado a un Usuario autor.
o	activo=false indica que está eliminado (lógicamente).
 
## 9. Endpoints Principales
A continuación, un resumen de los endpoints más relevantes.
### 1.	Autenticación
#### -	POST /auth
#### -	Body:
```json
{
  "email": "admin@forohub.com",
  "contrasena": "123456"
}
```
#### -	Respuesta:
```json
{
  "token": "<JWT_GENERADO>",
  "type": "Bearer"
}
```
### 2.	Tópicos
#### -	GET /topicos
.	Lista todos los tópicos activos.
#### -	POST /topicos
.	Registra un tópico nuevo, usando:
```json
{
  "titulo": "Título",
  "mensaje": "Mensaje",
  "idAutor": 1
}
```
#### -	GET /topicos/{id}
.	Devuelve el detalle de un tópico activo.

.	Retorna 404 si no existe, 410 si está inactivo.

#### -	PUT /topicos/{id}
.	Actualiza el título y el mensaje de un tópico activo.

.	Retorna 410 si el tópico fue eliminado lógicamente.
#### -	DELETE /topicos/{id}
.	Marca el activo=false (eliminación lógica).

.	Retorna 410 si ya estaba inactivo.

Todas las operaciones (excepto /auth) requieren el header Authorization: Bearer <TOKEN>.
 
## 10. Eliminación Lógica de Tópicos
Cuando se ejecuta:
```http
DELETE /topicos/{id}
```
•	El campo activo se setea a false.

•	El registro no se borra físicamente de la BD.

•	No se puede:

-	Volver a eliminar el mismo tópico.
  
-	Consultar su detalle.
  
-	Actualizarlo.
  
•	Se retorna un código 410 (Gone) indicando que “El tópico ya fue eliminado” si intentas accederlo después.
 
## 11. Validaciones y Manejador Global de Errores
•	Las clases DTO usan anotaciones como @NotBlank, @NotNull, etc.

•	Si se incumple una validación, salta MethodArgumentNotValidException.

•	Existe un @RestControllerAdvice (clase TratadorDeErrores) que maneja las excepciones globalmente.

-	Para MethodArgumentNotValidException, se retorna un 400 con los detalles de campos y mensajes.
-	Para RuntimeException, se retorna un 400 con el mensaje de la excepción (cuando no uses ResponseStatusException).

Al usar ResponseStatusException (e.g., throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...)) se respeta el código HTTP que indiques (404, 410, etc.).
 
## 12. Ejemplos de Peticiones (Insomnia/Postman)
### 1. Obtener Token (Login)
•	URL: POST http://localhost:8080/auth

•	Body:
```json
{
  "email": "admin@forohub.com",
  "contrasena": "123456"
}
```
•	Respuesta:
```json
{
  "token": "<JWT_GENERADO>",
  "type": "Bearer"
}
```

### 2. Registrar Tópico
•	URL: POST http://localhost:8080/topicos

•	Headers:

-	Authorization: Bearer <JWT_GENERADO>

•	Body:
```json
{
  "titulo": "Duda con Java",
  "mensaje": "¿Cómo funciona JPA?",
  "idAutor": 1
}
```
•	Respuesta: 201 Created (o 200 OK según tu implementación) con la información del nuevo tópico.

### 3. Listar Tópicos
•	URL: GET http://localhost:8080/topicos

•	Headers:

-	Authorization: Bearer <JWT_GENERADO>

•	Respuesta:
```json
[
  {
    "id": 1,
    "titulo": "Duda con Java",
    "mensaje": "¿Cómo funciona JPA?",
    "fechaCreacion": "2025-01-18T10:00:00",
    "status": "ABIERTO",
    "idAutor": 1
  }
  ...
]
```
### 4. Detalle de un Tópico

•	URL: GET http://localhost:8080/topicos/1

•	Headers:

-	Authorization: Bearer <TOKEN>

•	Respuesta:
```json
{
  "id": 1,
  "titulo": "Duda con Java",
  "mensaje": "¿Cómo funciona JPA?",
  "fechaCreacion": "2025-01-18T10:00:00",
  "status": "ABIERTO",
  "idAutor": 1
}
```

•	Si el ID no existe → 404 Not Found.

•	Si el tópico está inactivo → 410 Gone.

### 5. Actualizar un Tópico
•	URL: PUT http://localhost:8080/topicos/1

•	Body:
```json
{
  "titulo": "Duda con Java (editada)",
  "mensaje": "He comprendido las entidades en JPA",
  "idAutor": 1
}
```
•	Respuesta: 200 OK con el tópico actualizado.

### 6. Eliminar un Tópico (lógico)
•	URL: DELETE http://localhost:8080/topicos/1

•	Respuesta: 200 OK con un mensaje {"mensaje":"Tópico eliminado correctamente"} si se desactiva por primera vez.

•	Si ya estaba inactivo → 410 Gone (y un mensaje distinto).
 
## 13. Contribución
¡Las contribuciones son bienvenidas! Puedes hacer un fork del repositorio, crear tu rama con los cambios y abrir un Pull Request. Asegúrate de seguir una convención de commits y añadir pruebas (tests) si es posible.
 
## 14. Licencia
Este proyecto se distribuye bajo una licencia de tu preferencia (MIT, Apache, etc.). Asegúrate de incluir el archivo LICENSE en la raíz del repositorio con los términos correspondientes.
 
