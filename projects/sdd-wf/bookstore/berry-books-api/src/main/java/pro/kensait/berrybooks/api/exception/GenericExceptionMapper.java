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
 * 汎用 Exception マッパー
 * 
 * 予期しない例外を500 Internal Server Errorレスポンスにマッピングする。
 * 
 * @since 1.0.0
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        logger.error("[ GenericExceptionMapper ] Unexpected error: {}", exception.getMessage(), exception);
        
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            "システムエラーが発生しました。",
            uriInfo != null ? uriInfo.getPath() : ""
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
