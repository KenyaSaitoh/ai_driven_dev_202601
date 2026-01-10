package pro.kensait.backoffice.exception;

/**
 * ワークフロー未検出例外
 * 
 * 指定されたワークフローIDが存在しない場合にスローされる
 */
public class WorkflowNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WorkflowNotFoundException(String message) {
        super(message);
    }

    public WorkflowNotFoundException(Long workflowId) {
        super("ワークフローが見つかりません: workflowId=" + workflowId);
    }

    public WorkflowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
