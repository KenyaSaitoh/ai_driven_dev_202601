package pro.kensait.berrybooks.service.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.exception.OutOfStockException;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.service.delivery.DeliveryFeeService;

import java.time.LocalDate;

/**
 * OrderService - 注文処理サービス
 * 
 * 責務:
 * - 注文処理のビジネスロジック
 * - 在庫確認・更新、注文トランザクション作成、注文明細作成
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 * - @Transactional: トランザクション境界
 * 
 * トランザクション挙動:
 * - 正常終了: コミット（注文トランザクション、注文明細、在庫更新がすべて確定）
 * - 例外発生: ロールバック（注文トランザクション、注文明細が削除、在庫は外部API側で管理）
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
    
    /**
     * 注文処理
     * 
     * 処理フロー:
     * 1. ログ出力: 注文処理開始
     * 2. カート項目ごとに在庫確認・更新:
     *   - backOfficeClient.findStockById(item.bookId()) - 在庫情報取得
     *   - 在庫数チェック: stock.quantity() < item.count() → OutOfStockExceptionをスロー
     *   - backOfficeClient.updateStock(item.bookId(), item.version(), newQuantity) - 在庫更新
     *   - 楽観的ロック失敗時: OptimisticLockExceptionをスロー（再スロー）
     * 3. 配送料金計算:
     *   - deliveryFeeService.calculateDeliveryFee(request.totalPrice(), request.deliveryAddress()) - 配送料金計算
     * 4. 注文トランザクション作成:
     *   - OrderTranエンティティ生成
     *   - orderTranDao.insert(orderTran) - 注文トランザクション登録
     * 5. 注文明細作成（カート項目ごと）:
     *   - OrderDetailエンティティ生成
     *   - 複合主キー設定: OrderDetailPK(orderTranId, detailId)
     *   - スナップショット保存: bookName, publisherName, price
     *   - orderDetailDao.insert(orderDetail) - 注文明細登録
     * 6. ログ出力: 注文処理完了
     * 7. 注文トランザクション返却
     * 
     * @param customerId 顧客ID
     * @param request 注文リクエスト
     * @return OrderTran 作成された注文トランザクション
     * @throws OutOfStockException 在庫不足時
     * @throws OptimisticLockException 楽観的ロック失敗時（トランザクションロールバック）
     */
    @Transactional
    public OrderTran orderBooks(Long customerId, OrderRequest request) {
        logger.info("[ OrderService#orderBooks ] Start order processing for customerId={}", customerId);
        
        // 1. カート項目ごとに在庫確認・更新
        for (CartItemRequest item : request.cartItems()) {
            logger.debug("[ OrderService#orderBooks ] Processing item: bookId={}, count={}", item.bookId(), item.count());
            
            // 在庫情報取得
            StockTO stock = backOfficeClient.findStockById(item.bookId());
            if (stock == null) {
                logger.warn("[ OrderService#orderBooks ] Stock not found for bookId={}", item.bookId());
                throw new OutOfStockException("在庫情報が見つかりません: " + item.bookName());
            }
            
            // 在庫数チェック
            if (stock.quantity() < item.count()) {
                logger.warn("[ OrderService#orderBooks ] Out of stock: bookId={}, available={}, requested={}", 
                           item.bookId(), stock.quantity(), item.count());
                throw new OutOfStockException("在庫が不足しています: " + item.bookName());
            }
            
            // 在庫更新
            Integer newQuantity = stock.quantity() - item.count();
            try {
                backOfficeClient.updateStock(item.bookId(), item.version(), newQuantity);
                logger.debug("[ OrderService#orderBooks ] Stock updated: bookId={}, newQuantity={}", item.bookId(), newQuantity);
            } catch (OptimisticLockException e) {
                logger.warn("[ OrderService#orderBooks ] Optimistic lock conflict for bookId={}", item.bookId());
                throw e; // 再スロー
            }
        }
        
        // 2. 配送料金計算
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(
            request.totalPrice() - request.deliveryPrice(), 
            request.deliveryAddress()
        );
        logger.debug("[ OrderService#orderBooks ] Calculated delivery fee: {}", deliveryFee);
        
        // 3. 注文トランザクション作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(customerId.intValue());
        orderTran.setTotalPrice(request.totalPrice());
        orderTran.setDeliveryPrice(deliveryFee);
        orderTran.setDeliveryAddress(request.deliveryAddress());
        orderTran.setSettlementType(request.settlementType());
        
        orderTran = orderTranDao.insert(orderTran);
        logger.info("[ OrderService#orderBooks ] Order transaction created: orderTranId={}", orderTran.getOrderTranId());
        
        // 4. 注文明細作成
        int detailId = 1;
        for (CartItemRequest item : request.cartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            
            // 複合主キー設定
            OrderDetailPK pk = new OrderDetailPK(orderTran.getOrderTranId(), detailId);
            orderDetail.setId(pk);
            orderDetail.setOrderTran(orderTran);
            
            // スナップショット保存
            orderDetail.setBookId(item.bookId());
            orderDetail.setBookName(item.bookName());
            orderDetail.setPublisherName(item.publisherName());
            orderDetail.setPrice(item.price());
            orderDetail.setCount(item.count());
            
            orderDetailDao.insert(orderDetail);
            logger.debug("[ OrderService#orderBooks ] Order detail created: detailId={}", detailId);
            
            detailId++;
        }
        
        logger.info("[ OrderService#orderBooks ] Order processing completed successfully, orderTranId={}", orderTran.getOrderTranId());
        return orderTran;
    }
}
