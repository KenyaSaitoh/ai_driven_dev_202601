# BFF (Backend for Frontend) Pattern

## Overview

The BFF (Backend for Frontend) pattern is an architectural pattern where a dedicated backend service is created for each frontend application or experience. This pattern is particularly useful when you need to aggregate data from multiple backend services and tailor the API to specific frontend needs.

## Key Characteristics

### 1. Single Entry Point
- Frontend application communicates with only one backend service (the BFF)
- Simplifies frontend code by hiding complexity of multiple backend services
- Provides a consistent API interface to the frontend

### 2. API Aggregation
- Combines data from multiple microservices
- Reduces number of round trips from frontend
- Optimizes data transfer for specific frontend requirements

### 3. Frontend-Specific Optimization
- Tailored responses for specific frontend needs (mobile vs. web)
- Reduced payload size by filtering unnecessary data
- Custom data transformations for frontend consumption

## Implementation Patterns in berry-books-api

### Proxy Pattern
Some resources act as transparent proxies to backend services:

**BookResource**:
```
Frontend → BFF (BookResource) → back-office-api
```
- No business logic in BFF
- Direct forwarding of requests/responses
- Translation of authentication tokens if needed

**Implementation**:
- Use REST client (JAX-RS Client API) to call backend
- Forward requests with minimal transformation
- Handle errors from backend and translate to frontend-friendly format

### Local Implementation Pattern
Some resources implement business logic in BFF layer:

**OrderResource**:
```
Frontend → BFF (OrderResource) → {
    - Local DB (OrderTran, OrderDetail)
    - back-office-api (Stock management)
    - Distributed transaction coordination
}
```
- Contains order processing business logic
- Manages local database tables
- Coordinates distributed operations

**Implementation**:
- Implement business logic in Service layer
- Use local JPA entities for data managed by BFF
- Use REST clients for external API calls
- Handle distributed transaction considerations

### Authentication Layer
JWT authentication is implemented in BFF:

**AuthenResource**:
```
Frontend → BFF (AuthenResource) → customer-hub-api
         ↓
    JWT Token Generation
         ↓
    HttpOnly Cookie
```
- Validates credentials via external API
- Generates JWT tokens locally
- Manages session state (stateless JWT)
- Provides centralized authentication for all BFF endpoints

## Data Management Strategy

### Local Data (BFF Manages)
- Order transactions (ORDER_TRAN)
- Order details (ORDER_DETAIL)
- JPA entities: `OrderTran`, `OrderDetail`, `OrderDetailPK`

### External Data (External APIs Manage)
- Books, Categories, Publishers → back-office-api
- Customers → customer-hub-api
- Stock → back-office-api
- **No local entities or DAOs for these**

## Component Structure

```
berry-books-api (BFF)
├── api/ (JAX-RS Resources)
│   ├── BookResource (Proxy)
│   ├── CategoryResource (Proxy)
│   ├── AuthenResource (Local + External)
│   ├── OrderResource (Local + External)
│   └── ImageResource (Local)
├── service/ (Business Logic)
│   ├── OrderService (Local implementation)
│   └── DeliveryFeeService (Local implementation)
├── dao/ (Data Access - Local entities only)
│   ├── OrderTranDao
│   └── OrderDetailDao
├── entity/ (Local entities only)
│   ├── OrderTran
│   ├── OrderDetail
│   └── OrderDetailPK
├── external/ (External API Clients)
│   ├── BackOfficeRestClient
│   ├── CustomerHubRestClient
│   └── dto/ (External API DTOs)
├── security/ (JWT Authentication)
│   ├── JwtUtil
│   ├── JwtAuthenFilter
│   └── AuthenContext
└── filter/ (Cross-cutting Concerns)
    └── CorsFilter
```

## Implementation Guidelines

### DO Implement
- ✅ Resources for all APIs (proxy or local)
- ✅ Services for local business logic
- ✅ DAOs for locally managed entities
- ✅ REST clients for external API integration
- ✅ JWT authentication infrastructure
- ✅ DTO classes for external API communication
- ✅ Error handling and exception mappers

### DO NOT Implement
- ❌ Entities for externally managed data (Book, Stock, Category, Customer)
- ❌ DAOs for externally managed entities
- ❌ Services for simple proxy operations
- ❌ Database tables for external data

## Transaction Management

### Local Transactions
- Use `@Transactional` for operations on local entities
- JTA transaction management via Jakarta Transactions
- Standard ACID guarantees for local database

### Distributed Transactions
When operations span multiple services:

1. **Eventual Consistency Approach**:
   - Execute external API calls first (e.g., inventory update)
   - Then execute local transaction (e.g., create order)
   - If local transaction fails, implement compensation logic

2. **Error Handling**:
   - Handle partial failures gracefully
   - Provide clear error messages to frontend
   - Consider idempotency for retry scenarios

Example (Order Creation):
```
1. Call back-office-api to reserve/update stock
   ↓ (success)
2. Create local order transaction
   ↓ (success)
3. Return success to frontend

If step 2 fails:
- Stock already updated (eventual consistency issue)
- Consider compensation: notify admin, implement reconciliation job
```

## Security Considerations

### JWT Authentication
- Generate JWT tokens in BFF layer
- Include necessary claims (customer ID, email, roles)
- Set HttpOnly cookie to prevent XSS attacks
- Validate JWT on every protected endpoint

### CORS Configuration
- Configure allowed origins for frontend
- Set appropriate headers for cross-origin requests
- Restrict methods and headers as needed

### External API Security
- Store external API credentials securely
- Use HTTPS for all external communications
- Implement timeout and retry policies
- Handle authentication failures gracefully

## Performance Optimization

### Caching Strategies
- Consider caching frequently accessed data from external APIs
- Use appropriate cache expiration policies
- Invalidate cache when data changes

### Parallel Requests
- Make independent external API calls in parallel
- Use CompletableFuture or reactive patterns
- Set appropriate timeouts

### Response Optimization
- Filter unnecessary fields from external API responses
- Combine multiple API calls into single frontend request
- Use pagination for large datasets

## Testing Strategy

### Unit Tests
- Mock external REST clients
- Test business logic in isolation
- Verify data transformations

### Integration Tests
- Test actual HTTP endpoints
- Use test instances of external APIs (if available)
- Verify end-to-end flows
- Test error scenarios (external API failures)

### Contract Tests
- Verify BFF adheres to frontend expectations
- Verify BFF correctly calls external APIs
- Use tools like Pact for consumer-driven contract testing

## Best Practices

1. **Keep BFF Thin**: Minimize business logic in BFF; delegate to appropriate services
2. **Frontend-Specific**: Tailor BFF to frontend needs, don't try to make it generic
3. **Version Management**: Version your BFF API independently from backend services
4. **Error Handling**: Provide consistent error responses across all endpoints
5. **Documentation**: Document which data comes from which backend service
6. **Monitoring**: Track performance of external API calls separately
7. **Circuit Breaker**: Implement circuit breaker pattern for external API failures

## References

- [BFF Pattern (Sam Newman)](https://samnewman.io/patterns/architectural/bff/)
- [Pattern: Backends For Frontends](https://microservices.io/patterns/apigateway.html)
- Jakarta EE 10 Documentation
- JAX-RS 3.1 Specification

