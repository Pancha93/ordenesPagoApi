# ARQUITECTURA - Órdenes de Pago API

## Visión General

Sistema backend REST construido con **Spring Boot 4.0.3** y **Java 21**, diseñado siguiendo principios de arquitectura limpia, separación de responsabilidades y mejores prácticas de Spring Framework.

---

## Decisiones Arquitectónicas Clave

### 1. Arquitectura en Capas

**Estructura adoptada:**
```
Controller → Service → Repository → Entity
     ↓          ↓
    DTO    Business Logic
```

**Razones:**
- ✅ Separación clara de responsabilidades
- ✅ Testabilidad: Cada capa se puede testear independientemente
- ✅ Mantenibilidad: Cambios en una capa no afectan otras
- ✅ Reutilización: Servicios pueden ser llamados desde múltiples controllers

**Implementación:**
- **Controllers**: Solo reciben requests, validan entrada, delegan a servicios
- **Services**: Contienen lógica de negocio, orquestan operaciones, manejan transacciones
- **Repositories**: Acceso a datos usando Spring Data JPA
- **Entities**: Modelo de dominio con JPA

---

### 2. Seguridad: JWT + RBAC

**Enfoque:** Autenticación stateless con tokens JWT + Autorización basada en roles

**Componentes:**
1. **JwtTokenProvider**: Genera y valida tokens (JJWT 0.12+)
2. **JwtAuthenticationFilter**: Filtro que intercepta requests y valida tokens
3. **UserDetailsServiceImpl**: Carga usuarios desde BD para Spring Security
4. **SecurityConfig**: Configuración de Spring Security 6

**Flujo de Autenticación:**
```
1. POST /api/auth/login → {email, password}
2. AuthService valida con AuthenticationManager (BCrypt)
3. JwtTokenProvider genera token firmado (HS512)
4. Cliente recibe: {token, role, expiresIn}
5. Requests subsecuentes: Authorization: Bearer <token>
```

**Autorización en Profundidad:**
- **Nivel 1 (URL)**: SecurityFilterChain protege endpoints
- **Nivel 2 (Método)**: @PreAuthorize en servicios valida roles
- **Nivel 3 (Dato)**: Servicios verifican ownership (OPERATOR solo ve sus órdenes)

**¿Por qué @PreAuthorize en servicios y not en controllers?**
- Seguridad real, no solo documentativa
- Si el servicio es llamado desde múltiples lugares, la seguridad se mantiene
- Facilita testing de autorización

---

### 3. Manejo de Excepciones

**Estrategia:** Jerarquía de excepciones + Handler global

**Jerarquía:**
```
BusinessException (base)
├── OrderNotFoundException (404)
├── InvalidStateTransitionException (400)
├── UnauthorizedActionException (403)
├── StorageException (500)
├── ExternalIntegrationException (502)
└── InvoiceNotFoundException (404)
```

**GlobalExceptionHandler** (@RestControllerAdvice):
- Captura todas las excepciones
- Retorna ErrorResponse consistente
- Logging diferenciado: WARN (negocio), ERROR (técnico)
- Códigos HTTP apropiados

**Ventajas:**
- Respuestas consistentes para frontend
- No repetir try-catch en controllers
- Mensajes claros, no exponer stack traces en producción

---

### 4. DTOs vs Entidades

**Decisión:** NUNCA exponer entidades JPA en APIs

**Razones:**
- Evitar lazy loading exceptions (N+1 queries)
- Control total sobre qué se expone al frontend
- Evitar modificaciones accidentales
- Versionamiento de API independiente del modelo de datos

**Tipos de DTOs:**
- **Request**: Entrada de endpoints (validaciones con Bean Validation)
- **Response**: Salida de endpoints (información controlada)
- **External**: Payloads para integraciones externas

---

### 5. Transaccionalidad

**Uso de @Transactional:**
- En capa de **servicio**, NO en repositories ni controllers
- `readOnly = true` para consultas (optimización)
- Transacciones por defecto en operaciones de escritura

**Caso especial - Integración Externa:**
```java
@Transactional
public OrderResponse approveOrder(Long id, String userEmail) {
    // 1. Actualizar orden (transaccional)
    order.setStatus(APPROVED);
    orderRepository.save(order);
    
    // 2. Notificar externo (NO transaccional - fire and forget)
    externalService.notifyOrderApproved(order); // No debe hacer rollback si falla
    
    return response;
}
```

**Razón:** Si falla la notificación externa, la orden YA está aprobada en BD. Eventual consistency.

---

### 6. Patrón Repository + Specification

**Spring Data JPA:**
- Repositories extienden `JpaRepository` y `JpaSpecificationExecutor`
- Queries derivadas: `findByStatus`, `findByCreatedBy`
- @Query para queries complejas
- Specification para filtros dinámicos

**Ventajas:**
- No escribir SQL manualmente
- Type-safe queries
- Paginación y sorting out-of-the-box

---

### 7. Auditoría Automática

**JPA Auditing:**
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Trigger SQL (OrderStatusLog):**
- Cada UPDATE en `orders` dispara trigger
- Inserta registro en `order_status_log`
- Historial inmutable para auditoría

---

### 8. Abstracción de Almacenamiento

**StorageService Interface:**
```java
public interface StorageService {
    String store(MultipartFile file, String folder);
    String generateDownloadUrl(String storageKey, int expirationMinutes);
    void delete(String storageKey);
    boolean exists(String storageKey);
}
```

**Implementaciones:**
- **S3StorageServiceImpl**: AWS S3 (producción)
- **LocalStorageServiceImpl**: Sistema de archivos local (desarrollo)

**Ventajas:**
- Cambiar proveedor sin tocar lógica de negocio
- Testing fácil con mock
- Presigned URLs para descargas seguras

---

### 9. Integración Externa con WebClient

**WebClient (Spring WebFlux):**
- Reactivo y non-blocking
- Manejo de timeouts a nivel de HttpClient
- Retry configurable con @Retryable
- Fire-and-forget para notificaciones

**Configuración:**
```java
WebClient.builder()
    .baseUrl(baseUrl)
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofMillis(5000))
    ))
    .build();
```

---

### 10. Validación de Datos

**Bean Validation (Jakarta):**
```java
public class CreateOrderRequest {
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 500)
    private String description;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
```

**Manejo:**
- @Valid en controllers
- MethodArgumentNotValidException capturada por GlobalExceptionHandler
- ErrorResponse con fieldErrors para frontend

---

## Patrones de Diseño Aplicados

### 1. **Dependency Injection (Constructor)**
```java
@Service
@RequiredArgsConstructor // Lombok genera constructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final UserRepository userRepository;
}
```
**Ventajas:** Inmutabilidad, facilita testing

### 2. **Strategy Pattern (StorageService)**
Múltiples implementaciones de una interfaz, seleccionables vía configuración.

### 3. **DTO Pattern**
Objetos de transferencia separados del modelo de dominio.

### 4. **Builder Pattern**
Usado en DTOs y respuestas con Lombok @Builder.

### 5. **Repository Pattern**
Abstracción de acceso a datos con Spring Data JPA.

---

## Mejores Prácticas Aplicadas

### Código Limpio
- ✅ Métodos pequeños con responsabilidad única
- ✅ Nombres descriptivos (no abbreviations)
- ✅ Comentarios Javadoc en clases principales
- ✅ No magic numbers (usar constantes)

### Spring Boot 4 Modernizado
- ✅ Sin APIs deprecated
- ✅ Configuración funcional (lambdas en SecurityConfig)
- ✅ Records para DTOs inmutables (opcional)
- ✅ Spring Security 6 sin WebSecurityConfigurerAdapter

### Seguridad
- ✅ Contraseñas con BCrypt
- ✅ JWT con clave secreta fuerte (512 bits)
- ✅ CSRF deshabilitado (API stateless)
- ✅ Roles con prefijo ROLE_ (Spring Security convention)

### Performance
- ✅ Lazy loading en relaciones JPA
- ✅ @Transactional(readOnly=true) en consultas
- ✅ Paginación en listados
- ✅ Índices en columnas de búsqueda frecuente

---

## Testing Strategy (Pendiente)

### Tests Unitarios
- Services con mocks de repositories
- JwtTokenProvider con tokens válidos/inválidos
- Mappers DTO ↔ Entity

### Tests de Integración
- Controllers con MockMvc + @WebMvcTest
- Repositories con @DataJpaTest + H2

### Tests de Seguridad
- Endpoints protegidos (401/403)
- @PreAuthorize funcionando correctamente

---

## Escalabilidad y Mantenibilidad

### Preparado para Escalar
- ✅ Stateless (JWT) → múltiples instancias sin shared sessions
- ✅ Connection pooling (HikariCP por defecto)
- ✅ WebClient asíncrono para integraciones

### Fácil de Mantener
- ✅ Arquitectura en capas clara
- ✅ Código autodocumentado
- ✅ Swagger para documentación de API
- ✅ Excepciones con mensajes claros

### Fácil de Extender
- ✅ Agregar nuevo endpoint: Controller → Service → Repository
- ✅ Cambiar storage: Implementar StorageService
- ✅ Agregar notificación: Implementar listener/event

---

## Configuración Externalizada

**application.properties:**
- Base de datos (URL, credenciales)
- JWT (secret, expiration)
- API externa (base URL, timeout)
- Storage (type, base path)
- Logging levels

**Ventajas:**
- Configuración específica por entorno (dev, prod)
- Secrets fuera del código
- Profile-specific configs (@Profile)

---

## Conclusión

Esta arquitectura cumple con:
- ✅ Requisitos funcionales de la prueba técnica
- ✅ Mejores prácticas de Spring Boot 4
- ✅ Código limpio y mantenible
- ✅ Seguridad robusta (JWT + RBAC)
- ✅ Preparado para revisión técnica senior
- ✅ Sin sobreingeniería

**Próximos pasos:** Implementar lógica de servicios, StorageService y tests.
