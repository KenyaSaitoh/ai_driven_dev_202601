package pro.kensait.berrybooks.api.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * 楽観的ロック競合例外マッパー
 * 
 * OptimisticLockException → 409 Conflict
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(OptimisticLockException exception) {
        logger.warn("[ OptimisticLockExceptionMapper#toResponse ] Optimistic lock conflict: {}", 
                   exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            409,
            "Conflict",
            "他のユーザーが購入済みです。最新の在庫情報を確認してください。",
            "/" + uriInfo.getPath()
        );
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}

