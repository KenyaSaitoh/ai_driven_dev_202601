package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;

/**
 * ログインレスポンスDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record LoginResponse(
    Integer customerId,
    String customerName,
    String email,
    LocalDate birthday,
    String address
) {
}

