package pro.kensait.berrybooks.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * Exception → 500 Internal Server Error
 * 
 * その他の例外をHTTP 500 Internal Server Errorにマッピングする。
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        String path = (uriInfo != null) ? uriInfo.getPath() : "";
        logger.error("[ GenericExceptionMapper ] Unhandled exception at path: " + path, exception);

        ErrorResponse error = new ErrorResponse(
            500,
            "Internal Server Error",
            "サーバーエラーが発生しました",
            "/" + path
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
