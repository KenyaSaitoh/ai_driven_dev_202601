package pro.kensait.berrybooks.client.dto;

/**
 * REST APIエラーレスポンス用のDTO
 */
public record ErrorResponse(
    int status,
    String error,
    String message
) {
}

