package pro.kensait.berrybooks.api.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * バリデーション例外マッパー
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        logger.warn("[ ValidationExceptionMapper ] Validation error: {}", exception.getMessage());

        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.add(violation.getMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Validation Failed",
                "入力値に誤りがあります",
                "",
                errors
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}

