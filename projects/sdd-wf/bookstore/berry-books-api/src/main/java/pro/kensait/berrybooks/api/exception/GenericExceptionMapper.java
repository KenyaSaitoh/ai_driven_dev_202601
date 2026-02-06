package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

/**
 * 予期しない例外用Exception Mapper
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        logger.error("[ GenericExceptionMapper ] Unexpected error: {}", exception.getMessage(), exception);
        
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            "Internal Server Error",
            "システムエラーが発生しました。管理者に連絡してください。",
            path
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
