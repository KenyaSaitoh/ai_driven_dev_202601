package pro.kensait.customerhub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.customerhub.dto.ErrorResponse;

/**
 * 404 Not Found例外マッパー
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

    @Override
    public Response toResponse(NotFoundException exception) {
        logger.warn("[ NotFoundExceptionMapper ] Resource not found: {}", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "resource.not-found",
                "指定されたリソースが見つかりません"
        );

        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}

