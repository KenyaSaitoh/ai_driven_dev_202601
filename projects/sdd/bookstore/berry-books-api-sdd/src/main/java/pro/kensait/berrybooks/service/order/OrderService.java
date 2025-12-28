package pro.kensait.berrybooks.service.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.dao.StockDao;
import pro.kensait.berrybooks.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文サービス
 * 
 * トランザクション境界、楽観的ロック制御、在庫管理を実装
 */
@ApplicationScoped
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Inject
    private StockDao stockDao;
    
    @Inject
    private OrderTranDao orderTranDao;
    
    @Inject
    private OrderDetailDao orderDetailDao;
    
    /**
     * 書籍を注文（トランザクション境界）
     * 
     * 処理フロー:
     * 1. 在庫可用性チェック
     * 2. 在庫更新（楽観的ロック）
     * 3. 注文トランザクション作成
     * 4. 注文明細作成
     * 
     * @param orderTO 注文転送オブジェクト
     * @return 注文サマリー
     * @throws OutOfStockException 在庫不足の場合
     * @throws OptimisticLockException 楽観的ロック競合の場合
     */
    @Transactional
    public OrderSummaryTO orderBooks(OrderTO orderTO) {
        logger.info("[ OrderService#orderBooks ] customerId={}, cartItems={}", 
                   orderTO.getCustomerId(), orderTO.getCartItems().size());
        
        // 1. 在庫可用性チェック
        for (CartItem cartItem : orderTO.getCartItems()) {
            Stock stock = stockDao.findByBookId(cartItem.getBookId());
            
            if (stock == null) {
                logger.warn("[ OrderService#orderBooks ] Stock not found: bookId={}", cartItem.getBookId());
                throw new OutOfStockException(cartItem.getBookId(), cartItem.getBookName());
            }
            
            if (stock.getQuantity() < cartItem.getCount()) {
                logger.warn("[ OrderService#orderBooks ] Out of stock: bookId={}, available={}, requested={}", 
                           cartItem.getBookId(), stock.getQuantity(), cartItem.getCount());
                throw new OutOfStockException(cartItem.getBookId(), cartItem.getBookName());
            }
        }
        
        // 2. 在庫更新（楽観的ロック）
        for (CartItem cartItem : orderTO.getCartItems()) {
            stockDao.updateStock(cartItem.getBookId(), cartItem.getCount(), cartItem.getVersion());
        }
        
        // 3. 注文トランザクション作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        
        // Customer設定（外部APIから取得した情報を使用）
        Customer customer = new Customer();
        customer.setCustomerId(orderTO.getCustomerId());
        orderTran.setCustomer(customer);
        
        orderTran.setTotalPrice(orderTO.getTotalPrice());
        orderTran.setDeliveryPrice(orderTO.getDeliveryPrice());
        orderTran.setDeliveryAddress(orderTO.getDeliveryAddress());
        orderTran.setSettlementType(orderTO.getSettlementType());
        
        orderTran = orderTranDao.insert(orderTran);
        
        // 4. 注文明細作成
        int orderDetailId = 1;
        for (CartItem cartItem : orderTO.getCartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderTranId(orderTran.getOrderTranId());
            orderDetail.setOrderDetailId(orderDetailId++);
            
            // Book設定
            Book book = new Book();
            book.setBookId(cartItem.getBookId());
            orderDetail.setBook(book);
            
            orderDetail.setPrice(cartItem.getPrice());
            orderDetail.setCount(cartItem.getCount());
            
            orderDetailDao.insert(orderDetail);
        }
        
        logger.info("[ OrderService#orderBooks ] Order created successfully: orderTranId={}", 
                   orderTran.getOrderTranId());
        
        // 注文サマリーを返す
        return new OrderSummaryTO(
            orderTran.getOrderTranId(),
            orderTran.getOrderDate(),
            orderTran.getTotalPrice(),
            orderTran.getDeliveryPrice(),
            orderTran.getDeliveryAddress(),
            orderTran.getSettlementType()
        );
    }
    
    /**
     * 注文履歴を取得
     * 
     * @param customerId 顧客ID
     * @return 注文履歴リスト（1注文明細=1レコード）
     */
    public List<OrderHistoryTO> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#getOrderHistory ] customerId={}", customerId);
        return orderTranDao.getOrderHistory(customerId);
    }
    
    /**
     * 注文詳細を取得
     * 
     * @param orderTranId 注文トランザクションID
     * @return OrderTran（見つからない場合はnull）
     */
    public OrderTran getOrderDetail(Integer orderTranId) {
        logger.info("[ OrderService#getOrderDetail ] orderTranId={}", orderTranId);
        return orderTranDao.findById(orderTranId);
    }
    
    /**
     * 注文明細を取得
     * 
     * @param orderTranId 注文トランザクションID
     * @param orderDetailId 注文明細ID
     * @return OrderDetail（見つからない場合はnull）
     */
    public OrderDetail getOrderDetailItem(Integer orderTranId, Integer orderDetailId) {
        logger.info("[ OrderService#getOrderDetailItem ] orderTranId={}, orderDetailId={}", 
                   orderTranId, orderDetailId);
        return orderDetailDao.findById(orderTranId, orderDetailId);
    }
}

