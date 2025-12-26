package pro.kensait.berrybooks.service.order;

import java.util.List;

import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;

// 注文サービスのインターフェース
public interface OrderServiceIF {
    List<OrderTran> getOrderHistory(Integer customerId);
    List<OrderHistoryTO> getOrderHistory2(Integer customerId);
    List<OrderTran> getOrderHistory3(Integer customerId);
    OrderTran getOrderTran(Integer tranId);
    OrderTran getOrderTranWithDetails(Integer tranId);
    OrderDetail getOrderDetail(OrderDetailPK pk);
    OrderDetail getOrderDetail(Integer tranId, Integer detailId);
    List<OrderDetail> getOrderDetails(Integer orderTranId);
    OrderTran orderBooks(OrderTO orderTO);
}