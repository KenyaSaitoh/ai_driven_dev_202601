package pro.kensait.berrybooks.api.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

import java.util.stream.Collectors;

/**
 * Bean Validation例外マッパー
 * 
 * ConstraintViolationException → 400 Bad Request
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);
    
    @Context
    private UriInfo uriInfo;
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        logger.warn("[ ValidationExceptionMapper#toResponse ] Validation error: {}", 
                   exception.getMessage());
        
        // バリデーションエラーメッセージを収集
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            "Bad Request",
            message,
            "/" + uriInfo.getPath()
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}

