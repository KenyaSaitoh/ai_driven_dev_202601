package pro.kensait.berrybooks.service.order;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderDetailResponse;
import pro.kensait.berrybooks.api.dto.OrderHistoryResponse;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.dto.OrderResponse;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.service.exception.OutOfStockException;

/**
 * 注文サービス
 * 
 * 注文処理のビジネスロジックを提供する。
 * トランザクション管理、在庫チェック、外部API連携を実装する。
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
     * @return 作成された注文
     */
    @Transactional
    public OrderTran orderBooks(OrderRequest request, Integer customerId) {
        logger.info("[ OrderService#orderBooks ] customerId={}, cartItems={}", 
                customerId, request.cartItems().size());

        // 1. 在庫チェック
        for (CartItemRequest item : request.cartItems()) {
            BookTO book = backOfficeClient.findBookById(item.bookId());
            if (book == null) {
                logger.warn("[ OrderService#orderBooks ] Book not found: bookId={}", item.bookId());
                throw new OutOfStockException(item.bookId(), item.bookName());
            }
            
            if (book.getQuantity() < item.count()) {
                logger.warn("[ OrderService#orderBooks ] Out of stock: bookId={}, requested={}, available={}", 
                        item.bookId(), item.count(), book.getQuantity());
                throw new OutOfStockException(item.bookId(), item.bookName());
            }
        }

        // 2. 在庫更新（楽観的ロック）
        for (CartItemRequest item : request.cartItems()) {
            BookTO book = backOfficeClient.findBookById(item.bookId());
            int newQuantity = book.getQuantity() - item.count();
            
            logger.debug("[ OrderService#orderBooks ] Updating stock: bookId={}, oldQuantity={}, newQuantity={}, version={}", 
                    item.bookId(), book.getQuantity(), newQuantity, book.getVersion());
            
            backOfficeClient.updateStock(item.bookId(), book.getVersion(), newQuantity);
        }

        // 3. 注文トランザクション作成
        OrderTran orderTran = new OrderTran();
        orderTran.setCustomerId(customerId);
        orderTran.setOrderDate(java.time.LocalDate.now());  // DATE型
        orderTran.setTotalPrice(request.totalPrice());
        orderTran.setDeliveryPrice(request.deliveryPrice());
        orderTran.setDeliveryAddress(request.deliveryAddress());
        orderTran.setSettlementType(request.settlementType());
        
        orderTranDao.insert(orderTran);
        logger.info("[ OrderService#orderBooks ] OrderTran created: orderTranId={}", orderTran.getOrderTranId());

        // 4. 注文明細作成（スナップショットパターン）
        int lineNo = 1;
        for (CartItemRequest item : request.cartItems()) {
            // 外部APIから書籍情報を取得（スナップショット保存のため）
            BookTO book = backOfficeClient.findBookById(item.bookId());
            
            if (book == null || book.getPublisher() == null) {
                logger.error("[ OrderService#orderBooks ] Book or publisher not found: bookId={}", item.bookId());
                throw new RuntimeException("書籍情報の取得に失敗しました: bookId=" + item.bookId());
            }
            
            OrderDetail detail = new OrderDetail();
            detail.setOrderTranId(orderTran.getOrderTranId());
            detail.setOrderDetailId(lineNo++);
            detail.setBookId(item.bookId());
            detail.setBookName(book.getBookName());                           // スナップショット
            detail.setPublisherName(book.getPublisher().getPublisherName()); // スナップショット
            detail.setPrice(item.price());
            detail.setCount(item.count());
            
            orderDetailDao.insert(detail);
            logger.debug("[ OrderService#orderBooks ] OrderDetail created: orderTranId={}, orderDetailId={}, bookName={}", 
                    detail.getOrderTranId(), detail.getOrderDetailId(), detail.getBookName());
        }

        logger.info("[ OrderService#orderBooks ] Order completed: orderTranId={}, totalPrice={}", 
                orderTran.getOrderTranId(), orderTran.getTotalPrice());

        return orderTran;
    }

    /**
     * 顧客IDで注文履歴を取得する（非正規化）
     * 
     * @param customerId 顧客ID
     * @return 注文履歴リスト
     */
    public List<OrderHistoryResponse> findOrderHistoryByCustomerId(Integer customerId) {
        logger.info("[ OrderService#findOrderHistoryByCustomerId ] customerId={}", customerId);

        // OrderTranDaoで注文履歴を取得（JOIN済み）
        List<OrderTran> orders = orderTranDao.findByCustomerId(customerId);
        List<OrderHistoryResponse> history = new ArrayList<>();

        for (OrderTran order : orders) {
            // 各注文の明細を取得
            List<OrderDetail> details = orderDetailDao.findByOrderTranId(order.getOrderTranId());
            
            for (OrderDetail detail : details) {
                // スナップショットデータを使用（外部API呼び出し不要）
                history.add(new OrderHistoryResponse(
                        order.getOrderDate().atStartOfDay(),  // LocalDate → LocalDateTime
                        order.getOrderTranId(),
                        detail.getOrderDetailId(),
                        detail.getBookName(),
                        detail.getPublisherName(),
                        detail.getPrice(),
                        detail.getCount()));
            }
        }

        logger.info("[ OrderService#findOrderHistoryByCustomerId ] Found {} history items", history.size());
        return history;
    }

    /**
     * 注文IDで注文詳細を取得する
     * 
     * @param tranId 注文ID
     * @return 注文詳細
     */
    public OrderResponse findOrderById(Integer tranId) {
        logger.info("[ OrderService#findOrderById ] tranId={}", tranId);

        OrderTran orderTran = orderTranDao.findById(tranId);
        if (orderTran == null) {
            logger.warn("[ OrderService#findOrderById ] OrderTran not found: tranId={}", tranId);
            return null;
        }

        // 注文明細を取得
        List<OrderDetail> details = orderDetailDao.findByOrderTranId(tranId);
        List<OrderDetailResponse> detailResponses = new ArrayList<>();

        for (OrderDetail detail : details) {
            // スナップショットデータを使用（外部API呼び出し不要）
            detailResponses.add(new OrderDetailResponse(
                    detail.getOrderDetailId(),
                    detail.getBookId(),
                    detail.getBookName(),
                    detail.getPublisherName(),
                    detail.getPrice(),
                    detail.getCount()));
        }

        OrderResponse response = new OrderResponse(
                orderTran.getOrderTranId(),          // orderTranId
                orderTran.getCustomerId(),           // customerId
                orderTran.getOrderDate().atStartOfDay(),  // orderDate (LocalDate → LocalDateTime)
                orderTran.getTotalPrice(),           // totalPrice
                orderTran.getDeliveryPrice(),        // deliveryPrice
                orderTran.getDeliveryAddress(),      // deliveryAddress
                orderTran.getSettlementType(),       // settlementType
                detailResponses);                     // orderDetails

        logger.info("[ OrderService#findOrderById ] Found order: tranId={}, details={}", 
                tranId, detailResponses.size());
        return response;
    }

    /**
     * 注文明細IDで注文明細を取得する
     * 
     * @param tranId 注文ID
     * @param lineNo 明細番号
     * @return 注文明細
     */
    public OrderDetailResponse findOrderDetailById(Integer tranId, Integer detailId) {
        logger.info("[ OrderService#findOrderDetailById ] tranId={}, detailId={}", tranId, detailId);

        OrderDetailPK pk = new OrderDetailPK(tranId, detailId);
        OrderDetail detail = orderDetailDao.findByOrderDetailPK(pk);

        if (detail == null) {
            logger.warn("[ OrderService#findOrderDetailById ] OrderDetail not found: tranId={}, detailId={}", 
                    tranId, detailId);
            return null;
        }

        // 書籍情報を外部APIから取得
        var book = backOfficeClient.findBookById(detail.getBookId());
        if (book == null || book.getPublisher() == null) {
            logger.warn("[ OrderService#findOrderDetailById ] Book not found: bookId={}", detail.getBookId());
            return null;
        }

        return new OrderDetailResponse(
                detail.getOrderDetailId(),
                detail.getBookId(),
                book.getBookName(),
                book.getPublisher().getPublisherName(),
                detail.getPrice(),
                detail.getCount());
    }
}
