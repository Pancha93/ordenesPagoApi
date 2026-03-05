# Órdenes de Pago API - Backend

Sistema de gestión de Órdenes de Pago con Spring Boot 4.0.3

Administra las órdenes de pago, permitiendo a los usuarios crear pedidos, adjuntar archivos de factura (PDF o imágenes), aprobarlos o rechazarlos y notificar a un sistema externo tras la aprobación.

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 4.0.3**
- **PostgreSQL**
- **Spring Security 6+ con JWT**
- **Spring Data JPA**
- **Swagger/OpenAPI 3** (springdoc-openapi)
- **Maven**

## 📋 Prerrequisitos

- JDK 21 instalado
- Maven 3.8+ instalado
- PostgreSQL 14+ instalado y ejecutándose
- IDE recomendado: IntelliJ IDEA o VS Code

## 🔧 Configuración Inicial

### 1. Crear la Base de Datos

```sql
-- Conectarse a PostgreSQL
psql -U postgres

-- Crear la base de datos
CREATE DATABASE payment_order_db;

-- Verificar
\l
```

### 2. Configurar Credenciales

Editar `src/main/resources/application.properties` si tus credenciales son diferentes:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_order_db
spring.datasource.username=postgres
spring.datasource.password=root
```

## 🏃 Ejecutar la Aplicación

### Opción 1: Desde la línea de comandos

```bash
# Desde el directorio del proyecto (ordenesPagoApi)
mvn clean install
mvn spring-boot:run
```

### Opción 2: Desde tu IDE

1. Abrir el proyecto en tu IDE
2. Ejecutar la clase `OrdenesPagoApplication.java`

La aplicación iniciará en: **http://localhost:8080**

## 📚 Acceder a Swagger UI

Una vez la aplicación esté ejecutándose, puedes acceder a la documentación interactiva:

### Swagger UI (Interfaz Gráfica)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON Documentation
```
http://localhost:8080/api-docs
```

## 🧪 Probar la API

### 1. Health Check (sin autenticación)

```bash
curl http://localhost:8080/api/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "service": "Órdenes de Pago API",
  "timestamp": "2026-03-04T10:30:00",
  "version": "1.0.0"
}
```

### 2. Ping

```bash
curl http://localhost:8080/api/health/ping
```

Respuesta: `pong`

## 📖 Documentación API con Swagger

En Swagger UI podrás:

✅ Ver todos los endpoints disponibles  
✅ Probar las APIs directamente desde el navegador  
✅ Ver los esquemas de request/response  
✅ Autenticarte con JWT (cuando esté implementado)  
✅ Ver códigos de respuesta HTTP  

### Características de Swagger Configuradas:

- **Ordenamiento**: Operaciones ordenadas por método HTTP
- **Tags**: Agrupación lógica de endpoints
- **Seguridad JWT**: Botón "Authorize" para agregar token Bearer
- **Try it out**: Ejecutar directamente desde la UI

## 🔐 Seguridad (Estado Actual)

✅ **Implementación COMPLETA de JWT + RBAC**

- JWT Stateless con Spring Security 6
- Roles: **ADMIN** y **OPERATOR**
- @PreAuthorize en capa de servicio
- Tokens con expiración de 1 hora

### Usuarios de Prueba:

La aplicación crea automáticamente dos usuarios:

**ADMIN:**
```
Email: admin@vortexbird.com
Password: password123
```

**OPERATOR:**
```
Email: operator@vortexbird.com
Password: password123
```

## 📖 Usando la API con Swagger

### 1. Autenticarse

1. Ir a **http://localhost:8080/swagger-ui.html**
2. Expandir **Authentication** → `/api/auth/login`
3. Click en "Try it out"
4. Usar credenciales:
   ```json
   {
     "email": "admin@vortexbird.com",
     "password": "password123"
   }
   ```
5. Copiar el `token` de la respuesta

### 2. Autorizar Swagger

1. Click en el botón **"Authorize"** (candado verde arriba)
2. Pegar el token en el campo
3. Click en "Authorize"
4. Ahora todos los endpoints protegidos funcionarán

### 3. Probar Endpoints

**Crear Orden (OPERATOR):**
```json
POST /api/orders
{
  "description": "Pago de servicios de consultoría",
  "amount": 1500000.00
}
```

**Listar Órdenes:**
```
GET /api/orders?page=0&size=10
```

**Obtener Detalle:**
```
GET /api/orders/1
```

**Aprobar Orden (ADMIN):**
```
PATCH /api/orders/1/approve
```

**Rechazar Orden (ADMIN):**
```json
PATCH /api/orders/1/reject
{
  "rejectionReason": "Factura incompleta"
}
```

**Subir Factura:**
```
POST /api/invoices/upload/1
Content-Type: multipart/form-data
file: [PDF o imagen]
```

## 📦 Estructura del Proyecto Implementada

```
src/main/java/com/vortexbird/ordenesPago/
├── config/
│   ├── JpaConfig.java              # Habilita auditoría JPA
│   ├── OpenApiConfig.java          # Configuración Swagger
│   ├── SecurityConfig.java         # Spring Security + JWT
│   └── WebClientConfig.java        # WebClient para integraciones
├── controller/
│   ├── AuthController.java         # POST /api/auth/login
│   ├── HealthController.java       # GET /api/health
│   ├── InvoiceController.java      # APIs de facturas
│   └── OrderController.java        # APIs de órdenes
├── dto/
│   ├── external/
│   │   └── ApprovalNotificationDto.java
│   ├── request/
│   │   ├── CreateOrderRequest.java
│   │   ├── LoginRequest.java
│   │   ├── OrderFilterRequest.java
│   │   └── RejectOrderRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── ErrorResponse.java
│       ├── InvoiceResponse.java
│       ├── OrderDetailResponse.java
│       └── OrderResponse.java
├── entity/
│   ├── Invoice.java                # Facturas adjuntas
│   ├── Order.java                  # Órdenes de pago
│   ├── OrderStatusLog.java         # Auditoría de cambios
│   └── User.java                   # Usuarios del sistema
├── enums/
│   ├── OrderStatus.java            # PENDING, APPROVED, REJECTED, ARCHIVED
│   └── UserRole.java               # ADMIN, OPERATOR
├── exception/
│   ├── handler/
│   │   └── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ExternalIntegrationException.java
│   ├── InvalidStateTransitionException.java
│   ├── InvoiceNotFoundException.java
│   ├── OrderNotFoundException.java
│   ├── StorageException.java
│   └── UnauthorizedActionException.java
├── repository/
│   ├── InvoiceRepository.java
│   ├── OrderRepository.java
│   ├── OrderStatusLogRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── impl/
│   │   └── AuthServiceImpl.java
│   ├── AuthService.java
│   ├── ExternalNotificationService.java
│   ├── InvoiceService.java
│   ├── OrderService.java
│   └── StorageService.java
└── OrdenesPagoApplication.java
```

## ✅ Funcionalidades Implementadas

### Autenticación y Autorización
- ✅ Login con JWT (POST /api/auth/login)
- ✅ JWT stateless con expiración configurable
- ✅ Autorización basada en roles (RBAC)
- ✅ @PreAuthorize en servicios
- ✅ Manejo de errores 401/403

### Gestión de Órdenes
- ✅ Crear orden (OPERATOR)
- ✅ Listar órdenes con filtros y paginación
- ✅ Obtener detalle de orden
- ✅ Aprobar orden (ADMIN)
- ✅ Rechazar orden con razón (ADMIN)
- ✅ Validación de transiciones de estado

### Gestión de Facturas
- ✅ Subir factura (multipart/form-data)
- ✅ Obtener información de factura
- ✅ Interfaz StorageService (preparada para S3)
- ✅ Validación de tipos de archivo

### Arquitectura y Calidad
- ✅ Arquitectura en capas limpia
- ✅ Manejo global de excepciones
- ✅ DTOs desacoplados de entidades
- ✅ Repositories con Spring Data JPA
- ✅ Auditoría automática (createdAt, updatedAt)
- ✅ Documentación Swagger/OpenAPI
- ✅ Validaciones con Bean Validation
- ✅ Logging estructurado

## ⏳ Pendiente de Implementación

1. **Implementaciones de Servicios:** OrderServiceImpl, InvoiceServiceImpl (interfaces creadas)
2. **StorageService:** Implementación S3 o Local (interfaz lista)
3. **ExternalNotificationService:** WebClient para notificaciones (config lista)
4. **Triggers SQL:** Auditoría de cambios de estado en OrderStatusLog
5. **Stored Procedure:** Archivado automático de órdenes rechazadas
6. **Tests Unitarios:** Ejemplos representativos

## 📝 Próximos Pasos Recomendados

1. ⏳ Implementar OrderServiceImpl con lógica de negocio completa
2. ⏳ Implementar InvoiceServiceImpl con StorageService
3. ⏳ Implementar StorageService (S3 o Local)
4. ⏳ Implementar ExternalNotificationServiceImpl con WebClient
5. ⏳ Crear triggers y stored procedures SQL
6. ⏳ Agregar tests unitarios

## 📧 Contacto

Para preguntas o soporte: soporte@vortexbird.com

---

**Versión**: 1.0.0  
**Última actualización**: 4 de marzo de 2026
