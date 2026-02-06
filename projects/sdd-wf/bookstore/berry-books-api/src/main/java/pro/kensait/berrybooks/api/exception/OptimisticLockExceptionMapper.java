package pro.kensait.berrybooks.api.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

/**
 * OptimisticLockException用Exception Mapper
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(OptimisticLockException exception) {
        logger.warn("[ OptimisticLockExceptionMapper ] Optimistic lock conflict: {}", exception.getMessage());
        
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        
        ErrorResponse errorResponse = new ErrorResponse(
            409,
            "Conflict",
            "データが他のユーザーによって更新されました。再度お試しください。",
            path
        );
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}
