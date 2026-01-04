# Task Generation Instruction

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
- Specific operation methods such as file reading and file creation vary by platform
- See `../../platform_guides/` for details

---

## Parameter Configuration

**Please configure the following parameters before execution:**

```yaml
project_root: "Enter your project root path here"
spec_directory: "Enter your specification directory path here"
output_directory: "Enter your task output directory path here (optional)"
```

**Example (for back-office-api):**
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs"
output_directory: "projects/sdd/bookstore/back-office-api-sdd/tasks"
```

**Note:** 
- Adjust path separators according to your OS environment (Windows: `\`, Unix/Linux/Mac: `/`)
- Hereafter, replace `{project_root}` with the value configured above

---

## Project Role (Example: back-office-api)

**Microservice Pattern for back-office-api:**

- **Architecture Pattern**: Microservice
- **Role**: Complete data management service for books, inventory, categories, and publishers
- **Called By**: BFF (berry-books-api) as REST API
- **Managed Data**: 
  - Book (Books)
  - Stock (Inventory) - **@Version annotation required**
  - Category (Categories)
  - Publisher (Publishers)
- **Important Implementation Requirements**:
  - **Optimistic Locking**: Stock entity MUST have `@Version` annotation
  - **Dual Search Implementation**: Implement both JPQL and Criteria API search for books
  - **All Entities Implementation**: Implement Book, Stock, Category, Publisher entities
  - **CORS Support**: Handle cross-origin requests from BFF
  - **Error Handling**: OptimisticLockException → HTTP 409 Conflict

**Note:** The above is an example for back-office-api. Determine your project's role from the specifications (`{spec_directory}/system/architecture_design.md`).

---

## Overview

This instruction is designed to generate implementation task lists from completed specifications that enable **parallel work by multiple team members**.

**Important Principles:**
- Task lists are created at a **high level of abstraction**
- **Do NOT include source code or detailed implementation steps**
- Each task clearly indicates "what to create/modify"
- Detailed implementation is done in the next "implementation phase (code generation)" with reference to specifications

**Output Destination:**
- **For base projects**: `{project_root}/tasks/` directory
- **For extensions**: `{spec_directory}/tasks/` directory
- **Important**: Replace `{project_root}` and `{spec_directory}` with the values specified in parameters

---

## 1. Design Document Analysis

Based on the project information specified in parameters, load and analyze the following design documents:

**File Loading Methods:**
- File loading operations vary depending on the AI platform you're using
- **Cursor/Cline**: Use file reading tools
- **GitHub Copilot**: Use workspace context reference features
- **ChatGPT**: Request users to paste file contents
- **Others**: See `../../platform_guides/`

**Note:** Replace `{project_root}` with the path specified in parameters. All paths are relative to that project root.

### Project Charter (Highest Priority)
- **principles/** - Confirm development principles, architecture policies, quality standards, and organizational standards
  - `constitution.md` - Project development charter
  - Other charter files commonly used by the organization or team
  - **Important**: Even in task generation, adhere to principles stated in the charter (test coverage standards, architecture patterns, coding conventions, etc.)

### Required Documents (under system/)
- **architecture_design.md** - Confirm technical stack, architecture, and libraries
- **functional_design.md** - Confirm overall system functional design and links to each API

### Optional Documents (under system/, if they exist)
- **data_model.md** - Confirm entities and database schema
- **behaviors.md** - Confirm overall system behavior and links to each API
- **external_interface.md** - Confirm external integration and API specifications (including OpenAPI YAML)

### API-specific Documents (under api/)
- **api/API_XXX_yyyy/** - Each API directory (e.g., API_001_auth, API_002_books)
  - **functional_design.md** - Detailed functional design for each API (endpoints, request/response, business rules)
  - **behaviors.md** - Behavior specifications for each API (acceptance criteria, Given-When-Then)

**Note:** Available documents vary by project. Generate tasks based on what's available.

---

## 2. Task File Division Structure

**Output Destination Determination:**
- **For base projects**: `{project_root}/tasks/` directory
- **For extensions**: `{spec_directory}/tasks/` directory

**Important:** 
- Replace `{project_root}` with the path specified in parameters
- Base project tasks are placed in `{project_root}/tasks/`
- Extension tasks are placed in `{spec_directory}/tasks/` (e.g., `specs/enhancements/202512_inventory_alert/tasks/`)

To enable parallel work by multiple members, divide task files as follows:

### 2.1 Main Task List
**`tasks/tasks.md`** (placed in specified output destination)
- Main task list showing overall execution order
- Overview of each task and member assignment
- Link collection to other task files
- Explicit task dependencies

### 2.2 Setup Tasks
**`tasks/setup_tasks.md`**
- Project initialization (executed once by everyone before work)
- Development environment setup
- Database initialization
- Application server configuration
- Logging configuration
- Static resource placement (images, etc.)

### 2.3 Common Feature Tasks
**`tasks/common_tasks.md`**
- Common components shared across multiple features
- **All Entities**: Book, Stock, Category, Publisher (implement all)
- **Important**: Stock entity MUST have `@Version` annotation (optimistic locking)
- **Common DAOs**: DAOs for each entity
  - BookDao (JPQL search)
  - BookDaoCriteria (Criteria API search) - **implement both**
  - StockDao (optimistic locking support)
  - CategoryDao
  - PublisherDao
- **Common Services**: As needed
- **Common DTO/Response Models**: ErrorResponse, etc.
- **Exception Handlers**: OptimisticLockExceptionMapper (HTTP 409 Conflict)
- **CORS Filter**: Handle BFF requests

**Note - Microservice Pattern**: 
This project **implements ALL entities and DAOs**:
- ✅ Book, Stock, Category, Publisher entities
- ✅ BookDao, BookDaoCriteria, StockDao, CategoryDao, PublisherDao
- ✅ All Service classes
- ✅ All REST API endpoints

Determine specific common components from the project SPEC.

### 2.4 Feature-specific Tasks (per API)

**Extract features (APIs) from SPEC and generate task files:**

#### Feature Identification and Extraction
1. **Feature (API) Identification**
   - Extract features from requirements.md, api/ directory, and functional_design.md
   - Analyze the scope and responsibilities of each API (Auth API, Book API, Order API, Image API, etc.)
   - Understand inter-API dependencies

2. **Task File Naming Convention**
   - Basic format: `tasks/[API_ID]_[API_name].md`
   - Examples: `API_001_auth.md`, `API_002_books.md`, `API_003_orders.md`
   - Correspond to directory names under api/
   - **Note**: Use underscore separators in file names

3. **Contents of Each Feature Task File**
   - API-specific Resource classes (JAX-RS endpoints)
   - API-specific Service classes
   - API-specific Dao classes
   - API-specific DTO/response models
   - API-specific test cases (unit tests, API tests)
   - **Assignee:** 1 person (can be implemented independently per API)

4. **Feature Division Criteria**
   - **Small projects (1-3 APIs)**: Can be consolidated into one `all_apis.md`
   - **Medium projects (4-10 APIs)**: Divide files per API
   - **Large projects (10+ APIs)**: Consider directory division by API group

### 2.5 Integration Test Tasks
**`tasks/integration_tasks.md`**
- Inter-API integration tests
- E2E API tests (REST Assured/JAX-RS Client) - Test major business flows as API sequences
- Performance tests
- Security tests (JWT authentication, authorization)
- Final verification

---

## 3. Task Generation Rules

### 3.1 Parallel Execution Criteria

**Conditions for [P] marker (parallelizable):**
- Tasks editing different files
- Implementation of different entities
- Implementation of different APIs
- Independent test cases

**Conditions requiring sequential execution (no [P]):**
- Tasks editing the same file
- Tasks with dependencies (e.g., Entity → Dao → Service → Resource order)

### 3.2 Task Granularity

**Important: Define tasks at a high level of abstraction and do NOT include source code**

Divide each task by the following granularity:
- **Entity/Model**: Creation/modification of 1 entity class
- **DTO/Response**: Creation/modification of 1 DTO or response model class
- **Dao**: Creation/modification of 1 Dao class
- **Service**: Creation/modification of 1 Service class (divide into multiple tasks if complex)
- **Resource**: Creation/modification of 1 Resource class (JAX-RS endpoint)
- **Filter/Interceptor**: Creation/modification of 1 filter/interceptor
- **Test**: Creation/modification of 1 test class

**Note**: Adapt the above terminology to your project's technology stack.

**Task Description Level:**
- Clearly describe "what to create/modify"
- Concisely describe "what functionality to implement"
- **Do NOT describe source code or detailed implementation steps**
- Detailed implementation is done in the next "implementation phase (code generation)"

### 3.3 Dependency Ordering

Arrange tasks in the following order:

1. **Setup** (prerequisite for all)
   - Development environment setup
   - Project initialization
   - Database configuration

2. **Common Features** (shared across multiple features)
   - Common Entity/Model
   - Common Utility/Helper
   - Common Service
   - JWT authentication infrastructure
   - Common DTO/Response

3. **Feature-specific Implementation (per API)** (parallelizable)
   - General implementation order: DTO/Response → Entity → Dao → Service → Resource
   - Each API can be implemented independently
   - **Note**: Follow project architecture for implementation order

4. **Integration Tests** (after all API implementation)
   - Inter-API integration
   - E2E API tests (REST Assured) - based on major business flows
   - Performance tests

---

## 4. Task File Format

Each task file should include the following information:

### 4.1 Header Information
```markdown
# [Task File Name]

**Assignee:** [Expected number of people and roles]
**Recommended Skills:** [Required skill set]
**Estimated Effort:** [Hours]
**Dependent Tasks:** [Prerequisite task files]
```

### 4.2 Task List
```markdown
- [ ] [P] **Task X.X.X**: [Task name]
  - **Purpose**: [Function/objective to be realized by this task]
  - **Target**: [Component name or file name to create/modify]
  - **Reference SPEC**: [Specification document to reference (Markdown link format)] "Section Number Section Name"
  - **Notes**: [Points to consider, if any]
```

**Task ID Naming Convention:**
- Use **underscore separators** for task IDs (e.g., `T_SETUP_001`, `T_API001_003`)
- **Do NOT use hyphens** (e.g., ~~`T-SETUP-001`~~, ~~`T-API001-003`~~)
- Format: `T_[Category]_[Number]` or `T_[API_ID]_[Number]`

**SPEC Reference Description Rules:**
- **Required**: Describe in Markdown link format to make it clickable directly to SPEC files
- **Required**: Use relative paths (relative path from task file to SPEC file)
- **Required**: Specify section number and section name
- **Format**: `[Filename](relative path) "Section Number Section Name"`

**Example:**
```markdown
- [ ] **T_API002_003**: Create BookDao
  - **Purpose**: Implement book information search/retrieval functionality
  - **Target**: BookDao.java (DAO class)
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) "2.2 BookDao"
  - **Notes**: Consider joins with categories and filtering conditions
```

**Multiple SPEC Reference Example:**
```markdown
- [ ] **T_API002_006**: Create BookResource
  - **Purpose**: Implement book search/retrieval API endpoints
  - **Target**: BookResource.java (JAX-RS Resource class)
  - **Reference SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) "2. Endpoint Specifications"
    - [behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) "2. Book API"
  - **Notes**: Add @Secured annotation to endpoints requiring JWT authentication
```

**Note: Do NOT describe source code or detailed implementation steps**

---

## 5. Main Task List (tasks.md) Structure

Generate `{project_root}/tasks/tasks.md` with the following structure:

```markdown
# [Project Name] - Implementation Task List

## Overall Structure and Assignment

### Task Overview
| Task | Task File | Assignee | Parallel | Estimated Effort |
|------|-----------|----------|----------|-----------------|
| 0. Setup | setup_tasks.md | Everyone | No | [From analysis] |
| 1. Common | common_tasks.md | Common Team | Partial | [From analysis] |
| 2. Auth API | API_001_auth.md | Member A | Yes | [From analysis] |
| 3. Book API | API_002_books.md | Member B | Yes | [From analysis] |
| 4. Order API | API_003_orders.md | Member C | Yes | [From analysis] |
| ... | ... | ... | ... | ... |
| N. Integration | integration_tasks.md | Everyone | Partial | [From analysis] |

### Execution Order
1. Task 0: Setup (everyone executes)
2. Task 1: Common (common team implements)
3. Tasks 2～N-1: API-specific implementation (each member executes in parallel) ← Parallelization point
4. Task N: Integration tests (everyone participates)

### Task File List
- [Setup Tasks](setup_tasks.md)
- [Common Tasks](common_tasks.md)
- [Auth API Tasks](API_001_auth.md)
- [Book API Tasks](API_002_books.md)
- ...
- [Integration Test Tasks](integration_tasks.md)

## Dependency Diagram
[Illustrate dependencies in Mermaid format]
\```

**Generation Notes:**
- Get project name from requirements.md
- Determine number of APIs, naming, and division method from SPEC
- Calculate estimated effort from complexity analysis of each task
- Use underscore separators for all file names

---

## 6. Deliverable Checklist

Requirements that generated task files should meet:
- Clearly describe "what to create/modify" without source code or detailed implementation steps
- Use [P] marker to indicate parallelizable tasks and clearly describe dependencies
- Assign unique task IDs with underscore separators (e.g., `T_SETUP_001`)

---

## 7. Generation Procedure

1. **Charter and SPEC Analysis**: Load charter under `principles/` and all SPEC files (system/ and api/) to understand development principles and overall functionality
2. **Feature (API) Extraction**: Extract APIs requiring implementation, identify dependencies and common components
3. **Task Division**: Determine appropriate file division method based on number of APIs (small: few files, medium: per API, large: per group)
4. **Task Composition**: Extract tasks from each SPEC, classify into setup/common/per API/integration, and order them
5. **Parallelization Determination**: Add [P] markers and generate task files to specified output destination
6. **Main List Generation**: Generate overall overview and execution plan in `tasks/tasks.md`

**File Generation Methods:**
- File creation operations vary depending on the AI platform you're using
- See `../../platform_guides/` for details

**Note**: `{project_root}` and output destination are specified in parameters. Use underscore separators for all file names and task IDs.

---

## 8. Important Notes

### Naming Conventions
- Use underscore separators for all file names and task IDs (e.g., `setup_tasks.md`, `T_SETUP_001`)
- Do NOT use hyphens (`-`)

### SPEC Reference Description
All task "Reference SPEC" should be described in the following format:
- Make it clickable in Markdown link format (e.g., `[functional_design.md](relative path)`)
- Specify section number and section name (e.g., `"2.2 BookDao"`)
- List multiple SPEC references in bullet points
- Appropriately reference both system/ and api/ documents

### Task Generation Principles
- **Charter Compliance**: Must adhere to charter (development principles, quality standards, organizational standards) under `{project_root}/principles/`
- **Maintain Abstraction**: Tasks describe only "what to create". Do NOT describe source code or detailed implementation steps
- **Consider Existing Code**: Clearly distinguish modification tasks from new creation tasks when existing implementation exists
- **API Test Configuration**: Specify configuration to exclude E2E API tests from normal builds and enable individual execution (JUnit `@Tag`, Gradle configuration, etc.)

### REST API Specific Notes
- Do not generate tasks for View/XHTML as UI is not included
- Use REST Assured or JAX-RS Client for API endpoint (Resource) testing
- Consider appropriate use of JWT authentication, CORS, and HTTP status codes

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

