package pro.kensait.berrybooks.service.order;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.dao.StockDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.entity.Stock;
import pro.kensait.berrybooks.web.cart.CartItem;

// 注文処理と注文履歴取得を行うサービスクラス
@ApplicationScoped
@Transactional
public class OrderService implements OrderServiceIF {
    private static final Logger logger = LoggerFactory.getLogger(
            OrderService.class);

    @Inject
    private OrderTranDao orderTranDao;

    @Inject
    private OrderDetailDao orderDetailDao;

    @Inject
    private BookDao bookDao;

    @Inject
    private StockDao stockDao;

    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    @Override
    public List<OrderTran> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory ]");

        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderTran> orderTranList =
                orderTranDao.findByCustomerId(customerId);
        return orderTranList;
    }

    // サービスメソッド：注文履歴のリストを取得する（方式2・詳細版）
    @Override
    public List<OrderHistoryTO> getOrderHistory2(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory2 ]");

        // 顧客IDから注文履歴のリストを取得し、返す
        List<OrderHistoryTO> orderHistoryList =
                orderTranDao.findOrderHistoryByCustomerId(customerId);
        return orderHistoryList;
    }

    // サービスメソッド：注文エンティティのリストを取得する（方式3）
    @Override
    public List<OrderTran> getOrderHistory3(Integer customerId) {
        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderTran> orderTranList =
                orderTranDao.findByCustomerIdWithDetails(customerId);
        return orderTranList;
    }

    // サービスメソッド：注文エンティティを取得する
    @Override
    public OrderTran getOrderTran(Integer orderTranId) {
        logger.info("[ OrderService#getOrderTran ]");

        // 注文IDから注文エンティティを取得し、返す
        OrderTran orderTran = orderTranDao.findById(orderTranId);
        if (orderTran == null) {
            throw new RuntimeException("OrderTran not found for ID: " + orderTranId);
        }
        return orderTran;
    }

    // サービスメソッド：注文エンティティを明細と共に取得する
    @Override
    public OrderTran getOrderTranWithDetails(Integer orderTranId) {
        logger.info("[ OrderService#getOrderTranWithDetails ]");

        // 注文IDから注文エンティティを明細と共に取得し、返す
        OrderTran orderTran = orderTranDao.findByIdWithDetails(orderTranId);
        if (orderTran == null) {
            throw new RuntimeException("OrderTran not found for ID: " + orderTranId);
        }
        return orderTran;
    }

    // サービスメソッド：注文明細エンティティを取得する
    @Override
    public OrderDetail getOrderDetail(OrderDetailPK pk) {
        logger.info("[ OrderService#getOrderDetail ]");

        // 複合主キー（注文IDと注文明細ID）から注文明細エンティティを取得し、返す
        OrderDetail orderDetail = orderDetailDao.findById(pk);
        if (orderDetail == null) {
            throw new RuntimeException("OrderDetail not found for PK: " + pk);
        }
        return orderDetail;
    }

    // サービスメソッド：注文明細エンティティを取得する（オーバーロード）
    @Override
    public OrderDetail getOrderDetail(Integer tranId, Integer detailId) {
        logger.info("[ OrderService#getOrderDetail ] tranId=" + tranId 
                + ", detailId=" + detailId);

        OrderDetailPK pk = new OrderDetailPK(tranId, detailId);
        return getOrderDetail(pk);
    }

    // サービスメソッド：注文明細エンティティのリストを取得する
    @Override
    public List<OrderDetail> getOrderDetails(Integer orderTranId) {
        // 注文IDから注文明細エンティティのリストを取得し、返す
        List<OrderDetail> orderDetailList = orderDetailDao.findByOrderTranId(orderTranId);
        return orderDetailList;
    }

    // サービスメソッド：注文する
    @Override
    public OrderTran orderBooks(OrderTO orderTO) {
        logger.info("[ OrderService#orderBooks ]");

        // カートに追加された書籍毎に、在庫の残り個数をチェックする
        for (CartItem cartItem : orderTO.cartItems()) {

            // 楽観的ロック：在庫エンティティを作成
            // （悲観的ロックのfindByIdWithLockは使用しない）
            Stock stock = new Stock();
            stock.setBookId(cartItem.getBookId());
            
            // 現在の在庫数を取得する
            Stock currentStock = stockDao.findById(cartItem.getBookId());

            // 楽観的ロックのために現在の最新VERSIONをセットする
            stock.setVersion(currentStock.getVersion());

            // 在庫を減らす（マイナスになることも許容する）
            int remaining = currentStock.getQuantity() - cartItem.getCount();

            // 在庫を減らし楽観的ロックを行う（現在のVERSION値を持つStockエンティティで更新）
            stock.setQuantity(remaining);
            stockDao.update(stock);
        }

        // 新しいOrderTranインスタンスを生成する
        OrderTran orderTran = new OrderTran(
                orderTO.orderDate(),
                orderTO.customerId(),
                orderTO.totalPrice(),
                orderTO.deliveryPrice(),
                orderTO.deliveryAddress(),
                orderTO.settlementType());

        // 生成したOrderTranインスタンスをpersist操作により永続化する
        orderTranDao.persist(orderTran);

        // カートアイテム（個々の注文明細）のイテレータを取得する
        List<CartItem> cartItems = orderTO.cartItems();

        // OrderDetailインスタンスの主キー値（注文明細ID）の初期値を設定する
        int orderDetailId = 0;

        for (CartItem cartItem : cartItems) {
            Book book = bookDao.findById(cartItem.getBookId());

            // OrderDetailインスタンスの主キー値（注文明細ID）をカウントアップする
            orderDetailId = orderDetailId + 1;

            // 新しいOrderDetailインスタンスを生成する
            OrderDetail orderDetail = new OrderDetail(
                    orderTran.getOrderTranId(),
                    orderDetailId,
                    book,
                    cartItem.getCount());

            // OrderDetailインスタンスを保存する
            orderDetailDao.persist(orderDetail);
        }

        // データベースから明細を含めて再取得して返す
        // （永続化した明細をorderDetailsリレーションシップに反映させるため）
        return orderTranDao.findByIdWithDetails(orderTran.getOrderTranId());
    }
}