# Code Implementation Instruction

## About Agent Skills

This instruction conforms to **Agent Skills standard specification** (v1.0).

**Platform Independent:**
- Claude (Anthropic)
- GitHub Copilot
- ChatGPT (OpenAI)
- Gemini (Google)
- Cursor
- Other AI coding assistants

Compatible with all the above AI platforms.

**Platform-Specific Operations:**
- Specific operation methods such as file reading, creation, and editing vary by platform
- See `../../platform_guides/` for details

---

## Parameter Configuration

**Please configure the following parameters before execution:**

```yaml
project_root: "Enter your project root path here"
task_file: "Enter the task file path to execute here"
```

**Example (for berry-books-api):**
```yaml
project_root: "projects/sdd/bookstore/berry-books-api-sdd"
task_file: "projects/sdd/bookstore/berry-books-api-sdd/tasks/setup_tasks.md"
```

**Note:** 
- Adjust path separators according to your OS environment (Windows: `\`, Unix/Linux/Mac: `/`)
- Hereafter, replace `{project_root}` with the value configured above

---

## Implementation Execution

**Important: Execute only the tasks in the specified task file and stop when completed. Do NOT automatically proceed to the next task.**

Based on the project root and task file specified as parameters, execute the following:

### 1. Load and Analyze Implementation Context

**File Loading Methods:**
- File loading operations vary depending on the AI platform you're using
- **Cursor/Cline**: Use file reading tools
- **GitHub Copilot**: Use workspace context reference features
- **ChatGPT**: Request users to paste file contents
- **Others**: See `../../platform_guides/`

#### Documents to Load (in priority order)

1. **Highest Priority**: Charter files under `{project_root}/principles/` for development principles, architecture policies, quality standards, and organizational standards

2. **Required**: Complete task list and execution plan in specified task file
   - Task "Reference SPEC" is described in Markdown link format (clickable)
   - Must reference the linked SPEC file and specified section

3. **Required**: `{project_root}/specs/baseline/system/architecture_design.md` for:
   - Technical stack (language, version, framework, libraries)
   - Architecture patterns and layer structure
   - Package structure and naming conventions
   - Design patterns, transaction strategy, concurrency control
   - Logging strategy, error handling, security
   - Test strategy (test framework, coverage goals, test policy)
   - **Strictly adhere to the technical stack defined here when generating code**

4. **Required**: `{project_root}/specs/baseline/system/requirements.md` for functional requirements and success criteria

5. **Required**: `{project_root}/specs/baseline/system/functional_design.md` for system-wide functional design overview

6. **Required**: `{project_root}/specs/baseline/api/*/functional_design.md` for class design, methods, and endpoint specifications

7. **If exists**: `{project_root}/specs/baseline/system/data_model.md` for entities and relationships

8. **If exists**: `{project_root}/specs/baseline/api/*/behaviors.md` for acceptance criteria and test scenarios

9. **If exists**: `{project_root}/specs/baseline/system/external_interface.md` for external integration and API specifications

10. **Static Resources**: Check static files (images, etc.) in `{project_root}/resources/` folder and copy to appropriate locations during setup

**Note**: Replace `{project_root}` with the project root path explicitly specified in parameters.

### 2. Parse and Extract Task Structure

- **Task Composition**: Setup, common features, per-API implementation, integration/testing
- **Task Dependencies**: Sequential vs. parallel execution rules
- **Task Details**: ID, description, file path, parallel marker [P]
- **Execution Flow**: Order and dependency requirements

### 3. Execute Implementation Following Task Plan

- **Per-task Execution**: Complete each task before proceeding to next
- **Setup Tasks**: Execute resource placement (image file copying, etc.) with highest priority
- **Respect Dependencies**: Execute sequential tasks in order, parallel tasks [P] can be executed together
- **Follow TDD Approach**: Execute tests before corresponding implementation (if project adopts TDD)
- **File-based Coordination**: Tasks affecting same file must be executed sequentially
- **Verification Checkpoints**: Verify completion of each task before proceeding

### 4. Implementation Execution Rules

#### Project Root Usage
All path operations are based on the project root specified in parameters

#### Technical Stack Compliance
Strictly adhere to the technical stack described in architecture_design.md

- **Programming Language**: Check "1.1 Core Platform" in architecture_design.md (e.g., Java 21, Jakarta EE 10, etc.)
- **Framework**: Check "1.2 Jakarta EE Specifications" in architecture_design.md (e.g., JAX-RS 3.1, JPA 3.1, etc.)
- **Libraries and Versions**: Check "1.3 Additional Libraries" in architecture_design.md (e.g., jjwt 0.12.6, SLF4J, Log4j2, etc.)
- **Test Framework**: Check "Test Strategy" in architecture_design.md (e.g., JUnit 5, Mockito, REST Assured, etc.)
- **Database**: Check "Database Configuration" in architecture_design.md (e.g., HSQLDB, connection pool configuration, etc.)
- **Use specified version numbers accurately**: Do not use different versions

#### Charter Compliance
Adhere to development principles stated in charter under `{project_root}/principles/` for all implementation
- Follow test coverage standards, architecture patterns, and coding conventions
- Meet quality standards, security requirements, and performance standards

#### Setup Priority
Initialize project structure, dependencies, and configuration
- **Static Resource Placement**: Copy necessary images and files to appropriate locations
- Database schema setup

#### Tests Before Code
Create tests for contracts, entities, and integration scenarios (if TDD)

#### Core Development
Implement Entity, Dao, Service, Resource (JAX-RS endpoints)

#### Integration Work
Database connections, middleware, logging, external services

#### Finishing and Verification
Unit tests, performance optimization, documentation

### 5. Unit Test Generation Guidelines

- **Test Framework**: Use framework specified in "Test Strategy" in architecture_design.md (e.g., JUnit 5, Mockito)
- **Test Coverage**: Adhere to target values in "Test Coverage" in architecture_design.md (e.g., 80%+ for service layer)
- Extract and implement corresponding test cases from each Given-When-Then scenario in behaviors.md under api/
- Create normal/abnormal/boundary value tests for each method signature in functional_design.md under api/
- Verify entity validation rules (constraints, validation, uniqueness constraints) from data_model.md in tests
- Create test data referencing concrete examples in behaviors.md and functional_design.md
- Follow "Test Policy" in architecture_design.md when mocks or stubs are needed

### 6. API Integration Test Generation Guidelines

- **Test Framework**: Test REST APIs using REST Assured or JAX-RS Client
- **Framework Selection**: Check "Additional Libraries" and "Test Strategy" in architecture_design.md
- Create tests based on endpoint specifications (request/response format) in functional_design.md under api/
- Test scenarios in behaviors.md under api/ as actual HTTP requests
- For endpoints requiring JWT authentication, login first to obtain token
- Verify HTTP status codes, response bodies, headers
- Verify error cases (400, 401, 404, 500, etc.)
- Place tests under `src/test/java/<package>/api/` (refer to "Package Structure" in architecture_design.md for package)
- **Build Exclusion Configuration**: Configure API integration tests to NOT run in normal build (`./gradlew test`), enable individual execution
  - Add `@Tag("integration")` annotation to API integration test classes (JUnit 5)
  - Add configuration to Gradle's `build.gradle` to exclude "integration" tag from test task
  - Define dedicated Gradle task for integration tests (e.g., `integrationTest`) for individual execution
  - Make identifiable by test class naming convention (e.g., `*IntegrationTest.java` pattern)

---

## Component-Specific Reference Document Priority and Usage

### Important: Common Checks for All Component Generation

Reference the following in architecture_design.md:
- **Language/Version**: "1.1 Core Platform" (e.g., Java 21)
- **Package Placement**: "Package Organization"
- **Naming Conventions**: "Naming Conventions"
- **Annotations**: "1.2 Jakarta EE Specifications" and "State Management" (e.g., @Entity, @Path, @Inject)
- **Logging**: "Logging Strategy" (SLF4J usage, log levels, output policy)

### Entity Generation

- **Technical Stack**: Check Jakarta Persistence (JPA) version in "1.2 Jakarta EE Specifications" in architecture_design.md
- **Primary Reference**: data_model.md
  - Table structure, column definitions, data types, constraints (NOT NULL, UNIQUE, etc.)
  - Entity relationships (OneToMany, ManyToOne, etc.)
  - Validation rules (annotations like @NotNull, @Size, etc.)
- **Secondary Reference**: functional_design.md
  - Class design, attribute names, method signatures
  - Implement business logic methods if any (e.g., calculateTotal)
- **Concurrency Control**: Check use of optimistic locking (@Version) in "Concurrency Control" in architecture_design.md

### Dao Generation

- **Technical Stack**: Check Jakarta Persistence (JPA) and CDI versions in "1.2 Jakarta EE Specifications" in architecture_design.md
- **Scope**: Use @ApplicationScoped from "State Management" in architecture_design.md
- **Primary Reference**: functional_design.md under relevant api/
  - Dao interface, method signatures, return types
  - Query method operation specifications (search conditions, sort order, join conditions)
- **Secondary Reference**: data_model.md
  - Reference for SQL query design (table names, column names, join conditions)
  - Index and performance considerations

### Service Layer Generation

- **Technical Stack**: Check Jakarta CDI and Transactions versions in "1.2 Jakarta EE Specifications" in architecture_design.md
- **Scope**: Use @ApplicationScoped from "State Management" in architecture_design.md
- **Transactions**: Check @Transactional usage in "Transaction Management" in architecture_design.md
- **Primary Reference**: functional_design.md under relevant api/
  - Service class method signatures, business logic, processing flow
  - Transaction boundaries, exception handling, validation logic
- **Secondary Reference**: behaviors.md under relevant api/
  - Method behavior (Given-When-Then), business rules, constraints
  - Edge cases and error handling
- **Exception Handling**: Follow "Error Handling Policy" in architecture_design.md

### Resource (JAX-RS Endpoint) Generation

- **Technical Stack**: Check JAX-RS (Jakarta RESTful Web Services) version in "1.2 Jakarta EE Specifications" in architecture_design.md
- **Scope**: Select appropriate scope (typically @RequestScoped) from "State Management" in architecture_design.md
- **Primary Reference**: functional_design.md under relevant api/
  - Resource class design, endpoint specifications (HTTP method, path, parameters)
  - Request/response format (JSON), status codes, error handling
  - JWT authentication requirements, permission checks
- **Secondary Reference**: behaviors.md under relevant api/
  - Endpoint behavior, error cases, validation rules
- **Security**: Check JWT authentication implementation in "Security Design" in architecture_design.md

### DTO/Response Generation

- **Primary Reference**: functional_design.md under relevant api/
  - Request DTO, response DTO structure, field names, data types
  - Validation annotations (@NotNull, @Size, etc.)
- **Secondary Reference**: data_model.md
  - Correspondence with Entity, conversion logic

### Filter/Interceptor Generation

- **Technical Stack**: Check Jakarta Servlet and JAX-RS Filters in "1.2 Jakarta EE Specifications" in architecture_design.md
- **Primary Reference**: architecture_design.md
  - JWT authentication filter design, processing flow, error handling
  - CORS configuration, logging interceptor specifications
- **Secondary Reference**: functional_design.md
  - Security requirements, authentication/authorization specifications

### External Integration Component Generation

- **Technical Stack**: Check JAX-RS Client usage in "1.3 Additional Libraries" in architecture_design.md
- **Primary Reference**: external_interface.md
  - API specifications (endpoints, HTTP methods, request/response formats)
  - If OpenAPI YAML exists, check schema definitions, authentication methods, error responses
  - Communication protocols, timeout settings, retry policies
- **Secondary Reference**: functional_design.md
  - Integration class design, method names, error handling

### API Integration Test Generation (REST Assured/JAX-RS Client)

- **Primary Reference**: functional_design.md under relevant api/
  - Endpoint specifications (HTTP method, path, request/response formats)
  - Authentication requirements, status codes, error responses
- **Secondary Reference**: behaviors.md under relevant api/
  - Convert Given-When-Then scenarios to HTTP request/response tests
  - Error case responses (insufficient inventory, authentication failure, etc.)
- **Third Reference**: functional_design.md under system/
  - Inter-API integration, data flow, session (JWT) management

---

## Basic Policy for Document Reference

- **architecture_design.md**: Provides technical guidance for entire project (technical stack, architecture patterns, quality standards, etc.)
- **system/functional_design.md**: Provides system-wide functional design overview and links to each API
- **functional_design.md under api/**: Provides implementation-level details for each API (specific class design, method signatures, endpoint specifications, etc.)
- **behaviors.md under api/**: Provides behavior specifications (Given-When-Then), acceptance criteria, and test scenarios for each API
- **Reference methods for each component**: See sections above

---

## Progress Tracking and Error Handling

- Report progress after each completed task
- Stop execution if sequential task fails
- For parallel tasks [P], continue successful tasks and report failed tasks
- Provide clear error messages with context for debugging
- Suggest next steps if implementation cannot continue
- **Important**: Mark completed tasks as [X] in task file

**Task Update Methods:**
- File editing operations vary depending on the AI platform you're using
- See `../../platform_guides/` for details

---

## Completion Verification

- Confirm charter principles and quality standards are adhered to
- Confirm all required tasks are completed
- Confirm implemented features match requirements
- Verify tests pass and coverage meets requirements
- Confirm implementation follows architecture design
- Verify class design matches functional design specifications
- **Specification Traceability Verification**:
  - Confirm all acceptance criteria (Given-When-Then) in behaviors.md under api/ are covered by test cases
  - Confirm all endpoints, DTOs, classes, methods defined in functional_design.md under api/ are implemented
  - Confirm all constraints (NOT NULL, UNIQUE, FK, etc.) defined in data_model.md are implemented
  - Confirm all API specifications defined in external_interface.md are implemented
- Confirm static resources are correctly placed
- Report final status with summary of completed work
- **Stop here when all tasks in this task file are completed**

---

## BFF Pattern Specific Implementation Requirements

### berry-books-api Architecture Characteristics

**BFF (Backend for Frontend) Role:**
- Single entry point for frontend (berry-books-spa)
- Integrates multiple backend microservices
- Provides frontend-optimized APIs

### Implementation Pattern Distinction

#### Proxy Pattern (Transparent Forwarding to External APIs)
The following Resources have no independent business logic and only forward to external APIs:

**BookResource (Proxy → back-office-api)**:
- GET /api/books → back-office-api
- GET /api/books/{id} → back-office-api
- GET /api/books/search/jpql → back-office-api
- GET /api/books/search/criteria → back-office-api
- **Implementation**: Forward as-is via BackOfficeRestClient
- **Note**: Do NOT implement BookService, BookDao, Book entity

**CategoryResource (Proxy → back-office-api)**:
- GET /api/categories → back-office-api
- **Implementation**: Forward as-is via BackOfficeRestClient
- **Note**: Do NOT implement CategoryService, CategoryDao, Category entity

#### Local Implementation Pattern (Has Business Logic)

**AuthenResource (Local Implementation + customer-hub-api Integration)**:
- JWT generation/verification implemented in BFF layer
- Customer information retrieval via customer-hub-api
- **Implementation**: AuthenResource, JwtUtil, JwtAuthenFilter, AuthenContext
- **External Integration**: CustomerHubRestClient
- **Note**: Do NOT implement Customer entity (managed by external API)

**OrderResource (Local Implementation + External API Integration)**:
- Implement order processing business logic
- Inventory check/update via back-office-api
- Order data managed in local DB
- **Implementation**: OrderResource, OrderService, OrderTranDao, OrderDetailDao
- **Entities**: OrderTran, OrderDetail, OrderDetailPK (only these implemented)
- **External Integration**: BackOfficeRestClient (inventory management)

**ImageResource (Local Implementation)**:
- Directly serve resources (image files) from within WAR
- Use ServletContext.getResourceAsStream()
- **Implementation**: ImageResource

### Components NOT to Implement

The following components are managed by external APIs and are **NOT implemented in berry-books-api**:

**Entities (Do NOT implement)**:
- ❌ Book entity
- ❌ Stock entity
- ❌ Category entity
- ❌ Publisher entity
- ❌ Customer entity

**DAOs (Do NOT implement)**:
- ❌ BookDao
- ❌ StockDao
- ❌ CategoryDao
- ❌ PublisherDao
- ❌ CustomerDao

**Services (Do NOT implement)**:
- ❌ BookService
- ❌ CategoryService

### External API Integration Components (Required Implementation)

**BackOfficeRestClient**:
- Handles integration with back-office-api
- Book/inventory/category information retrieval
- Inventory update (optimistic locking support)
- **Configuration**: `back-office-api.base-url` property
- **Reference SPEC**: "14. back-office-api Integration" in external_interface.md

**CustomerHubRestClient**:
- Handles integration with customer-hub-api
- Customer information retrieval/registration
- **Configuration**: `customer-hub-api.base-url` property
- **Reference SPEC**: "3. customer-hub-api Integration" in external_interface.md

### Data Model Constraints

**Entities to Implement (order-related only)**:
- ✅ OrderTran (order transaction)
- ✅ OrderDetail (order detail)
- ✅ OrderDetailPK (order detail composite primary key)

**Database Table Management**:
- Directly manage only ORDER_TRAN and ORDER_DETAIL tables
- Other tables (BOOK, STOCK, etc.) managed by external APIs

### Transaction Management Considerations

**Distributed Transactions**:
- Inventory update: Independent transaction in back-office-api
- Order creation: JTA transaction in berry-books-api
- **Eventual Consistency**: Note cases where order creation fails after successful inventory update
- **Reference SPEC**: "6. Transaction Management (BFF Pattern)" in architecture_design.md

---

## Important Notes

### Task Execution Scope
- This instruction assumes complete task breakdown exists in task file
- If tasks are incomplete or missing, first use `task_generation.md` instruction to generate task list
- **Execute only tasks in specified task file. Do NOT automatically proceed to other task files (e.g., next feature's tasks).**
- Tasks are units of division of labor. After completing one task, wait for user confirmation before proceeding to next task.

### REST API Specific Notes
- Do not implement View/XHTML as UI is not included
- Use REST Assured or JAX-RS Client for endpoint testing
- Consider appropriate use of JWT authentication, CORS, and HTTP status codes
- Implement validation for request/response JSON formats

### Project Root Handling
- Replace `{project_root}` with the path explicitly specified in parameters
- Can use either relative or absolute paths
- All file operations are based on this project root

---

## Platform-Specific Guides

Execution methods for this instruction vary by AI platform.
See the following for details:

- **Cursor/Cline**: `../../platform_guides/cursor_cline.md`
- **GitHub Copilot**: `../../platform_guides/github_copilot.md`
- **Others**: `../../platform_guides/other_platforms.md`

