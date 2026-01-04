# Sample Output Example (back-office-api)

This file demonstrates example outputs when using this Agent Skill for back-office-api.

---

## Example 1: Task Generation Output

### Input Parameters
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
spec_directory: "projects/sdd/bookstore/back-office-api-sdd/specs"
output_directory: "projects/sdd/bookstore/back-office-api-sdd/tasks"
```

### Generated Task Files

```
tasks/
├── tasks.md                    # Main task list
├── setup_tasks.md              # Project setup
├── common_tasks.md             # Common components (all entities)
├── API_001_books.md            # Book API (dual search)
├── API_002_stocks.md           # Stock API (optimistic locking)
├── API_003_categories.md       # Category API
├── API_004_publishers.md       # Publisher API
└── integration_tasks.md        # Integration tests
```

### Sample Task File Content (API_002_stocks.md)

```markdown
# Stock API Tasks (Optimistic Locking)

**Assignee:** 1 developer
**Recommended Skills:** Jakarta EE, JPA, Optimistic Locking, Concurrency Control
**Estimated Effort:** 14 hours
**Dependent Tasks:** [Setup Tasks](setup_tasks.md), [Common Tasks](common_tasks.md)

---

## Overview

Implement inventory management API with optimistic locking to handle concurrent
stock updates safely.

---

## Tasks

### Entity

- [ ] **T_API002_001**: Create/Verify Stock Entity with @Version
  - **Purpose**: Implement Stock entity with optimistic locking support
  - **Target**: Stock.java
  - **Reference SPEC**: [data_model.md](../specs/baseline/system/data_model.md) "Stock Table"
  - **Notes**: MUST include @Version annotation for concurrency control

### Data Access Layer

- [ ] **T_API002_002**: Create StockDao
  - **Purpose**: Implement data access for stock operations
  - **Target**: StockDao.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) "2.2 StockDao"
  - **Notes**: Include methods for stock retrieval and update

### Business Logic Layer

- [ ] **T_API002_003**: Create StockService
  - **Purpose**: Implement business logic for inventory management
  - **Target**: StockService.java
  - **Reference SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) "2.3 StockService"
    - [behaviors.md](../specs/baseline/api/API_002_stocks/behaviors.md) "Stock Management Scenarios"
  - **Notes**: Handle OptimisticLockException and implement retry logic if needed

### API Layer

- [ ] **T_API002_004**: Create StockResource
  - **Purpose**: Implement REST API endpoints for stock operations
  - **Target**: StockResource.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) "3. Endpoint Specifications"
  - **Notes**: Return HTTP 409 Conflict on optimistic lock failure

### Exception Handling

- [ ] **T_API002_005**: Create OptimisticLockExceptionMapper
  - **Purpose**: Map OptimisticLockException to HTTP 409 Conflict
  - **Target**: OptimisticLockExceptionMapper.java
  - **Reference SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) "Exception Handling"
  - **Notes**: Provide clear error message for concurrent modification

### Testing

- [ ] [P] **T_API002_006**: Create StockServiceTest (Unit)
  - **Purpose**: Unit test for StockService
  - **Target**: StockServiceTest.java
  - **Reference SPEC**: [behaviors.md](../specs/baseline/api/API_002_stocks/behaviors.md) "Stock Management Scenarios"
  - **Notes**: Mock StockDao, test all business logic scenarios

- [ ] [P] **T_API002_007**: Create StockConcurrencyTest
  - **Purpose**: Test concurrent stock update scenarios
  - **Target**: StockConcurrencyTest.java
  - **Reference SPEC**: [behaviors.md](../specs/baseline/api/API_002_stocks/behaviors.md) "Concurrency Scenarios"
  - **Notes**: Test that optimistic locking prevents lost updates

- [ ] **T_API002_008**: Create StockApiIntegrationTest
  - **Purpose**: Integration test for stock REST API
  - **Target**: StockApiIntegrationTest.java
  - **Reference SPEC**: 
    - [functional_design.md](../specs/baseline/api/API_002_stocks/functional_design.md) "3. Endpoint Specifications"
    - [behaviors.md](../specs/baseline/api/API_002_stocks/behaviors.md) "Stock API Scenarios"
  - **Notes**: Use REST Assured, test full API flow including 409 Conflict, add @Tag("integration")

---

## Completion Criteria

- [ ] Stock entity has @Version annotation
- [ ] Optimistic locking correctly prevents lost updates
- [ ] OptimisticLockException returns HTTP 409 Conflict
- [ ] Unit tests achieve 80%+ coverage
- [ ] Concurrency tests pass
- [ ] Integration tests pass all scenarios
```

---

## Example 2: Book API with Dual Search Implementation

### Task File (API_001_books.md) - Excerpt

```markdown
### Data Access Layer - JPQL Implementation

- [ ] **T_API001_003**: Create BookDao (JPQL)
  - **Purpose**: Implement book search using JPQL
  - **Target**: BookDao.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_books/functional_design.md) "2.2 BookDao (JPQL)"
  - **Notes**: Dynamic JPQL query for title, category, publisher filtering

### Data Access Layer - Criteria API Implementation

- [ ] **T_API001_004**: Create BookDaoCriteria (Criteria API)
  - **Purpose**: Implement book search using Criteria API
  - **Target**: BookDaoCriteria.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_books/functional_design.md) "2.3 BookDaoCriteria"
  - **Notes**: Type-safe Criteria API implementation, same functionality as JPQL

### Business Logic Layer

- [ ] **T_API001_005**: Create BookService
  - **Purpose**: Implement business logic for book operations
  - **Target**: BookService.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_books/functional_design.md) "2.4 BookService"
  - **Notes**: Provide separate methods for JPQL and Criteria API search

### API Layer

- [ ] **T_API001_006**: Create BookResource with Dual Search Endpoints
  - **Purpose**: Implement REST API with both search implementations
  - **Target**: BookResource.java
  - **Reference SPEC**: [functional_design.md](../specs/baseline/api/API_001_books/functional_design.md) "3. Endpoint Specifications"
  - **Notes**: 
    - GET /books/search/jpql - uses JPQL implementation
    - GET /books/search/criteria - uses Criteria API implementation
```

---

## Example 3: Code Implementation Progress

### Input Parameters
```yaml
project_root: "projects/sdd/bookstore/back-office-api-sdd"
task_file: "projects/sdd/bookstore/back-office-api-sdd/tasks/API_002_stocks.md"
```

### Progress Report

```
=== Task Execution Progress ===

Task File: API_002_stocks.md
Total Tasks: 8
Completed: 5
In Progress: 1
Pending: 2

✅ T_API002_001: Create Stock Entity with @Version - COMPLETED
✅ T_API002_002: Create StockDao - COMPLETED
✅ T_API002_003: Create StockService - COMPLETED
✅ T_API002_004: Create StockResource - COMPLETED
✅ T_API002_005: Create OptimisticLockExceptionMapper - COMPLETED
🔄 T_API002_006: Create StockServiceTest (Unit) - IN PROGRESS
⏳ T_API002_007: Create StockConcurrencyTest - PENDING
⏳ T_API002_008: Create StockApiIntegrationTest - PENDING

=== Files Created ===
1. src/main/java/pro/kensait/backoffice/entity/Stock.java
   - @Version annotation for optimistic locking ✓
   - bookId, quantity, version fields ✓
   
2. src/main/java/pro/kensait/backoffice/dao/StockDao.java
   - findById(Integer bookId) ✓
   - update(Stock stock) with version check ✓
   
3. src/main/java/pro/kensait/backoffice/service/StockService.java
   - getStock(Integer bookId) ✓
   - updateStock(Integer bookId, Integer quantity, int version) ✓
   - Handles OptimisticLockException ✓
   
4. src/main/java/pro/kensait/backoffice/api/StockResource.java
   - GET /stocks/{bookId} ✓
   - PUT /stocks/{bookId} ✓
   - Returns 409 Conflict on version mismatch ✓
   
5. src/main/java/pro/kensait/backoffice/exception/OptimisticLockExceptionMapper.java
   - Maps to HTTP 409 with error message ✓

=== Current Status ===
Implementing unit tests for StockService. All production code is complete and
follows specifications. Tests will verify stock retrieval, update, and optimistic
locking behavior with mocked StockDao.

=== Next Steps ===
1. Complete T_API002_006 (StockServiceTest)
2. Proceed to T_API002_007 (StockConcurrencyTest) - Critical for validating optimistic locking
3. Finish with T_API002_008 (StockApiIntegrationTest)

All implementations follow microservice pattern and use specified technical stack
(Jakarta EE 10, JPA 3.1 with @Version annotation).
```

---

## Example 4: Dual Search Implementation Comparison

### Generated Code - JPQL (BookDao.java)

```java
@ApplicationScoped
public class BookDao {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        StringBuilder jpql = new StringBuilder(
            "SELECT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE 1=1"
        );
        
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND b.title LIKE :keyword");
        }
        if (categoryId != null) {
            jpql.append(" AND b.category.id = :categoryId");
        }
        if (publisherId != null) {
            jpql.append(" AND b.publisher.id = :publisherId");
        }
        
        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        
        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (publisherId != null) {
            query.setParameter("publisherId", publisherId);
        }
        
        return query.getResultList();
    }
}
```

### Generated Code - Criteria API (BookDaoCriteria.java)

```java
@ApplicationScoped
public class BookDaoCriteria {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);
        
        // Fetch associations
        book.fetch("category", JoinType.LEFT);
        book.fetch("publisher", JoinType.LEFT);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.like(book.get("title"), "%" + keyword + "%"));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(book.get("category").get("id"), categoryId));
        }
        if (publisherId != null) {
            predicates.add(cb.equal(book.get("publisher").get("id"), publisherId));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);
        
        return em.createQuery(cq).getResultList();
    }
}
```

**Both implementations produce identical results!**

---

## Example 5: Verification Report

### Completion Verification Output

```
=== Completion Verification Report ===

Project: back-office-api
Task File: API_002_stocks.md
Date: 2025-01-04

## Charter Compliance
✅ Development principles adhered to
✅ Test coverage standards met (87% for service/API layer)
✅ Coding conventions followed
✅ Concurrency control requirements satisfied

## Task Completion
✅ All 8 tasks completed
✅ All files created as specified
✅ No pending or blocked tasks

## Specification Traceability
✅ All endpoints in functional_design.md implemented:
   - GET /stocks/{bookId}
   - PUT /stocks/{bookId}

✅ All scenarios in behaviors.md tested:
   - Stock retrieval
   - Stock update (successful)
   - Concurrent updates (optimistic lock conflict)
   - Version mismatch handling

✅ Architecture design requirements met:
   - @Version annotation on Stock entity
   - OptimisticLockException handling
   - HTTP 409 Conflict response
   - Proper error messages

## Test Results
✅ Unit Tests: 15 passed, 0 failed (Coverage: 87%)
✅ Concurrency Tests: 5 passed, 0 failed
   - Verified lost update prevention ✓
   - Verified version increment ✓
   - Verified exception handling ✓
✅ Integration Tests: 8 passed, 0 failed
✅ Linter: No errors or warnings

## Microservice Pattern Compliance
✅ Stock entity with @Version annotation
✅ Optimistic locking correctly implemented
✅ HTTP 409 returned on version conflict
✅ CORS enabled for BFF access
✅ Proper REST API design

## Files Created (5 production + 3 test)
Production:
1. Stock.java (@Version annotation)
2. StockDao.java
3. StockService.java
4. StockResource.java
5. OptimisticLockExceptionMapper.java

Tests:
1. StockServiceTest.java
2. StockConcurrencyTest.java
3. StockApiIntegrationTest.java

## Critical Feature Validation
✅ Optimistic Locking Works: Concurrent updates properly detected
✅ Version Conflict Returns 409: Correct HTTP status code
✅ Error Messages Clear: Helpful for BFF retry logic

## Summary
Stock API implementation is COMPLETE and ready for integration with BFF.
Optimistic locking is correctly implemented and thoroughly tested.
All acceptance criteria met. No blockers or issues.

Recommended next task: API_001_books.md (can be executed in parallel)
```

---

## Notes

These examples demonstrate:
1. Microservice pattern with all entities managed
2. Optimistic locking implementation for Stock
3. Dual search implementations (JPQL and Criteria API)
4. Comprehensive testing including concurrency tests
5. Proper HTTP status codes (especially 409 Conflict)

Actual outputs will vary based on:
- Project specifications
- AI platform being used
- Specific requirements
- Team preferences

