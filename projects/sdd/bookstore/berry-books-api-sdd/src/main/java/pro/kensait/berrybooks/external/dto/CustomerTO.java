package pro.kensait.berrybooks.external.dto;

import java.time.LocalDate;

/**
 * 顧客転送オブジェクト
 * 
 * 外部API（berry-books-rest）とのデータ転送に使用
 * Java Recordを使用してイミュータブルなDTOを実装
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

