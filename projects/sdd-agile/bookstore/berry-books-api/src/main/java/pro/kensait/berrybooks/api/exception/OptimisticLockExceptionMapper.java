package pro.kensait.berrybooks.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * 楽観的ロック例外マッパー
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);

    @Override
    public Response toResponse(OptimisticLockException exception) {
        logger.warn("[ OptimisticLockExceptionMapper ] Optimistic lock error: {}", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                "他のユーザーによって更新されています。再度お試しください。",
                ""
        );

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}

