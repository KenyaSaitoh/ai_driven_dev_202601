# Sample Output Example

This file demonstrates example outputs when using this Agent Skill.

---

## Example 1: Task Generation Output

### Input Parameters
```yaml
project_root: "projects/sdd/bookstore/berry-books-api-sdd"
spec_directory: "projects/sdd/bookstore/berry-books-api-sdd/specs"
output_directory: "projects/sdd/bookstore/berry-books-api-sdd/tasks"
```

### Generated Task Files

```
tasks/
├── tasks.md                    # Main task list
├── setup_tasks.md              # Project setup
├── common_tasks.md             # Common components
├── API_001_auth.md             # Authentication API
├── API_002_books.md            # Book API (proxy)
├── API_003_categories.md       # Category API (proxy)
├── API_004_orders.md           # Order API (local)
├── API_005_images.md           # Image serving
└── integration_tasks.md        # Integration tests
```

### Sample Task File Content (API_001_auth.md)

```markdown
# Authentication API Tasks

**Assignee:** 1 developer
**Recommended Skills:** Jakarta EE, JAX-RS, JWT, REST API design
**Estimated Effort:** 16 hours
**Dependent Tasks:** [Setup Tasks](setup_tasks.md), [Common Tasks](common_tasks.md)

---

## Overview

Implement JWT-based authentication system that integrates with customer-hub-api
for customer data management.

---

## Tasks

### DTO and Request Models

- [ ] [P] **T_API001_001**: Create LoginRequest DTO
  - **Purpose**: Define request structure for login endpoint
  - **Target**: LoginRequest.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) "2.1 LoginRequest"
  - **Notes**: Include email and password fields with validation annotations

- [ ] [P] **T_API001_002**: Create RegisterRequest DTO
  - **Purpose**: Define request structure for registration endpoint
  - **Target**: RegisterRequest.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) "2.2 RegisterRequest"
  - **Notes**: Include all customer fields required for registration

### External Integration

- [ ] **T_API001_003**: Create CustomerHubRestClient
  - **Purpose**: REST client for customer-hub-api integration
  - **Target**: CustomerHubRestClient.java
  - **Reference SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) "3. customer-hub-api Integration"
  - **Notes**: Implement findByEmail, findById, and register methods

### Security Components

- [ ] **T_API001_004**: Create JwtUtil
  - **Purpose**: JWT token generation and validation
  - **Target**: JwtUtil.java
  - **Reference SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) "8. Security Design"
  - **Notes**: Use jjwt library, configure token expiration

- [ ] **T_API001_005**: Create JwtAuthenFilter
  - **Purpose**: JAX-RS filter for JWT authentication
  - **Target**: JwtAuthenFilter.java
  - **Reference SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) "8. Security Design"
  - **Notes**: Extract JWT from cookie, validate, and populate AuthenContext

- [ ] **T_API001_006**: Create AuthenContext
  - **Purpose**: Store authenticated user information
  - **Target**: AuthenContext.java
  - **Reference SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) "8. Security Design"
  - **Notes**: Use @RequestScoped CDI bean

### Resource Endpoints

- [ ] **T_API001_007**: Create AuthenResource
  - **Purpose**: Implement authentication API endpoints
  - **Target**: AuthenResource.java
  - **Reference SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) "3. Endpoint Specifications"
    - [behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) "2. Authentication Scenarios"
  - **Notes**: Implement /login, /logout, /register, /me endpoints

### Testing

- [ ] [P] **T_API001_008**: Create AuthenResourceTest (Unit)
  - **Purpose**: Unit test for AuthenResource
  - **Target**: AuthenResourceTest.java
  - **Reference SPEC**: [behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) "2. Authentication Scenarios"
  - **Notes**: Mock CustomerHubRestClient, test all scenarios

- [ ] [P] **T_API001_009**: Create JwtUtilTest
  - **Purpose**: Unit test for JWT utility
  - **Target**: JwtUtilTest.java
  - **Reference SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) "8. Security Design"
  - **Notes**: Test token generation, validation, expiration

- [ ] **T_API001_010**: Create AuthApiIntegrationTest
  - **Purpose**: Integration test for authentication endpoints
  - **Target**: AuthApiIntegrationTest.java
  - **Reference SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) "3. Endpoint Specifications"
    - [behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) "2. Authentication Scenarios"
  - **Notes**: Use REST Assured, test full authentication flow, add @Tag("integration")

---

## Completion Criteria

- [ ] All authentication endpoints are functional
- [ ] JWT tokens are generated and validated correctly
- [ ] Unit tests achieve 80%+ coverage
- [ ] Integration tests pass all scenarios
- [ ] Security best practices are followed
```

---

## Example 2: Code Implementation Progress

### Input Parameters
```yaml
project_root: "projects/sdd/bookstore/berry-books-api-sdd"
task_file: "projects/sdd/bookstore/berry-books-api-sdd/tasks/API_001_auth.md"
```

### Progress Report

```
=== Task Execution Progress ===

Task File: API_001_auth.md
Total Tasks: 10
Completed: 7
In Progress: 1
Pending: 2

✅ T_API001_001: Create LoginRequest DTO - COMPLETED
✅ T_API001_002: Create RegisterRequest DTO - COMPLETED
✅ T_API001_003: Create CustomerHubRestClient - COMPLETED
✅ T_API001_004: Create JwtUtil - COMPLETED
✅ T_API001_005: Create JwtAuthenFilter - COMPLETED
✅ T_API001_006: Create AuthenContext - COMPLETED
✅ T_API001_007: Create AuthenResource - COMPLETED
🔄 T_API001_008: Create AuthenResourceTest (Unit) - IN PROGRESS
⏳ T_API001_009: Create JwtUtilTest - PENDING
⏳ T_API001_010: Create AuthApiIntegrationTest - PENDING

=== Files Created ===
1. src/main/java/pro/kensait/berrybooks/api/dto/LoginRequest.java
2. src/main/java/pro/kensait/berrybooks/api/dto/RegisterRequest.java
3. src/main/java/pro/kensait/berrybooks/external/CustomerHubRestClient.java
4. src/main/java/pro/kensait/berrybooks/security/JwtUtil.java
5. src/main/java/pro/kensait/berrybooks/security/JwtAuthenFilter.java
6. src/main/java/pro/kensait/berrybooks/security/AuthenContext.java
7. src/main/java/pro/kensait/berrybooks/api/AuthenResource.java

=== Current Status ===
Implementing unit tests for AuthenResource. All production code is complete and 
follows specifications. Tests will verify login, logout, register, and /me endpoints
with mocked CustomerHubRestClient.

=== Next Steps ===
1. Complete T_API001_008 (AuthenResourceTest)
2. Proceed to T_API001_009 (JwtUtilTest) 
3. Finish with T_API001_010 (AuthApiIntegrationTest)

All tasks follow BFF pattern guidelines and use specified technical stack
(Jakarta EE 10, JAX-RS 3.1, jjwt 0.12.6).
```

---

## Example 3: Verification Report

### Completion Verification Output

```
=== Completion Verification Report ===

Project: berry-books-api
Task File: API_001_auth.md
Date: 2025-01-04

## Charter Compliance
✅ Development principles adhered to
✅ Test coverage standards met (85% for service/API layer)
✅ Coding conventions followed
✅ Security requirements satisfied

## Task Completion
✅ All 10 tasks completed
✅ All files created as specified
✅ No pending or blocked tasks

## Specification Traceability
✅ All endpoints in functional_design.md implemented:
   - POST /api/auth/login
   - POST /api/auth/logout
   - POST /api/auth/register
   - GET /api/auth/me

✅ All scenarios in behaviors.md tested:
   - Successful login
   - Invalid credentials
   - Registration with existing email
   - Token expiration handling
   - Logout functionality

✅ Architecture design requirements met:
   - JWT token generation (jjwt 0.12.6)
   - HttpOnly cookie usage
   - @ApplicationScoped for services
   - @RequestScoped for context
   - Proper error handling

## Test Results
✅ Unit Tests: 12 passed, 0 failed (Coverage: 87%)
✅ Integration Tests: 8 passed, 0 failed
✅ Linter: No errors or warnings

## BFF Pattern Compliance
✅ Customer entity NOT implemented (external API)
✅ CustomerHubRestClient properly configured
✅ Authentication handled in BFF layer
✅ Proper error translation from external API

## Files Created (7 production + 3 test)
Production:
1. LoginRequest.java
2. RegisterRequest.java
3. CustomerHubRestClient.java
4. JwtUtil.java
5. JwtAuthenFilter.java
6. AuthenContext.java
7. AuthenResource.java

Tests:
1. AuthenResourceTest.java
2. JwtUtilTest.java
3. AuthApiIntegrationTest.java

## Summary
Authentication API implementation is COMPLETE and ready for integration.
All acceptance criteria met. No blockers or issues.

Recommended next task: API_002_books.md (can be executed in parallel with other APIs)
```

---

## Notes

These examples demonstrate:
1. How task files are structured
2. What a typical implementation progress looks like
3. How completion verification is reported

Actual outputs will vary based on:
- Project specifications
- AI platform being used
- Specific requirements and constraints
- Team preferences and conventions

