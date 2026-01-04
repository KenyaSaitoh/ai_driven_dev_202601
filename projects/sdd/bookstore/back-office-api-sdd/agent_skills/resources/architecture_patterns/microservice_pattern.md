# Microservice Pattern (back-office-api)

## Overview

The back-office-api is a microservice that provides data management capabilities for books, inventory, categories, and publishers. It follows the microservices architecture pattern and serves as a backend service called by BFF (Backend for Frontend).

## Key Characteristics

### 1. Independent Data Management
- Manages its own database
- Complete CRUD operations for Book, Stock, Category, Publisher entities
- Full control over data lifecycle

### 2. REST API Provider
- Exposes REST API endpoints for data access
- Called by BFF (berry-books-api)
- CORS-enabled for cross-origin requests
- Standard HTTP status codes and error responses

### 3. Optimistic Locking for Concurrency Control
- Uses `@Version` annotation for Stock entity
- Prevents lost updates in concurrent inventory modifications
- Returns HTTP 409 Conflict on version mismatch

## Architecture Components

```
back-office-api (Microservice)
├── api/ (JAX-RS Resources)
│   ├── BookResource
│   ├── StockResource
│   ├── CategoryResource
│   └── PublisherResource
├── service/ (Business Logic)
│   ├── BookService
│   ├── StockService
│   ├── CategoryService
│   └── PublisherService
├── dao/ (Data Access)
│   ├── BookDao (JPQL)
│   ├── BookDaoCriteria (Criteria API)
│   ├── StockDao
│   ├── CategoryDao
│   └── PublisherDao
├── entity/ (JPA Entities)
│   ├── Book
│   ├── Stock (@Version for optimistic locking)
│   ├── Category
│   └── Publisher
└── filter/ (Cross-cutting Concerns)
    ├── CorsFilter
    └── ExceptionMappers
```

## Optimistic Locking Implementation

### Why Optimistic Locking?

Inventory (Stock) management requires handling concurrent updates:
- Multiple orders might try to update the same stock simultaneously
- Without locking, last write wins (lost update problem)
- Optimistic locking detects conflicts and prevents data inconsistency

### Implementation

**Entity with @Version:**
```java
@Entity
@Table(name = "STOCK")
public class Stock {
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;
    
    @Column(name = "QUANTITY")
    private Integer quantity;
    
    @Version  // Optimistic locking
    @Column(name = "VERSION")
    private int version;
    
    // getters, setters
}
```

**Update Process:**
1. Read stock with current version
2. Modify quantity
3. Attempt update with version check
4. If version mismatch → OptimisticLockException
5. Return HTTP 409 Conflict to caller

**Exception Handling:**
```java
@Provider
public class OptimisticLockExceptionMapper 
    implements ExceptionMapper<OptimisticLockException> {
    
    @Override
    public Response toResponse(OptimisticLockException exception) {
        return Response
            .status(Response.Status.CONFLICT)
            .entity(new ErrorResponse("Stock has been modified by another transaction"))
            .build();
    }
}
```

**BFF Handling:**
- BFF receives 409 Conflict
- Retries stock update (re-read current version)
- If retry fails, returns error to frontend

## Dual Search Implementation

### Why Two Implementations?

The back-office-api provides **two different implementations** of the same search functionality:

1. **JPQL (Java Persistence Query Language)** - `BookDao`
2. **Criteria API** - `BookDaoCriteria`

This allows:
- Learning different JPA query approaches
- Choosing the best approach for specific needs
- Demonstrating query flexibility

### JPQL Implementation (`BookDao`)

**Advantages:**
- Readable, SQL-like syntax
- Easy to write and understand
- Good for simple to moderate complexity queries

**Example:**
```java
@ApplicationScoped
public class BookDao {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        StringBuilder jpql = new StringBuilder(
            "SELECT b FROM Book b WHERE 1=1"
        );
        
        if (keyword != null) {
            jpql.append(" AND b.title LIKE :keyword");
        }
        if (categoryId != null) {
            jpql.append(" AND b.category.id = :categoryId");
        }
        if (publisherId != null) {
            jpql.append(" AND b.publisher.id = :publisherId");
        }
        
        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        
        if (keyword != null) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (publisherId != null) {
            query.setParameter("publisherId", publisherId");
        }
        
        return query.getResultList();
    }
}
```

### Criteria API Implementation (`BookDaoCriteria`)

**Advantages:**
- Type-safe (compile-time checking)
- Refactoring-friendly
- Good for complex dynamic queries

**Example:**
```java
@ApplicationScoped
public class BookDaoCriteria {
    @Inject
    private EntityManager em;
    
    public List<Book> searchBooks(String keyword, Integer categoryId, Integer publisherId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (keyword != null) {
            predicates.add(cb.like(book.get("title"), "%" + keyword + "%"));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(book.get("category").get("id"), categoryId));
        }
        if (publisherId != null) {
            predicates.add(cb.equal(book.get("publisher").get("id"), publisherId));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        
        return em.createQuery(cq).getResultList();
    }
}
```

### Service Layer Choice

The `BookService` can use either implementation:

```java
@ApplicationScoped
public class BookService {
    @Inject
    private BookDao bookDao;  // JPQL implementation
    
    @Inject
    private BookDaoCriteria bookDaoCriteria;  // Criteria API implementation
    
    public List<Book> searchBooksJpql(String keyword, Integer categoryId, Integer publisherId) {
        return bookDao.searchBooks(keyword, categoryId, publisherId);
    }
    
    public List<Book> searchBooksCriteria(String keyword, Integer categoryId, Integer publisherId) {
        return bookDaoCriteria.searchBooks(keyword, categoryId, publisherId);
    }
}
```

### Exposed as Separate Endpoints

```java
@Path("/books")
@ApplicationScoped
public class BookResource {
    @Inject
    private BookService bookService;
    
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
        @QueryParam("keyword") String keyword,
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("publisherId") Integer publisherId
    ) {
        List<Book> books = bookService.searchBooksJpql(keyword, categoryId, publisherId);
        return Response.ok(books).build();
    }
    
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
        @QueryParam("keyword") String keyword,
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("publisherId") Integer publisherId
    ) {
        List<Book> books = bookService.searchBooksCriteria(keyword, categoryId, publisherId);
        return Response.ok(books).build();
    }
}
```

## Microservice Integration

### Called by BFF

```
Frontend → BFF (berry-books-api) → back-office-api
```

**Example Flow (Order Creation):**
1. Frontend sends order request to BFF
2. BFF calls back-office-api to check stock availability
3. BFF calls back-office-api to update stock (with optimistic locking)
4. If 409 Conflict, BFF retries
5. BFF creates order in its own database
6. BFF returns response to frontend

### CORS Configuration

Since BFF and back-office-api are separate services:

```java
@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Content-Type, Authorization");
    }
}
```

## Data Model

### Entity Relationships

```
Publisher (1) ──── (*) Book (*) ──── (1) Category
                          │
                          │ (1)
                          │
                          (*) 
                        Stock (@Version)
```

### All Entities Managed by back-office-api

- ✅ **Book**: Title, ISBN, price, publication date
- ✅ **Stock**: Quantity, version (optimistic locking)
- ✅ **Category**: Name, description
- ✅ **Publisher**: Name, address

**Compare with BFF:**
- BFF manages only Order-related entities
- back-office-api manages all book-related data

## Implementation Guidelines

### DO Implement
- ✅ All entities (Book, Stock, Category, Publisher)
- ✅ All DAOs for data access
- ✅ All Services for business logic
- ✅ All Resources for REST endpoints
- ✅ Optimistic locking for Stock
- ✅ Dual search implementations (JPQL & Criteria API)
- ✅ CORS filter
- ✅ Exception mappers
- ✅ Comprehensive tests (including concurrency tests)

### Key Implementation Points

1. **Stock Entity Must Have @Version**
2. **Provide Both JPQL and Criteria API Search**
3. **Handle OptimisticLockException Properly**
4. **Return HTTP 409 on Version Conflict**
5. **Enable CORS for BFF Access**
6. **Implement Proper Error Responses**

## Testing Strategy

### Unit Tests
- Test business logic in Services
- Mock DAOs
- Test optimistic locking scenarios

### Integration Tests
- Test REST endpoints
- Test actual database operations
- **Test concurrent stock updates** (important!)
- Test JPQL and Criteria API search results match

### Concurrency Test Example

```java
@Test
void testConcurrentStockUpdate() {
    // Thread 1 updates stock
    // Thread 2 updates same stock simultaneously
    // Verify one succeeds, one gets OptimisticLockException
}
```

## Best Practices

1. **Always Use @Version for Stock**: Inventory data requires concurrency control
2. **Test Both Search Implementations**: Ensure JPQL and Criteria API return same results
3. **Handle 409 Gracefully**: Provide clear error messages for version conflicts
4. **Document API Endpoints**: Clearly indicate which endpoints use which search method
5. **Monitor Performance**: Compare JPQL vs Criteria API performance
6. **Use Proper HTTP Status Codes**: 200 OK, 404 Not Found, 409 Conflict, etc.

## References

- Jakarta EE 10 Documentation
- JPA 3.1 Specification
- Optimistic Locking Best Practices
- Criteria API Guide

