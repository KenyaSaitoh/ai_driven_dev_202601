package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;
import pro.kensait.berrybooks.common.exception.OutOfStockException;

/**
 * OutOfStockException マッパー
 * 
 * OutOfStockExceptionを409 Conflictレスポンスにマッピングする。
 * 
 * @since 1.0.0
 */
@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutOfStockExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(OutOfStockException exception) {
        logger.warn("[ OutOfStockExceptionMapper ] {}", exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict",
            exception.getMessage(),
            uriInfo != null ? uriInfo.getPath() : ""
        );
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}
