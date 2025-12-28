package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.service.order.OutOfStockException;

/**
 * 在庫不足例外マッパー
 * 
 * OutOfStockException → 409 Conflict
 */
@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutOfStockExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(OutOfStockException exception) {
        logger.warn("[ OutOfStockExceptionMapper#toResponse ] Out of stock: bookId={}, bookName={}", 
                   exception.getBookId(), exception.getBookName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            409,
            "Conflict",
            exception.getMessage(),
            "/" + uriInfo.getPath()
        );
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}

