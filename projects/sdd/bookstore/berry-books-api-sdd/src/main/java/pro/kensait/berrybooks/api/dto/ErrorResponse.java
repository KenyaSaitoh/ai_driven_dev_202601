package pro.kensait.berrybooks.api.dto;

/**
 * 統一的なエラーレスポンス形式
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {
}

