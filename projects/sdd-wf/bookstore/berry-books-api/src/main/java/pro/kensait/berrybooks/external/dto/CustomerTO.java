package pro.kensait.berrybooks.external.dto;

import java.time.LocalDate;

/**
 * 顧客情報転送オブジェクト
 * 
 * customer-hub-apiからの顧客情報を転送する。
 * 
 * @param customerId 顧客ID
 * @param customerName 顧客名
 * @param password パスワード（BCryptハッシュ）
 * @param email メールアドレス
 * @param birthday 生年月日
 * @param address 住所
 * 
 * @since 1.0.0
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
