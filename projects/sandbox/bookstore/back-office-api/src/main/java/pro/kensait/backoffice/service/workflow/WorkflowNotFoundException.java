package pro.kensait.backoffice.service.workflow;

/**
 * ワークフローが見つからない例外
 */
public class WorkflowNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WorkflowNotFoundException(String message) {
        super(message);
    }

    public WorkflowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


