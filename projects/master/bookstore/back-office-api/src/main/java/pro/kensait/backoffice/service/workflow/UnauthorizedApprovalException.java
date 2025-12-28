package pro.kensait.backoffice.service.workflow;

/**
 * 承認権限なしの例外
 */
public class UnauthorizedApprovalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedApprovalException(String message) {
        super(message);
    }

    public UnauthorizedApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}


