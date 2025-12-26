package pro.kensait.berrybooks.api.dto;

import java.util.List;

/**
 * エラーレスポンスDTO
 */
public record ErrorResponse(
    Integer status,
    String error,
    String message,
    String path,
    List<String> details
) {
    // 詳細なしのコンストラクタ
    public ErrorResponse(Integer status, String error, String message, String path) {
        this(status, error, message, path, null);
    }
}

