package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;

/**
 * ログイン・ユーザー情報レスポンスDTO
 */
public record LoginResponse(
    Integer customerId,
    String customerName,
    String email,
    LocalDate birthday,
    String address
) {
}

