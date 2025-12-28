package pro.kensait.berrybooks.external.dto;

/**
 * REST API用のエラーレスポンス
 * berry-books-rest APIのエラーレスポンスに使用
 */
public record ErrorResponse(
        String code,
        String message
        ) {
}




