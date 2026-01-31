package pro.kensait.backoffice.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.backoffice.api.dto.ErrorResponse;

/**
 * 楽観的ロック例外マッパー
 * 
 * OptimisticLockExceptionを409 Conflictレスポンスに変換
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);

    @Override
    public Response toResponse(OptimisticLockException exception) {
        logger.warn("Optimistic lock conflict detected", exception);

        ErrorResponse error = new ErrorResponse(
            "OptimisticLockException",
            "データが他のユーザーによって更新されました。再度お試しください。"
        );

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .build();
    }
}

