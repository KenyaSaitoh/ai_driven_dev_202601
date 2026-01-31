package pro.kensait.backoffice.api.dto;

/**
 * エラーレスポンスDTO
 */
public class ErrorResponse {
    private String error;
    private String message;

    // デフォルトコンストラクタ
    public ErrorResponse() {
    }

    // コンストラクタ
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    // シンプルコンストラクタ
    public ErrorResponse(String message) {
        this.error = "Error";
        this.message = message;
    }

    // Getter/Setter
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorResponse [error=" + error + ", message=" + message + "]";
    }
}

