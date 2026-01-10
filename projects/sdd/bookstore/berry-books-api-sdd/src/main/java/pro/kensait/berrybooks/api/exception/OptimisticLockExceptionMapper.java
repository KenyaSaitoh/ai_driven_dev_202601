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
import pro.kensait.berrybooks.service.exception.OptimisticLockException;

/**
 * OptimisticLockException → 409 Conflict
 * 
 * 楽観的ロック競合例外をHTTP 409 Conflictにマッピングする。
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(OptimisticLockException exception) {
        String path = (uriInfo != null) ? uriInfo.getPath() : "";
        logger.warn("[ OptimisticLockExceptionMapper ] message={}, path={}", 
                    exception.getMessage(), path);

        ErrorResponse error = new ErrorResponse(
            409,
            "Conflict",
            exception.getMessage(),
            "/" + path
        );

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
