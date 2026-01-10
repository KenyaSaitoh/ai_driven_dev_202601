package pro.kensait.backoffice.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.backoffice.exception.InvalidWorkflowStateException;
import pro.kensait.backoffice.exception.UnauthorizedApprovalException;
import pro.kensait.backoffice.exception.WorkflowNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ワークフロー例外マッパー
 * 
 * ワークフロー関連の例外をHTTPレスポンスに変換する
 */
@Provider
public class WorkflowExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        // WorkflowNotFoundException → 404 Not Found
        if (exception instanceof WorkflowNotFoundException) {
            logger.warn("[WorkflowExceptionMapper] Workflow not found: {}", exception.getMessage());
            return createErrorResponse(Response.Status.NOT_FOUND, 
                                      "WorkflowNotFoundException", 
                                      exception.getMessage());
        }

        // InvalidWorkflowStateException → 400 Bad Request
        if (exception instanceof InvalidWorkflowStateException) {
            logger.warn("[WorkflowExceptionMapper] Invalid workflow state: {}", exception.getMessage());
            return createErrorResponse(Response.Status.BAD_REQUEST, 
                                      "InvalidWorkflowStateException", 
                                      exception.getMessage());
        }

        // UnauthorizedApprovalException → 403 Forbidden
        if (exception instanceof UnauthorizedApprovalException) {
            logger.warn("[WorkflowExceptionMapper] Unauthorized approval: {}", exception.getMessage());
            return createErrorResponse(Response.Status.FORBIDDEN, 
                                      "UnauthorizedApprovalException", 
                                      exception.getMessage());
        }

        // 上記以外の例外は処理しない（他のExceptionMapperに委譲）
        return null;
    }

    /**
     * エラーレスポンスを作成
     * 
     * @param status HTTPステータス
     * @param error エラータイプ
     * @param message エラーメッセージ
     * @return HTTPレスポンス
     */
    private Response createErrorResponse(Response.Status status, String error, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);

        return Response.status(status)
                       .entity(errorResponse)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
