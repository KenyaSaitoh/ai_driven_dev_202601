package pro.kensait.berrybooks.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.security.AuthenticatedUser;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文ビジネスロジックサービス
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
    
    @Inject
    private DeliveryFeeService deliveryFeeService;
    
    @Inject
    private AuthenticatedUser authenticatedUser;
    
    /**
     * 注文を作成する
     * 
     * @param orderRequest 注文リクエスト
     * @return 作成された注文トランザクション
     */
    @Transactional
    public OrderTran createOrder(OrderRequest orderRequest) {
        logger.info("[ OrderService#createOrder ] customerId={}, itemCount={}", 
            authenticatedUser.getCustomerId(), orderRequest.cartItems().size());
        
        // 1. 在庫確認
        for (CartItemRequest item : orderRequest.cartItems()) {
            StockTO stock = backOfficeClient.findStockById(item.bookId());
            if (stock.getQuantity() < item.count()) {
                logger.error("[ OrderService#createOrder ] Out of stock: bookId={}, available={}, requested={}",
                    item.bookId(), stock.getQuantity(), item.count());
                throw new RuntimeException("在庫不足: " + item.bookName());
            }
        }
        
        // 2. 購入金額の計算
        int totalAmount = orderRequest.cartItems().stream()
            .mapToInt(item -> item.price() * item.count())
            .sum();
        
        // 3. 配送料金の計算
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, orderRequest.deliveryAddress());
        
        // 4. 注文トランザクションの作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(authenticatedUser.getCustomerId());
        orderTran.setTotalPrice(totalAmount + deliveryFee);
        orderTran.setDeliveryPrice(deliveryFee);
        orderTran.setDeliveryAddress(orderRequest.deliveryAddress());
        orderTran.setSettlementType(orderRequest.settlementType());
        
        orderTran = orderTranDao.insert(orderTran);
        logger.debug("[ OrderService#createOrder ] OrderTran created: orderTranId={}", orderTran.getOrderTranId());
        
        // 5. 注文明細の作成（スナップショット保存）
        int detailId = 1;
        for (CartItemRequest item : orderRequest.cartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderTranId(orderTran.getOrderTranId());
            orderDetail.setOrderDetailId(detailId++);
            orderDetail.setBookId(item.bookId());
            orderDetail.setBookName(item.bookName());
            orderDetail.setPublisherName(item.publisherName());
            orderDetail.setPrice(item.price());
            orderDetail.setCount(item.count());
            
            orderDetailDao.insert(orderDetail);
            orderTran.addOrderDetail(orderDetail);
        }
        
        // 6. 在庫更新
        for (CartItemRequest item : orderRequest.cartItems()) {
            StockTO stock = backOfficeClient.findStockById(item.bookId());
            int newQuantity = stock.getQuantity() - item.count();
            backOfficeClient.updateStock(item.bookId(), item.version(), newQuantity);
            logger.debug("[ OrderService#createOrder ] Stock updated: bookId={}, newQuantity={}",
                item.bookId(), newQuantity);
        }
        
        logger.info("[ OrderService#createOrder ] Order created successfully: orderTranId={}", 
            orderTran.getOrderTranId());
        return orderTran;
    }
    
    /**
     * 顧客の注文履歴を取得する
     * 
     * @param customerId 顧客ID
     * @return 注文トランザクションのリスト
     */
    public List<OrderTran> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#getOrderHistory ] customerId={}", customerId);
        return orderTranDao.findByCustomerId(customerId);
    }
    
    /**
     * 注文詳細を取得する
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文トランザクション
     */
    public OrderTran getOrderDetail(Integer orderTranId) {
        logger.info("[ OrderService#getOrderDetail ] orderTranId={}", orderTranId);
        return orderTranDao.findById(orderTranId);
    }
}