package pro.kensait.berrybooks.external.dto;

import java.time.LocalDate;

/**
 * 顧客情報転送用DTO（Transfer Object）
 * 
 * customer-hub-apiとのデータ転送に使用する。
 */
public record CustomerTO(
    Integer customerId,
    String customerName,
    String password,
    String email,
    LocalDate birthday,
    String address
) {
}
