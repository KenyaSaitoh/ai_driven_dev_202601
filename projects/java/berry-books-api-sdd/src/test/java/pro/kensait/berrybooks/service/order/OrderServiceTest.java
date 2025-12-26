package pro.kensait.berrybooks.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.OptimisticLockException;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.dao.StockDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.entity.Publisher;
import pro.kensait.berrybooks.entity.Stock;
import pro.kensait.berrybooks.web.cart.CartItem;

import java.time.LocalDateTime;

/**
 * OrderServiceのユニットテストクラス
 * 
 * <p>注文処理のビジネスロジックをテストします。</p>
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private StockDao stockDao;
    
    @Mock
    private OrderTranDao orderTranDao;
    
    @Mock
    private OrderDetailDao orderDetailDao;
    
    @Mock
    private BookDao bookDao;
    
    @InjectMocks
    private OrderService orderService;
    
    private OrderTO orderTO;
    private List<CartItem> cartItems;
    
    @BeforeEach
    void setUp() {
        // カートアイテムの準備
        cartItems = new ArrayList<>();
        
        CartItem item1 = new CartItem();
        item1.setBookId(1);
        item1.setBookName("Java SE ディープダイブ");
        item1.setPublisherName("Test Publisher");
        item1.setPrice(new BigDecimal("3400"));
        item1.setCount(2);
        item1.setVersion(1L);
        cartItems.add(item1);
        
        CartItem item2 = new CartItem();
        item2.setBookId(2);
        item2.setBookName("SpringBoot in Cloud");
        item2.setPublisherName("Test Publisher");
        item2.setPrice(new BigDecimal("3000"));
        item2.setCount(1);
        item2.setVersion(1L);
        cartItems.add(item2);
        
        // OrderTOの準備
        orderTO = new OrderTO();
        orderTO.setCustomerId(1);
        orderTO.setDeliveryAddress("東京都渋谷区神南1-1-1");
        orderTO.setDeliveryPrice(new BigDecimal("800"));
        orderTO.setSettlementCode(2);
        orderTO.setCartItems(cartItems);
    }
    
    /**
     * 注文確定（正常系）
     */
    @Test
    void testOrderBooks_Success() {
        // Arrange
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(10);
        stock1.setVersion(1L);
        
        Stock stock2 = new Stock();
        stock2.setBookId(2);
        stock2.setQuantity(5);
        stock2.setVersion(1L);
        
        when(stockDao.findByBookId(1)).thenReturn(stock1);
        when(stockDao.findByBookId(2)).thenReturn(stock2);
        when(stockDao.update(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        
        // BookDaoのモック設定
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Java SE ディープダイブ");
        book1.setPublisher(publisher);
        book1.setPrice(new BigDecimal("3400"));
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("SpringBoot in Cloud");
        book2.setPublisher(publisher);
        book2.setPrice(new BigDecimal("3000"));
        
        when(bookDao.findById(1)).thenReturn(book1);
        when(bookDao.findById(2)).thenReturn(book2);
        
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        doAnswer(invocation -> {
            OrderTran ot = invocation.getArgument(0);
            ot.setOrderTranId(1);
            return null;
        }).when(orderTranDao).persist(any(OrderTran.class));
        
        // Act
        OrderTran result = orderService.orderBooks(orderTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        
        // 在庫が減算されたことを確認
        assertEquals(8, stock1.getQuantity(), "書籍1の在庫は10-2=8であるべき");
        assertEquals(4, stock2.getQuantity(), "書籍2の在庫は5-1=4であるべき");
        
        // 各Daoのメソッドが呼ばれたことを確認
        verify(stockDao, times(1)).findByBookId(1);
        verify(stockDao, times(1)).findByBookId(2);
        verify(stockDao, times(2)).update(any(Stock.class));
        verify(orderTranDao, times(1)).persist(any(OrderTran.class));
        verify(orderDetailDao, times(2)).persist(any());
    }
    
    /**
     * 在庫不足（異常系）
     */
    @Test
    void testOrderBooks_OutOfStock() {
        // Arrange
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(1); // 注文数2より少ない
        stock1.setVersion(1L);
        
        when(stockDao.findByBookId(1)).thenReturn(stock1);
        
        // Act & Assert
        OutOfStockException exception = assertThrows(
            OutOfStockException.class,
            () -> orderService.orderBooks(orderTO),
            "在庫不足の場合、OutOfStockExceptionがスローされるべき"
        );
        
        assertEquals(1, exception.getBookId());
        assertEquals("Java SE ディープダイブ", exception.getBookName());
        
        // 在庫更新が呼ばれないことを確認
        verify(stockDao, never()).update(any(Stock.class));
        verify(orderTranDao, never()).persist(any(OrderTran.class));
        verify(orderDetailDao, never()).persist(any());
    }
    
    /**
     * 楽観的ロック競合（異常系）
     */
    @Test
    void testOrderBooks_OptimisticLockException() {
        // Arrange
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(10);
        stock1.setVersion(2L); // カートに保存されたバージョン(1L)と異なる
        
        when(stockDao.findByBookId(1)).thenReturn(stock1);
        when(stockDao.update(any(Stock.class))).thenThrow(new OptimisticLockException());
        
        // Act & Assert
        assertThrows(
            OptimisticLockException.class,
            () -> orderService.orderBooks(orderTO),
            "バージョン不一致の場合、OptimisticLockExceptionがスローされるべき"
        );
        
        // 注文登録が呼ばれないことを確認
        verify(orderTranDao, never()).persist(any(OrderTran.class));
        verify(orderDetailDao, never()).persist(any());
    }
    
    /**
     * 在庫が存在しない（異常系）
     */
    @Test
    void testOrderBooks_StockNotFound() {
        // Arrange
        when(stockDao.findByBookId(1)).thenReturn(null); // 在庫が見つからない
        
        // Act & Assert
        OutOfStockException exception = assertThrows(
            OutOfStockException.class,
            () -> orderService.orderBooks(orderTO),
            "在庫が存在しない場合、OutOfStockExceptionがスローされるべき"
        );
        
        assertEquals(1, exception.getBookId());
        assertEquals("Java SE ディープダイブ", exception.getBookName());
        
        // 在庫更新が呼ばれないことを確認
        verify(stockDao, never()).update(any(Stock.class));
        verify(orderTranDao, never()).persist(any(OrderTran.class));
        verify(orderDetailDao, never()).persist(any());
    }
    
    /**
     * 注文履歴取得（正常系）
     */
    @Test
    void testGetOrderHistory_Success() {
        // Arrange
        Integer customerId = 1;
        List<OrderTran> orderTrans = new ArrayList<>();
        
        // OrderTranを作成
        OrderTran orderTran1 = new OrderTran();
        orderTran1.setOrderTranId(1);
        orderTran1.setCustomerId(customerId);
        orderTran1.setOrderDate(LocalDateTime.of(2025, 12, 1, 10, 0));
        orderTran1.setTotalPrice(new BigDecimal("9800"));
        orderTran1.setDeliveryPrice(new BigDecimal("800"));
        orderTran1.setSettlementType(2);
        
        // Publisher, Book, OrderDetailを作成
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Book book = new Book();
        book.setBookId(1);
        book.setBookName("Test Book");
        book.setPublisher(publisher);
        book.setPrice(new BigDecimal("3000"));
        
        OrderDetailPK detailPK = new OrderDetailPK();
        detailPK.setOrderTranId(1);
        detailPK.setOrderDetailId(1);
        
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(detailPK);
        orderDetail.setBook(book);
        orderDetail.setPrice(new BigDecimal("3000"));
        orderDetail.setCount(2);
        
        List<OrderDetail> orderDetails = new ArrayList<>();
        orderDetails.add(orderDetail);
        orderTran1.setOrderDetails(orderDetails);
        
        orderTrans.add(orderTran1);
        
        when(orderTranDao.findByCustomerIdWithDetails(customerId)).thenReturn(orderTrans);
        
        // Act
        List<OrderHistoryTO> result = orderService.getOrderHistory(customerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "注文明細が1件あるはず");
        assertEquals(1, result.get(0).tranId(), "注文IDは1");
        assertEquals(1, result.get(0).detailId(), "注文明細IDは1");
        assertEquals("Test Book", result.get(0).bookName(), "書籍名はTest Book");
        assertEquals("Test Publisher", result.get(0).publisherName(), "出版社名はTest Publisher");
        assertEquals(new BigDecimal("3000"), result.get(0).price(), "価格は3000");
        assertEquals(2, result.get(0).count(), "数量は2");
        
        verify(orderTranDao, times(1)).findByCustomerIdWithDetails(customerId);
    }
    
    /**
     * 注文履歴取得（履歴なし）
     */
    @Test
    void testGetOrderHistory_NoHistory() {
        // Arrange
        Integer customerId = 999;
        when(orderTranDao.findByCustomerIdWithDetails(customerId)).thenReturn(new ArrayList<>());
        
        // Act
        List<OrderHistoryTO> result = orderService.getOrderHistory(customerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(orderTranDao, times(1)).findByCustomerIdWithDetails(customerId);
    }
    
    /**
     * 注文詳細取得（正常系）- JOIN FETCHでN+1問題を回避
     */
    @Test
    void testGetOrderDetail_Success() {
        // Arrange
        Integer orderTranId = 1;
        
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(orderTranId);
        orderTran.setTotalPrice(new BigDecimal("9800"));
        orderTran.setOrderDetails(new ArrayList<>()); // JOIN FETCHで取得された明細リスト
        
        when(orderTranDao.findByIdWithDetails(orderTranId)).thenReturn(orderTran);
        
        // Act
        OrderSummaryTO result = orderService.getOrderDetail(orderTranId);
        
        // Assert
        assertNotNull(result);
        assertEquals(orderTranId, result.getOrderTran().getOrderTranId());
        assertNotNull(result.getOrderDetails());
        
        verify(orderTranDao, times(1)).findByIdWithDetails(orderTranId);
        // JOIN FETCHを使用するため、orderDetailDaoは呼ばれない
        verify(orderDetailDao, never()).findByOrderTranId(anyInt());
    }
    
    /**
     * 注文詳細取得（注文が存在しない）
     */
    @Test
    void testGetOrderDetail_NotFound() {
        // Arrange
        Integer orderTranId = 999;
        when(orderTranDao.findByIdWithDetails(orderTranId)).thenReturn(null);
        
        // Act
        OrderSummaryTO result = orderService.getOrderDetail(orderTranId);
        
        // Assert
        assertNull(result, "注文が存在しない場合、nullが返されるべき");
        
        verify(orderTranDao, times(1)).findByIdWithDetails(orderTranId);
        verify(orderDetailDao, never()).findByOrderTranId(anyInt());
    }
}

