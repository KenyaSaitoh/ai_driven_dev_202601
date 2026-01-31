package pro.kensait.backoffice.service.workflow;

/**
 * 無効なワークフロー状態遷移の例外
 */
public class InvalidWorkflowStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidWorkflowStateException(String message) {
        super(message);
    }

    public InvalidWorkflowStateException(String message, Throwable cause) {
        super(message, cause);
    }
}


