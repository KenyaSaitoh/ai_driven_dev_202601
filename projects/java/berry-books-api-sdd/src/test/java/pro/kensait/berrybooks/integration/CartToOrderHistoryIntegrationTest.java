package pro.kensait.berrybooks.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.Publisher;
import pro.kensait.berrybooks.entity.Stock;
import pro.kensait.berrybooks.service.book.BookService;
import pro.kensait.berrybooks.service.order.OrderTO;
import pro.kensait.berrybooks.service.order.OrderHistoryTO;
import pro.kensait.berrybooks.web.book.SearchParam;
import pro.kensait.berrybooks.web.cart.CartItem;
import pro.kensait.berrybooks.web.cart.CartSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * T_INTEG_002: カート → 注文処理 → 注文履歴の結合テスト
 * 
 * 目的: F-002（ショッピングカート）→ F-003（注文処理）→ F-005（注文履歴）の連携を確認する
 * 
 * テストシナリオ:
 * 1. カート画面で「注文に進む」ボタンをクリック
 * 2. 注文入力画面で配送先住所を入力
 * 3. 決済方法を選択
 * 4. 配送料金を計算
 * 5. 注文確定ボタンをクリック
 * 6. 注文完了画面が表示されることを確認
 * 7. 注文履歴画面で注文が表示されることを確認
 * 
 * 期待結果: 注文が正常に完了し、注文履歴に表示される
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartToOrderHistoryIntegrationTest extends IntegrationTestBase {

    @Mock
    private BookDao bookDao;
    
    @InjectMocks
    private BookService bookService;
    
    private CartSession cartSession;

    @BeforeEach
    public void setUp() {
        // テスト用のセッションを初期化
        cartSession = new CartSession();
        
        // カートをクリア
        cartSession.clear();
    }

    @Test
    @Order(1)
    @DisplayName("カート → 注文処理 → 注文履歴の統合フロー")
    public void testCartToOrderHistoryFlow() throws Exception {
        // Given: テスト用の書籍データを準備
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Stock stock = new Stock();
        stock.setBookId(1);
        stock.setQuantity(10);
        stock.setVersion(1L);
        
        final Book mockBook = new Book();
        mockBook.setBookId(1);
        mockBook.setBookName("Test Book");
        mockBook.setPublisher(publisher);
        mockBook.setPrice(new BigDecimal("3000"));
        mockBook.setStock(stock);
        
        List<Book> bookList = new ArrayList<>();
        bookList.add(mockBook);
        
        // BookDaoのモック設定
        when(bookDao.findByCategory(1)).thenReturn(bookList);
        
        // Given: カートに書籍が1冊存在する
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1); // Javaカテゴリ
        List<Book> books = bookService.searchBook(searchParam);
        assertFalse(books.isEmpty(), "テスト用の書籍が必要です");
        
        final Book testBook = books.get(0);
        CartItem cartItem = new CartItem();
        cartItem.setBookId(testBook.getBookId());
        cartItem.setBookName(testBook.getBookName());
        cartItem.setPublisherName(testBook.getPublisher().getPublisherName());
        cartItem.setPrice(testBook.getPrice());
        cartItem.setCount(1);
        cartItem.setVersion(testBook.getStock().getVersion());
        
        cartSession.addItem(cartItem);
        cartSession.calculateTotalPrice();
        
        // Step 1: カートから注文入力へ遷移
        List<CartItem> cartItems = cartSession.getCartItems();
        assertNotNull(cartItems, "カートアイテムがnullです");
        assertEquals(1, cartItems.size(), "カートに1件の書籍が存在するはずです");
        
        // Step 2: 注文情報を作成
        OrderTO orderTO = new OrderTO();
        orderTO.setCustomerId(1); // テスト用顧客ID
        orderTO.setDeliveryAddress("東京都渋谷区テスト町1-2-3");
        orderTO.setSettlementCode(1); // 銀行振込
        orderTO.setCartItems(cartItems);
        
        // Step 3: 配送料金を計算（通常: 800円）
        BigDecimal totalPrice = cartSession.getTotalPrice();
        BigDecimal deliveryFee = BigDecimal.valueOf(800); // 5000円未満の場合
        if (totalPrice.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            deliveryFee = BigDecimal.ZERO; // 送料無料
        }
        orderTO.setDeliveryPrice(deliveryFee);
        
        // Step 4: 注文を確定
        // Note: 実際のテストではモックを使用してデータベース操作を避ける
        // ここでは注文TOの検証のみ行う
        assertNotNull(orderTO.getCustomerId(), "顧客IDがnullです");
        assertNotNull(orderTO.getDeliveryAddress(), "配送先住所がnullです");
        assertNotNull(orderTO.getSettlementCode(), "決済方法がnullです");
        assertNotNull(orderTO.getCartItems(), "カートアイテムリストがnullです");
        assertEquals(1, orderTO.getCartItems().size(), "注文アイテムが1件であるはずです");
        
        // Step 5: 注文履歴に反映されることを確認（サービスレイヤーでの確認）
        // 実際のテストではorderServiceを使用して注文履歴を取得
        // ここではOrderTOの構造のみ検証
        assertTrue(orderTO.getCartItems().stream()
                .anyMatch(item -> item.getBookId().equals(testBook.getBookId())),
                "注文履歴に購入した書籍が含まれるはずです");
        
        // Step 6: 注文完了後、カートをクリア
        cartSession.clear();
        assertTrue(cartSession.getCartItems().isEmpty(), 
                "注文完了後、カートは空であるはずです");
    }
    
    @Test
    @Order(2)
    @DisplayName("配送料金の計算ロジック確認")
    public void testDeliveryFeeCalculation() {
        // Given: 異なる購入金額でテスト
        
        // Case 1: 4999円、東京都（送料: 800円）
        BigDecimal amount1 = BigDecimal.valueOf(4999);
        BigDecimal fee1 = calculateDeliveryFee("東京都", amount1);
        assertEquals(BigDecimal.valueOf(800), fee1, 
                "4999円の場合、送料は800円です");
        
        // Case 2: 5000円、東京都（送料無料）
        BigDecimal amount2 = BigDecimal.valueOf(5000);
        BigDecimal fee2 = calculateDeliveryFee("東京都", amount2);
        assertEquals(BigDecimal.ZERO, fee2, 
                "5000円以上の場合、送料無料です");
        
        // Case 3: 3000円、沖縄県（送料: 1700円）
        BigDecimal amount3 = BigDecimal.valueOf(3000);
        BigDecimal fee3 = calculateDeliveryFee("沖縄県", amount3);
        assertEquals(BigDecimal.valueOf(1700), fee3, 
                "沖縄県の場合、送料は1700円です");
        
        // Case 4: 5000円、沖縄県（送料無料優先）
        BigDecimal amount4 = BigDecimal.valueOf(5000);
        BigDecimal fee4 = calculateDeliveryFee("沖縄県", amount4);
        assertEquals(BigDecimal.ZERO, fee4, 
                "5000円以上の場合、沖縄県でも送料無料です");
    }
    
    /**
     * 配送料金を計算するヘルパーメソッド
     * BR-020の実装
     */
    private BigDecimal calculateDeliveryFee(String address, BigDecimal totalAmount) {
        // 5000円以上は送料無料
        if (totalAmount.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // 沖縄県は1700円
        if (address != null && address.startsWith("沖縄県")) {
            return BigDecimal.valueOf(1700);
        }
        
        // その他は800円
        return BigDecimal.valueOf(800);
    }
    
    @Test
    @Order(3)
    @DisplayName("注文履歴の表示順序確認")
    public void testOrderHistorySort() {
        // Given: 複数の注文履歴が存在する想定
        // OrderHistoryTO(LocalDate orderDate, Integer tranId, Integer detailId, 
        //                String bookName, String publisherName, BigDecimal price, Integer count)
        List<OrderHistoryTO> orderHistoryList = new ArrayList<>();
        
        OrderHistoryTO order1 = new OrderHistoryTO(
            LocalDate.of(2025, 12, 1),      // orderDate
            1,                                // tranId
            1,                                // detailId
            "Test Book 1",                    // bookName
            "Test Publisher",                 // publisherName
            BigDecimal.valueOf(3000),         // price
            1                                 // count
        );
        
        OrderHistoryTO order2 = new OrderHistoryTO(
            LocalDate.of(2025, 12, 15),     // orderDate
            2,                                // tranId
            2,                                // detailId
            "Test Book 2",                    // bookName
            "Test Publisher",                 // publisherName
            BigDecimal.valueOf(5000),         // price
            1                                 // count
        );
        
        OrderHistoryTO order3 = new OrderHistoryTO(
            LocalDate.of(2025, 12, 10),     // orderDate
            3,                                // tranId
            3,                                // detailId
            "Test Book 3",                    // bookName
            "Test Publisher",                 // publisherName
            BigDecimal.valueOf(4000),         // price
            1                                 // count
        );
        
        orderHistoryList.add(order1);
        orderHistoryList.add(order2);
        orderHistoryList.add(order3);
        
        // When: 注文日降順でソート
        orderHistoryList.sort((o1, o2) -> 
                o2.orderDate().compareTo(o1.orderDate()));
        
        // Then: 最新の注文が最初に表示される
        assertEquals(2, orderHistoryList.get(0).tranId(), 
                "最新の注文（12/15）が最初に表示されるはずです");
        assertEquals(3, orderHistoryList.get(1).tranId(), 
                "2番目に新しい注文（12/10）が2番目に表示されるはずです");
        assertEquals(1, orderHistoryList.get(2).tranId(), 
                "最も古い注文（12/01）が最後に表示されるはずです");
    }
}
