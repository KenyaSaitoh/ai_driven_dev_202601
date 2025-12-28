package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * 汎用例外マッパー
 * 
 * Exception → 500 Internal Server Error
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        logger.error("[ GenericExceptionMapper#toResponse ] Unexpected error occurred", exception);
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            "Internal Server Error",
            "サーバーエラーが発生しました",
            "/" + uriInfo.getPath()
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}

