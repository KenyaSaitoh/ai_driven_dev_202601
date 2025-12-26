package pro.kensait.berrybooks.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.dao.StockDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.entity.Stock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderTranDao orderTranDao;

    @Mock
    private OrderDetailDao orderDetailDao;

    @Mock
    private BookDao bookDao;

    @Mock
    private StockDao stockDao;

    @InjectMocks
    private OrderService orderService;

    private Integer testCustomerId;
    private Integer testOrderTranId;
    private OrderTran testOrderTran;
    private List<OrderTran> testOrderTranList;

    @BeforeEach
    void setUp() {
        testCustomerId = 1;
        testOrderTranId = 100;
        testOrderTran = new OrderTran();
        testOrderTran.setOrderTranId(testOrderTranId);
        testOrderTran.setCustomer(testCustomerId);
        
        testOrderTranList = new ArrayList<>();
        testOrderTranList.add(testOrderTran);
    }

    @Test
    @DisplayName("顧客IDで注文履歴を取得できることをテストする")
    void testGetOrderHistory() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findByCustomerId(testCustomerId)).thenReturn(testOrderTranList);

        // 実行フェーズ
        List<OrderTran> result = orderService.getOrderHistory(testCustomerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrderTranId, result.get(0).getOrderTranId());
        verify(orderTranDao, times(1)).findByCustomerId(testCustomerId);
    }

    @Test
    @DisplayName("注文履歴をTransfer Objectで取得できることをテストする")
    void testGetOrderHistory2() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        List<OrderHistoryTO> expectedList = new ArrayList<>();
        when(orderTranDao.findOrderHistoryByCustomerId(testCustomerId)).thenReturn(expectedList);

        // 実行フェーズ
        List<OrderHistoryTO> result = orderService.getOrderHistory2(testCustomerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        verify(orderTranDao, times(1)).findOrderHistoryByCustomerId(testCustomerId);
    }

    @Test
    @DisplayName("注文履歴を明細と共に取得できることをテストする")
    void testGetOrderHistory3() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findByCustomerIdWithDetails(testCustomerId)).thenReturn(testOrderTranList);

        // 実行フェーズ
        List<OrderTran> result = orderService.getOrderHistory3(testCustomerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderTranDao, times(1)).findByCustomerIdWithDetails(testCustomerId);
    }

    @Test
    @DisplayName("注文IDで注文情報を取得できることをテストする")
    void testGetOrderTran() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findById(testOrderTranId)).thenReturn(testOrderTran);

        // 実行フェーズ
        OrderTran result = orderService.getOrderTran(testOrderTranId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(testOrderTranId, result.getOrderTranId());
        verify(orderTranDao, times(1)).findById(testOrderTranId);
    }

    @Test
    @DisplayName("存在しない注文IDで例外がスローされることをテストする")
    void testGetOrderTranNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findById(testOrderTranId)).thenReturn(null);

        // 実行フェーズと検証フェーズ（出力値ベース）
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderTran(testOrderTranId);
        });
        assertTrue(exception.getMessage().contains("OrderTran not found"));
    }

    @Test
    @DisplayName("注文情報を明細と共に取得できることをテストする")
    void testGetOrderTranWithDetails() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findByIdWithDetails(testOrderTranId)).thenReturn(testOrderTran);

        // 実行フェーズ
        OrderTran result = orderService.getOrderTranWithDetails(testOrderTranId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(testOrderTranId, result.getOrderTranId());
        verify(orderTranDao, times(1)).findByIdWithDetails(testOrderTranId);
    }

    @Test
    @DisplayName("存在しない注文IDで明細取得時に例外がスローされることをテストする")
    void testGetOrderTranWithDetailsNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(orderTranDao.findByIdWithDetails(testOrderTranId)).thenReturn(null);

        // 実行フェーズと検証フェーズ（出力値ベース）
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderTranWithDetails(testOrderTranId);
        });
        assertTrue(exception.getMessage().contains("OrderTran not found"));
    }

    @Test
    @DisplayName("複合主キーで注文明細を取得できることをテストする")
    void testGetOrderDetailByPK() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        OrderDetailPK pk = new OrderDetailPK(testOrderTranId, 1);
        OrderDetail expectedDetail = new OrderDetail();
        expectedDetail.setOrderDetailId(1);
        when(orderDetailDao.findById(pk)).thenReturn(expectedDetail);

        // 実行フェーズ
        OrderDetail result = orderService.getOrderDetail(pk);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.getOrderDetailId());
        verify(orderDetailDao, times(1)).findById(pk);
    }

    @Test
    @DisplayName("存在しない注文明細IDで例外がスローされることをテストする")
    void testGetOrderDetailByPKNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        OrderDetailPK pk = new OrderDetailPK(testOrderTranId, 1);
        when(orderDetailDao.findById(pk)).thenReturn(null);

        // 実行フェーズと検証フェーズ（出力値ベース）
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderDetail(pk);
        });
        assertTrue(exception.getMessage().contains("OrderDetail not found"));
    }

    @Test
    @DisplayName("注文IDと明細IDで注文明細を取得できることをテストする（オーバーロード）")
    void testGetOrderDetailByIds() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer detailId = 1;
        OrderDetail expectedDetail = new OrderDetail();
        expectedDetail.setOrderDetailId(detailId);
        when(orderDetailDao.findById(any(OrderDetailPK.class))).thenReturn(expectedDetail);

        // 実行フェーズ
        OrderDetail result = orderService.getOrderDetail(testOrderTranId, detailId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(detailId, result.getOrderDetailId());
        verify(orderDetailDao, times(1)).findById(any(OrderDetailPK.class));
    }

    @Test
    @DisplayName("注文IDで注文明細のリストを取得できることをテストする")
    void testGetOrderDetails() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        List<OrderDetail> expectedList = new ArrayList<>();
        OrderDetail detail1 = new OrderDetail();
        detail1.setOrderDetailId(1);
        expectedList.add(detail1);
        when(orderDetailDao.findByOrderTranId(testOrderTranId)).thenReturn(expectedList);

        // 実行フェーズ
        List<OrderDetail> result = orderService.getOrderDetails(testOrderTranId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderDetailDao, times(1)).findByOrderTranId(testOrderTranId);
    }

    @Test
    @DisplayName("注文が正常に完了し、在庫が減少することをテストする")
    void testOrderBooksSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer bookId = 1;
        Integer quantity = 2;
        Integer stockQuantity = 10;
        
        CartItem cartItem = new CartItem();
        cartItem.setBookId(bookId);
        cartItem.setBookName("Test Book");
        cartItem.setPublisherName("Test Publisher");
        cartItem.setPrice(new BigDecimal("1000"));
        cartItem.setCount(quantity);
        
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        
        OrderTO orderTO = new OrderTO(
            testCustomerId,
            LocalDate.now(),
            cartItems,
            new BigDecimal("2000"),
            new BigDecimal("800"),
            "東京都渋谷区",
            1 // クレジットカード
        );
        
        Stock stock = new Stock();
        stock.setBookId(bookId);
        stock.setQuantity(stockQuantity);
        
        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName("Test Book");
        
        OrderTran savedOrderTran = new OrderTran();
        savedOrderTran.setOrderTranId(testOrderTranId);
        
        when(stockDao.findByIdWithLock(bookId)).thenReturn(stock);
        when(bookDao.findById(bookId)).thenReturn(book);
        doAnswer(invocation -> {
            OrderTran ot = invocation.getArgument(0);
            ot.setOrderTranId(testOrderTranId);
            return null;
        }).when(orderTranDao).persist(any(OrderTran.class));
        when(orderTranDao.findByIdWithDetails(testOrderTranId)).thenReturn(savedOrderTran);
        doNothing().when(orderDetailDao).persist(any(OrderDetail.class));

        // 実行フェーズ
        OrderTran result = orderService.orderBooks(orderTO);

        // 検証フェーズ（出力値ベース、状態ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(stockQuantity - quantity, stock.getQuantity());
        verify(stockDao, times(1)).findByIdWithLock(bookId);
        verify(orderTranDao, times(1)).persist(any(OrderTran.class));
        verify(orderDetailDao, times(1)).persist(any(OrderDetail.class));
        verify(orderTranDao, times(1)).findByIdWithDetails(testOrderTranId);
    }

    @Test
    @DisplayName("在庫不足の場合にOutOfStockExceptionがスローされることをテストする")
    void testOrderBooksOutOfStock() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer bookId = 1;
        Integer quantity = 10;
        Integer stockQuantity = 5;
        
        CartItem cartItem = new CartItem();
        cartItem.setBookId(bookId);
        cartItem.setBookName("Test Book");
        cartItem.setPublisherName("Test Publisher");
        cartItem.setPrice(new BigDecimal("2000"));
        cartItem.setCount(quantity);
        
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        
        OrderTO orderTO = new OrderTO(
            testCustomerId,
            LocalDate.now(),
            cartItems,
            new BigDecimal("2000"),
            new BigDecimal("800"),
            "東京都渋谷区",
            1 // クレジットカード
        );
        
        Stock stock = new Stock();
        stock.setBookId(bookId);
        stock.setQuantity(stockQuantity);
        
        when(stockDao.findByIdWithLock(bookId)).thenReturn(stock);

        // 実行フェーズと検証フェーズ（出力値ベース、コミュニケーションベース）
        OutOfStockException exception = assertThrows(OutOfStockException.class, () -> {
            orderService.orderBooks(orderTO);
        });
        assertEquals(bookId, exception.getBookId());
        assertEquals("Test Book", exception.getBookName());
        verify(stockDao, times(1)).findByIdWithLock(bookId);
        verify(orderTranDao, never()).persist(any(OrderTran.class));
    }

    @Test
    @DisplayName("複数の書籍を含む注文が正常に処理されることをテストする")
    void testOrderBooksMultipleItems() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer bookId1 = 1;
        Integer bookId2 = 2;
        
        CartItem cartItem1 = new CartItem();
        cartItem1.setBookId(bookId1);
        cartItem1.setBookName("Test Book 1");
        cartItem1.setPublisherName("Test Publisher 1");
        cartItem1.setPrice(new BigDecimal("2000"));
        cartItem1.setCount(2);
        
        CartItem cartItem2 = new CartItem();
        cartItem2.setBookId(bookId2);
        cartItem2.setBookName("Test Book 2");
        cartItem2.setPublisherName("Test Publisher 2");
        cartItem2.setPrice(new BigDecimal("1500"));
        cartItem2.setCount(3);
        
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);
        cartItems.add(cartItem2);
        
        OrderTO orderTO = new OrderTO(
            testCustomerId,
            LocalDate.now(),
            cartItems,
            new BigDecimal("5000"),
            new BigDecimal("800"),
            "東京都渋谷区",
            1 // クレジットカード
        );
        
        Stock stock1 = new Stock();
        stock1.setBookId(bookId1);
        stock1.setQuantity(10);
        
        Stock stock2 = new Stock();
        stock2.setBookId(bookId2);
        stock2.setQuantity(20);
        
        Book book1 = new Book();
        book1.setBookId(bookId1);
        book1.setBookName("Test Book 1");
        
        Book book2 = new Book();
        book2.setBookId(bookId2);
        book2.setBookName("Test Book 2");
        
        OrderTran savedOrderTran = new OrderTran();
        savedOrderTran.setOrderTranId(testOrderTranId);
        
        when(stockDao.findByIdWithLock(bookId1)).thenReturn(stock1);
        when(stockDao.findByIdWithLock(bookId2)).thenReturn(stock2);
        when(bookDao.findById(bookId1)).thenReturn(book1);
        when(bookDao.findById(bookId2)).thenReturn(book2);
        doAnswer(invocation -> {
            OrderTran ot = invocation.getArgument(0);
            ot.setOrderTranId(testOrderTranId);
            return null;
        }).when(orderTranDao).persist(any(OrderTran.class));
        when(orderTranDao.findByIdWithDetails(testOrderTranId)).thenReturn(savedOrderTran);
        doNothing().when(orderDetailDao).persist(any(OrderDetail.class));

        // 実行フェーズ
        OrderTran result = orderService.orderBooks(orderTO);

        // 検証フェーズ（出力値ベース、状態ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(8, stock1.getQuantity()); // 10 - 2
        assertEquals(17, stock2.getQuantity()); // 20 - 3
        verify(orderDetailDao, times(2)).persist(any(OrderDetail.class));
    }
}

