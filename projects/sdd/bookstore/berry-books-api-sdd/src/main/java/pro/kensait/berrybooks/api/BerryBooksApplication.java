package pro.kensait.berrybooks.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration for Berry Books API
 * 
 * This class activates JAX-RS and defines the base URI for all REST endpoints.
 * All resources will be accessible at: /api/*
 * 
 * Example:
 * - http://localhost:8080/berry-books-api-sdd/api/books
 * - http://localhost:8080/berry-books-api-sdd/api/auth/login
 * - http://localhost:8080/berry-books-api-sdd/api/orders
 * 
 * Note: No explicit resource registration is needed.
 * All classes annotated with @Path will be automatically discovered and registered.
 */
@ApplicationPath("/api")
public class BerryBooksApplication extends Application {
    // No additional configuration needed
    // JAX-RS will automatically scan and register all @Path annotated classes
}
