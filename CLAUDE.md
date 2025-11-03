# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Cretas Backend System** (白垩纪食品溯源系统) - A Spring Boot backend service for food traceability management. This system tracks the complete production lifecycle including raw materials, production batches, equipment usage, quality inspections, and distribution chain management.

**Tech Stack:**
- Spring Boot 2.7.15
- Java 11
- Maven
- MySQL 8
- Redis (session management)
- MyBatis + JPA/Hibernate
- Sa-Token + JWT authentication
- SpringDoc OpenAPI 3.0 documentation

## Development Commands

### Build & Run

```bash
# Build project (skip tests)
mvn clean package -DskipTests

# Run application
mvn spring-boot:run

# Run packaged JAR
java -jar target/cretas-backend-system-1.0.0.jar

# Build with tests
mvn clean install
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Run specific test method
mvn test -Dtest=AuthControllerTest#testPlatformLogin
```

### Database

```bash
# Connect to MySQL
mysql -h 106.14.165.234 -u Cretas -pnDJs8tpFphAYxdXi cretas

# Check Redis connection
redis-cli -h 106.14.165.234 -p 6379 -a 123456 ping
```

### Deployment

```bash
# Package for production
mvn clean package -DskipTests

# Start on server (see fix-document/部署指南.md for full deployment)
nohup java -jar -Xms512m -Xmx1024m cretas-backend-system-1.0.0.jar > logs/application.log 2>&1 &

# Check application health
curl http://localhost:10010/actuator/health
```

## Architecture

### Package Structure

The codebase follows a **layered architecture** pattern:

```
com.cretas.aims/
├── CretasApplication.java       # Main entry point (@SpringBootApplication)
├── controller/                  # REST API endpoints (20+ controllers)
├── service/                     # Business logic interfaces
│   └── impl/                    # Service implementations
├── repository/                  # Spring Data JPA repositories (23+ repos)
├── entity/                      # JPA entities (30+ entities)
│   ├── BaseEntity.java          # Abstract base with audit fields
│   └── enums/                   # Domain enums (10+ enums)
├── dto/                         # Data Transfer Objects
│   ├── common/                  # ApiResponse, PageRequest, PageResponse
│   ├── auth/, user/, production/, etc.
├── mapper/                      # DTO mappers
├── security/                    # JWT authentication filter
├── config/                      # Spring configuration
├── exception/                   # Custom exceptions & global handler
└── util/                        # Utilities (JwtUtil, etc.)
```

### Key Architectural Patterns

1. **Dependency Injection**: Constructor-based injection using Lombok `@RequiredArgsConstructor`
2. **DTO Pattern**: Separation between entities and API contracts with explicit mappers
3. **Multi-Tenant Design**: Factory-based isolation with `factoryId` in domain entities
4. **Dual Authentication**: Separate flows for factory users and platform administrators
5. **Stateless API**: JWT-based authentication suitable for distributed systems
6. **Base Entity Pattern**: All entities extend `BaseEntity` for automatic audit timestamps

## Authentication & Security

### Authentication Stack

- **JWT** (jjwt 0.11.5): Primary token mechanism
- **Sa-Token** (1.28.0): Session management
- **Spring Security**: Security framework with method-level security
- **BCrypt**: Password hashing

### Key Security Files

- `security/JwtAuthenticationFilter.java`: Per-request JWT validation filter
- `config/SecurityConfig.java`: Security configuration and filter chain
- `util/JwtUtil.java`: JWT generation and validation
- `controller/AuthController.java`: Login/registration endpoints

### Authentication Flow

1. User submits credentials to `/api/auth/login` or `/api/auth/platform-login`
2. AuthService validates against BCrypt password hash
3. JWT generated with claims (userId, factoryId, username, role)
4. Session record created in database
5. Token returned to client
6. Client includes token in `Authorization: Bearer <token>` header
7. JwtAuthenticationFilter validates on each request

### Role-Based Access Control

**Platform Roles** (`PlatformRole` enum):
- `super_admin`: Full system access
- `system_admin`: System configuration
- `operation_admin`: Operations management
- `auditor`: Read-only audit access

**Factory User Roles** (`FactoryUserRole` enum):
- `factory_super_admin`: Full factory access
- `permission_admin`: User/permission management
- `department_admin`: Department management
- `operator`: Production operations
- `viewer`: Read-only access

### Test Accounts

**Platform Admins:**
- `platform_admin / admin123` (super_admin)
- `system_admin / admin123` (system_admin)

**Factory Users (factoryId: F001):**
- `testadmin / password123` (factory_super_admin)
- `testuser / password123` (operator)

## Database Access

### Dual ORM Approach

1. **JPA/Hibernate** (primary): Entity mapping, relationships, CRUD operations
   - Automatic audit timestamps via `@CreatedDate` and `@LastModifiedDate`
   - Lazy loading for relationships
   - Cascade operations

2. **MyBatis** (legacy/complex queries): XML-based queries in `src/main/resources/mapper/`
   - Used for complex joins and custom SQL
   - Configured for underscore-to-camelCase conversion

### Repository Pattern

All repositories extend `JpaRepository<Entity, ID>`:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.factoryId = :factoryId")
    Page<User> findByFactoryId(@Param("factoryId") String factoryId, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Integer userId, @Param("lastLogin") LocalDateTime lastLogin);
}
```

### Key Entity Relationships

- **User** → Factory (many-to-one)
- **ProductionPlan** → Factory, User (many-to-one)
- **ProductionBatch** → ProductionPlan (many-to-one)
- **MaterialBatch** → Supplier (many-to-one)
- **MaterialConsumption** → MaterialBatch, ProductionBatch (many-to-one)
- **QualityInspection** → ProductionBatch (one-to-many)

## Configuration

### Application Configuration

Primary config: `src/main/resources/application.yml`

**Key Settings:**
- Server port: `10010`
- MySQL: `106.14.165.234:3306/cretas`
- Redis: `106.14.165.234:6379`
- JWT secret: `cretas.jwt.secret` (change in production!)
- JWT expiration: 24 hours (access), 30 days (refresh)
- Sa-Token timeout: 30 days
- File upload path: `/data/uploads`

### Environment-Specific Config

For production, override settings via:
1. External `application-prod.yml`
2. Environment variables
3. JVM arguments: `-Dspring.profiles.active=prod`

## Domain Model

### Core Entities

**Production Flow:**
1. **ProductionPlan**: Planned production with target quantities
2. **MaterialBatch**: Raw materials with batch numbers and supplier info
3. **MaterialConsumption**: Tracks which materials used in which production batches
4. **ProductionBatch**: Finished goods with full traceability
5. **QualityInspection**: Quality control records per batch

**Supporting Entities:**
- **Supplier**: Raw material sources
- **Customer**: Distribution tracking
- **Equipment**: Machinery and tools
- **EquipmentUsage**: Equipment logging for hygiene/safety compliance
- **EmployeeWorkSession**: Time tracking for labor cost allocation
- **Factory**: Multi-tenant factory management
- **User**: Factory users with role-based access

### Status Enums

Key status transitions:
- `ProductionPlanStatus`: PENDING → IN_PROGRESS → COMPLETED → CANCELLED
- `QualityInspectionStatus`: PENDING → PASSED / FAILED / CANCELLED
- `MaterialBatchStatus`: IN_STOCK → ALLOCATED → USED → EXPIRED

## API Design

### Standard Response Format

All endpoints return `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-01-01T12:00:00"
}
```

### Pagination

Use `PageRequest` (query params) and return `PageResponse<T>`:

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "pageSize": 10
}
```

### Error Handling

Global exception handler in `exception/GlobalExceptionHandler.java`:
- `BusinessException` → 400
- `AuthenticationException` → 401
- `AuthorizationException` → 403
- `ResourceNotFoundException` → 404
- `ValidationException` → 422

## API Documentation

**Swagger UI**: `http://localhost:10010/swagger-ui/index.html`
**OpenAPI JSON**: `http://localhost:10010/v3/api-docs`
**OpenAPI YAML**: `http://localhost:10010/v3/api-docs.yaml`

Configuration in `config/SwaggerConfig.java` using SpringDoc OpenAPI 3.0 (v1.7.0)

### Importing to Apifox

1. **Via URL** (Recommended):
   - Open Apifox → Import Data → URL Import
   - Enter: `http://localhost:10010/v3/api-docs`
   - Format: OpenAPI 3.0
   - Click Import

2. **Via File**:
   ```bash
   curl http://localhost:10010/v3/api-docs > openapi.json
   ```
   Then import the file in Apifox

## Adding New Features

### Standard Implementation Flow

1. **Create Entity** in `entity/` extending `BaseEntity`
   - Add JPA annotations and relationships
   - Add to relevant enum if status tracking needed

2. **Create Repository** in `repository/` extending `JpaRepository`
   - Add custom query methods as needed

3. **Create DTOs** in `dto/domain/`
   - Request DTOs with validation annotations
   - Response DTOs with necessary fields

4. **Create Mapper** in `mapper/`
   - Entity-to-DTO conversion methods

5. **Create Service Interface** in `service/`
   - Define business operations

6. **Create Service Implementation** in `service/impl/`
   - Use `@Service` and `@RequiredArgsConstructor`
   - Inject dependencies via constructor

7. **Create Controller** in `controller/`
   - Use `@RestController` and `@RequestMapping`
   - Add Swagger annotations
   - Return `ApiResponse<T>`

8. **Add Tests** (if test infrastructure exists)

### Example Feature Implementation

```java
// 1. Entity
@Entity
@Table(name = "feature_name")
@Data
@EqualsAndHashCode(callSuper = true)
public class FeatureName extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;
}

// 2. Repository
@Repository
public interface FeatureNameRepository extends JpaRepository<FeatureName, Integer> {
    List<FeatureName> findByFactoryId(String factoryId);
}

// 3. Service
@Service
@RequiredArgsConstructor
public class FeatureNameService {
    private final FeatureNameRepository repository;
    private final FeatureNameMapper mapper;

    public FeatureNameDTO create(CreateFeatureNameRequest request) {
        FeatureName entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }
}

// 4. Controller
@RestController
@RequestMapping("/api/feature-name")
@RequiredArgsConstructor
public class FeatureNameController {
    private final FeatureNameService service;

    @PostMapping
    public ApiResponse<FeatureNameDTO> create(@Valid @RequestBody CreateFeatureNameRequest request) {
        return ApiResponse.success(service.create(request));
    }
}
```

## Common Development Patterns

### Factory Context in JWT

Most operations require factory context. Extract from JWT:

```java
@PostMapping
public ApiResponse<T> operation(@RequestHeader("Authorization") String token) {
    String factoryId = jwtUtil.extractFactoryId(token.substring(7));
    // Use factoryId for data isolation
}
```

### Pagination

```java
@GetMapping
public ApiResponse<PageResponse<DTO>> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id,desc") String sort
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(...));
    Page<Entity> result = repository.findAll(pageable);
    return ApiResponse.success(PageResponse.of(result.map(mapper::toDTO)));
}
```

### Transaction Management

Use `@Transactional` on service methods that perform multiple database operations:

```java
@Transactional
public void completeProduction(Integer planId) {
    ProductionPlan plan = planRepository.findById(planId).orElseThrow();
    plan.setStatus(ProductionPlanStatus.COMPLETED);
    planRepository.save(plan);

    // Update related records atomically
    materialConsumptionRepository.updateStatus(planId, MaterialStatus.USED);
}
```

## Important Notes

### Multi-Tenant Data Isolation

Always filter by `factoryId` when querying:
- Factory users can only access their own factory's data
- Platform admins can access all factories
- Verify factory context in service layer

### Whitelist-Based Registration

Phone numbers must be pre-registered in `phone_whitelist` table before users can register. Verification codes are logged to console in development mode.

### Session Management

Sa-Token creates session records in the database. Token expiration is 30 days with 2-hour activity timeout. Concurrent logins are disabled by default.

### Password Security

Always use BCrypt for password hashing. Never store plaintext passwords. Use `PasswordEncoder` bean for encoding/validation.

## Documentation References

Detailed documentation in `fix-document/`:
- `快速开始.md`: Quick start guide with test accounts
- `部署指南.md`: Deployment instructions for production
- `测试流程文档.md`: Testing procedures
- `登录注册流程说明.md`: Authentication flow details
- `功能对比检查报告.md`: Feature comparison checklist

## Troubleshooting

### Redis Connection Issues
```bash
redis-cli -h 106.14.165.234 -p 6379 -a 123456 ping
```

### Database Connection Issues
Check `application.yml` credentials and verify MySQL is accessible.

### JWT Validation Failures
Verify JWT secret matches between token generation and validation. Check token expiration.

### Port Already in Use
```bash
lsof -ti:10010 | xargs kill -9
```

### Verification Codes in Development
Check console logs for verification codes (not sent via SMS in dev mode):
```
发送验证码到手机号: 13800138000, 验证码: 784521 (开发环境,未实际发送)
```
- add to memory
- add to memory "remember my ip address, never use47.251.121.76:10010 but the correct one is http://106.14.165.234:10010"