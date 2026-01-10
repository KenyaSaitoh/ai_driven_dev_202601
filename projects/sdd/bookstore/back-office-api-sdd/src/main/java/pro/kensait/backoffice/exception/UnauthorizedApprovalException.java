package pro.kensait.backoffice.exception;

/**
 * 承認権限不足例外
 * 
 * 承認操作を実行する権限が不足している場合にスローされる
 */
public class UnauthorizedApprovalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedApprovalException(String message) {
        super(message);
    }

    public UnauthorizedApprovalException(Long employeeId, String reason) {
        super("承認権限がありません: employeeId=" + employeeId + ", reason=" + reason);
    }

    public UnauthorizedApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
