package pro.kensait.berrybooks.service.order;

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
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.entity.Publisher;
import pro.kensait.berrybooks.entity.Stock;

import java.time.LocalDate;

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
        item1.setPrice(3400);
        item1.setCount(2);
        item1.setVersion(1L);
        cartItems.add(item1);
        
        CartItem item2 = new CartItem();
        item2.setBookId(2);
        item2.setBookName("SpringBoot in Cloud");
        item2.setPublisherName("Test Publisher");
        item2.setPrice(3000);
        item2.setCount(1);
        item2.setVersion(1L);
        cartItems.add(item2);
        
        // OrderTOの準備
        orderTO = new OrderTO();
        orderTO.setCustomerId(1);
        orderTO.setDeliveryAddress("東京都渋谷区神南1-1-1");
        orderTO.setDeliveryPrice(800);
        orderTO.setSettlementType(2);
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
        
        // BookDaoのモック設定
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Java SE ディープダイブ");
        book1.setPublisher(publisher);
        book1.setPrice(3400);
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("SpringBoot in Cloud");
        book2.setPublisher(publisher);
        book2.setPrice(3000);
        
        when(bookDao.findById(1)).thenReturn(book1);
        when(bookDao.findById(2)).thenReturn(book2);
        
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        when(orderTranDao.insert(any(OrderTran.class))).thenAnswer(invocation -> {
            OrderTran ot = invocation.getArgument(0);
            ot.setOrderTranId(1);
            return ot;
        });
        
        // Act
        OrderSummaryTO result = orderService.orderBooks(orderTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.orderTranId());
        
        // 在庫が減算されたことを確認
        assertEquals(8, stock1.getQuantity(), "書籍1の在庫は10-2=8であるべき");
        assertEquals(4, stock2.getQuantity(), "書籍2の在庫は5-1=4であるべき");
        
        // 各Daoのメソッドが呼ばれたことを確認
        verify(stockDao, times(1)).findByBookId(1);
        verify(stockDao, times(1)).findByBookId(2);
        verify(stockDao, times(2)).updateStock(anyInt(), anyInt(), any());
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
        verify(orderDetailDao, times(2)).insert(any(OrderDetail.class));
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
        verify(stockDao, never()).updateStock(anyInt(), anyInt(), any());
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
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
        doAnswer(invocation -> {
            throw new OptimisticLockException();
        }).when(stockDao).updateStock(anyInt(), anyInt(), any());
        
        // Act & Assert
        assertThrows(
            OptimisticLockException.class,
            () -> orderService.orderBooks(orderTO),
            "バージョン不一致の場合、OptimisticLockExceptionがスローされるべき"
        );
        
        // 注文登録が呼ばれないことを確認
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
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
        verify(stockDao, never()).updateStock(anyInt(), anyInt(), any());
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
    }
    
    /**
     * 注文履歴取得（正常系）
     */
    @Test
    void testGetOrderHistory_Success() {
        // Arrange
        Integer customerId = 1;
        List<OrderHistoryTO> orderHistory = new ArrayList<>();
        
        // OrderHistoryTOを作成
        OrderHistoryTO history1 = new OrderHistoryTO(
            LocalDate.of(2025, 12, 1),
            1,  // orderTranId
            1,  // orderDetailId
            "Test Book",
            "Test Publisher",
            3000,  // price
            2   // count
        );
        
        orderHistory.add(history1);
        
        when(orderTranDao.getOrderHistory(customerId)).thenReturn(orderHistory);
        
        // Act
        List<OrderHistoryTO> result = orderService.getOrderHistory(customerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "注文明細が1件あるはず");
        assertEquals(1, result.get(0).orderTranId(), "注文IDは1");
        assertEquals(1, result.get(0).orderDetailId(), "注文明細IDは1");
        assertEquals("Test Book", result.get(0).bookName(), "書籍名はTest Book");
        assertEquals("Test Publisher", result.get(0).publisherName(), "出版社名はTest Publisher");
        assertEquals(3000, result.get(0).price(), "価格は3000");
        assertEquals(2, result.get(0).count(), "数量は2");
        
        verify(orderTranDao, times(1)).getOrderHistory(customerId);
    }
    
    /**
     * 注文履歴取得（履歴なし）
     */
    @Test
    void testGetOrderHistory_NoHistory() {
        // Arrange
        Integer customerId = 999;
        when(orderTranDao.getOrderHistory(customerId)).thenReturn(new ArrayList<>());
        
        // Act
        List<OrderHistoryTO> result = orderService.getOrderHistory(customerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(orderTranDao, times(1)).getOrderHistory(customerId);
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
        orderTran.setTotalPrice(9800);
        orderTran.setOrderDetails(new ArrayList<>()); // JOIN FETCHで取得された明細リスト
        
        when(orderTranDao.findById(orderTranId)).thenReturn(orderTran);
        
        // Act
        OrderTran result = orderService.getOrderDetail(orderTranId);
        
        // Assert
        assertNotNull(result);
        assertEquals(orderTranId, result.getOrderTranId());
        assertNotNull(result.getOrderDetails());
        
        verify(orderTranDao, times(1)).findById(orderTranId);
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
        when(orderTranDao.findById(orderTranId)).thenReturn(null);
        
        // Act
        OrderTran result = orderService.getOrderDetail(orderTranId);
        
        // Assert
        assertNull(result, "注文が存在しない場合、nullが返されるべき");
        
        verify(orderTranDao, times(1)).findById(orderTranId);
        verify(orderDetailDao, never()).findByOrderTranId(anyInt());
    }
}
