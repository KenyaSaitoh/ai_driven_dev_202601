package pro.kensait.berrybooks.api.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * ConstraintViolationException → 400 Bad Request
 * 
 * Bean Validation例外をHTTP 400 Bad Requestにマッピングする。
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String path = (uriInfo != null) ? uriInfo.getPath() : "";
        
        String violations = exception.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));

        logger.warn("[ ValidationExceptionMapper ] violations={}, path={}", violations, path);

        ErrorResponse error = new ErrorResponse(
            400,
            "Bad Request",
            violations,
            "/" + path
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
