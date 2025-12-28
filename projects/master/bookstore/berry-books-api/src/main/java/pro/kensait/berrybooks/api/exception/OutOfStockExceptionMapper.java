package pro.kensait.berrybooks.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.service.order.OutOfStockException;

/**
 * 在庫不足例外マッパー
 */
@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {
    private static final Logger logger = LoggerFactory.getLogger(OutOfStockExceptionMapper.class);

    @Override
    public Response toResponse(OutOfStockException exception) {
        logger.warn("[ OutOfStockExceptionMapper ] Out of stock: {}", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Out of Stock",
                "「" + exception.getBookName() + "」の在庫が不足しています",
                ""
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}

