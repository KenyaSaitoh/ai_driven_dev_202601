package pro.kensait.berrybooks.client.dto;

import java.time.LocalDate;

/**
 * Customer Transfer Object (REST API用のDTO)
 * berry-books-rest APIとの通信に使用
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

