package pro.kensait.backoffice.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.backoffice.api.dto.ErrorResponse;
import pro.kensait.backoffice.service.workflow.InvalidWorkflowStateException;
import pro.kensait.backoffice.service.workflow.UnauthorizedApprovalException;
import pro.kensait.backoffice.service.workflow.WorkflowNotFoundException;

/**
 * ワークフロー関連例外マッパー
 */
@Provider
public class WorkflowExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        logger.error("ワークフロー例外が発生しました", exception);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());

        if (exception instanceof WorkflowNotFoundException) {
            // 404 Not Found
            errorResponse.setError("Workflow Not Found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        } else if (exception instanceof InvalidWorkflowStateException) {
            // 400 Bad Request
            errorResponse.setError("Invalid Workflow State");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        } else if (exception instanceof UnauthorizedApprovalException) {
            // 403 Forbidden
            errorResponse.setError("Unauthorized Approval");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(errorResponse)
                    .build();
        } else if (exception instanceof IllegalArgumentException) {
            // 400 Bad Request
            errorResponse.setError("Bad Request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        // その他の例外は500 Internal Server Error
        errorResponse.setError("Internal Server Error");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}


