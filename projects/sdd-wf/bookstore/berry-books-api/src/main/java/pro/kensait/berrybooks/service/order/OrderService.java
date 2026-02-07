package pro.kensait.berrybooks.service.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.common.exception.OutOfStockException;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文処理サービス
 * 
 * トランザクション境界: このクラスのメソッド全体
 * 処理フロー:
 * 1. 在庫確認（外部API）
 * 2. 在庫更新（外部API、楽観的ロック対応）
 * 3. 注文トランザクション作成（ローカルDB）
 * 4. 注文明細作成（ローカルDB）
 */
@ApplicationScoped
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Inject
    private OrderTranDao orderTranDao;
    
    @Inject
    private OrderDetailDao orderDetailDao;
    
    @Inject
    private BackOfficeRestClient backOfficeClient;
    
    /**
     * 注文を作成する
     * 
     * @param request 注文リクエスト
     * @param customerId 顧客ID
     * @return 作成された注文トランザクション
     * @throws OutOfStockException 在庫不足の場合
     * @throws OptimisticLockException 楽観的ロック競合の場合
     */
    @Transactional
    public OrderTran createOrder(OrderRequest request, Integer customerId) 
            throws OutOfStockException, OptimisticLockException {
        
        logger.info("[ OrderService#createOrder ] START: customerId={}, cartItemsCount={}", 
                    customerId, request.cartItems().size());
        
        try {
            // 1. カートアイテムごとに在庫確認と更新
            for (CartItemRequest cartItem : request.cartItems()) {
                logger.info("[ OrderService#createOrder ] Checking stock: bookId={}, requestedCount={}", 
                            cartItem.bookId(), cartItem.count());
                
                // 在庫情報を取得
                StockTO stock = backOfficeClient.findStockById(cartItem.bookId());
                
                // 在庫チェック
                if (stock.quantity() < cartItem.count()) {
                    logger.warn("[ OrderService#createOrder ] Out of stock: bookId={}, available={}, requested={}", 
                                cartItem.bookId(), stock.quantity(), cartItem.count());
                    throw new OutOfStockException(
                        String.format("書籍ID %d の在庫が不足しています（在庫: %d, 注文数: %d）", 
                                      cartItem.bookId(), stock.quantity(), cartItem.count()));
                }
                
                // 在庫更新（楽観的ロック対応）
                int newQuantity = stock.quantity() - cartItem.count();
                logger.info("[ OrderService#createOrder ] Updating stock: bookId={}, newQuantity={}, version={}", 
                            cartItem.bookId(), newQuantity, cartItem.version());
                
                try {
                    backOfficeClient.updateStock(cartItem.bookId(), newQuantity, cartItem.version());
                    logger.info("[ OrderService#createOrder ] Stock updated successfully: bookId={}", 
                                cartItem.bookId());
                } catch (WebApplicationException e) {
                    if (e.getResponse().getStatus() == 409) {
                        logger.error("[ OrderService#createOrder ] Optimistic lock conflict: bookId={}", 
                                     cartItem.bookId());
                        throw new OptimisticLockException("在庫データが他のユーザーによって更新されました");
                    }
                    throw e;
                }
            }
            
            // 2. 注文トランザクション作成
            OrderTran orderTran = new OrderTran();
            orderTran.setOrderDate(LocalDate.now());
            orderTran.setCustomerId(customerId);
            orderTran.setTotalPrice(request.totalPrice());
            orderTran.setDeliveryPrice(request.deliveryPrice());
            orderTran.setDeliveryAddress(request.deliveryAddress());
            orderTran.setSettlementType(request.settlementType());
            
            orderTran = orderTranDao.insert(orderTran);
            logger.info("[ OrderService#createOrder ] OrderTran created: orderTranId={}", 
                        orderTran.getOrderTranId());
            
            // 3. 注文明細作成
            List<OrderDetail> orderDetails = new ArrayList<>();
            int orderDetailId = 1;
            
            for (CartItemRequest cartItem : request.cartItems()) {
                OrderDetailPK pk = new OrderDetailPK(orderTran.getOrderTranId(), orderDetailId);
                
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(pk);
                orderDetail.setOrderTran(orderTran);
                orderDetail.setBookId(cartItem.bookId());
                orderDetail.setBookName(cartItem.bookName());
                orderDetail.setPublisherName(cartItem.publisherName());
                orderDetail.setPrice(cartItem.price());
                orderDetail.setCount(cartItem.count());
                
                orderDetail = orderDetailDao.insert(orderDetail);
                orderDetails.add(orderDetail);
                
                logger.info("[ OrderService#createOrder ] OrderDetail created: orderTranId={}, orderDetailId={}, bookId={}", 
                            orderTran.getOrderTranId(), orderDetailId, cartItem.bookId());
                
                orderDetailId++;
            }
            
            orderTran.setOrderDetails(orderDetails);
            
            logger.info("[ OrderService#createOrder ] END: orderTranId={}", 
                        orderTran.getOrderTranId());
            
            return orderTran;
            
        } catch (OutOfStockException | OptimisticLockException e) {
            logger.error("[ OrderService#createOrder ] Business error: {}", e.getMessage());
            throw e;
        } catch (WebApplicationException e) {
            logger.error("[ OrderService#createOrder ] External API error: {}", e.getMessage());
            throw new RuntimeException("外部API呼び出し中にエラーが発生しました", e);
        } catch (RuntimeException e) {
            logger.error("[ OrderService#createOrder ] Unexpected error", e);
            throw new RuntimeException("注文作成中にエラーが発生しました", e);
        }
    }
    
    /**
     * 顧客の注文履歴を取得する
     * 
     * @param customerId 顧客ID
     * @return 注文トランザクションのリスト（注文日の降順）
     */
    public List<OrderTran> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#getOrderHistory ] customerId={}", customerId);
        
        List<OrderTran> orderHistory = orderTranDao.findByCustomerId(customerId);
        
        logger.info("[ OrderService#getOrderHistory ] Found {} orders", orderHistory.size());
        
        return orderHistory;
    }
    
    /**
     * 注文IDで注文詳細を取得する
     * 
     * @param tranId 注文トランザクションID
     * @return 注文トランザクション（注文明細を含む）、存在しない場合はnull
     */
    public OrderTran getOrderById(Integer tranId) {
        logger.info("[ OrderService#getOrderById ] tranId={}", tranId);
        
        OrderTran orderTran = orderTranDao.findById(tranId).orElse(null);
        
        if (orderTran != null) {
            logger.info("[ OrderService#getOrderById ] Found order: orderTranId={}, detailsCount={}", 
                        orderTran.getOrderTranId(), orderTran.getOrderDetails().size());
        } else {
            logger.warn("[ OrderService#getOrderById ] Order not found: tranId={}", tranId);
        }
        
        return orderTran;
    }
}
