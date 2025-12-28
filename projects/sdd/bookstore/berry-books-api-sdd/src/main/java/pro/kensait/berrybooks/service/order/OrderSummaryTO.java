package pro.kensait.berrybooks.service.order;

import java.time.LocalDate;

/**
 * 注文サマリーDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record OrderSummaryTO(
    Integer orderTranId,
    LocalDate orderDate,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType
) {
}

