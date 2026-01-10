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
import pro.kensait.berrybooks.service.exception.OutOfStockException;

/**
 * OutOfStockException → 409 Conflict
 * 
 * 在庫不足例外をHTTP 409 Conflictにマッピングする。
 */
@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {

    private static final Logger logger = LoggerFactory.getLogger(OutOfStockExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(OutOfStockException exception) {
        String path = (uriInfo != null) ? uriInfo.getPath() : "";
        logger.warn("[ OutOfStockExceptionMapper ] bookId={}, bookName={}, path={}", 
                    exception.getBookId(), exception.getBookName(), path);

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
