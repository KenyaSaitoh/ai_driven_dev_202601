package pro.kensait.berrybooks.external.dto;

import java.time.LocalDate;

/**
 * 顧客情報転送オブジェクト（外部APIレスポンス・リクエスト）
 */
public record CustomerTO(
    Integer customerId,
    String customerName,
    String password,
    String email,
    LocalDate birthday,
    String address
) {}
