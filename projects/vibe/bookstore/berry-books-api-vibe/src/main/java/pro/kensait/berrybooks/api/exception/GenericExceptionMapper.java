package pro.kensait.berrybooks.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * 汎用例外マッパー
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("[ GenericExceptionMapper ] Unexpected error: {}", exception.getMessage(), exception);

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "予期しないエラーが発生しました",
                ""
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}

