package pro.kensait.backoffice.exception;

import pro.kensait.backoffice.common.WorkflowState;

/**
 * 不正ワークフロー状態例外
 * 
 * ワークフローの状態遷移が不正な場合にスローされる
 */
public class InvalidWorkflowStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidWorkflowStateException(String message) {
        super(message);
    }

    public InvalidWorkflowStateException(WorkflowState currentState, String operation) {
        super("現在の状態では操作できません: state=" + currentState + ", operation=" + operation);
    }

    public InvalidWorkflowStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
