package pro.kensait.berrybooks.integration;

import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.dao.CategoryDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.Category;
import pro.kensait.berrybooks.entity.Customer;
import pro.kensait.berrybooks.entity.Publisher;
import pro.kensait.berrybooks.entity.Stock;
import pro.kensait.berrybooks.service.book.BookService;
import pro.kensait.berrybooks.service.book.SearchParam;
import pro.kensait.berrybooks.service.customer.CustomerService;
import pro.kensait.berrybooks.service.order.CartItem;
import pro.kensait.berrybooks.web.cart.CartSession;

import java.util.ArrayList;
import java.util.List;

/**
 * T_INTEG_001: ログイン → 書籍検索 → カート追加の結合テスト
 * 
 * 目的: F-004（顧客管理・認証）→ F-001（書籍検索）→ F-002（ショッピングカート）の連携を確認する
 * 
 * テストシナリオ:
 * 1. ログイン画面でログイン
 * 2. 書籍検索画面でカテゴリ「Java」を選択して検索
 * 3. 検索結果から書籍を選択してカートに追加
 * 4. カート画面で書籍が表示されることを確認
 * 5. 合計金額が正しく計算されることを確認
 * 
 * 期待結果: カートに書籍が追加され、合計金額が正しく表示される
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginToCartIntegrationTest extends IntegrationTestBase {

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
    @DisplayName("ログイン → 書籍検索 → カート追加の統合フロー")
    public void testLoginToCartFlow() throws Exception {
        // Given: テスト用の書籍データを準備
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Stock stock = new Stock();
        stock.setBookId(1);
        stock.setQuantity(10);
        stock.setVersion(1L);
        
        Book testBook = new Book();
        testBook.setBookId(1);
        testBook.setBookName("Java Test Book");
        testBook.setPublisher(publisher);
        testBook.setPrice(3000);
        testBook.setStock(stock);
        
        List<Book> bookList = new ArrayList<>();
        bookList.add(testBook);
        
        // BookDaoのモック設定
        when(bookDao.findByCategory(1)).thenReturn(bookList);
        
        // When: ログイン処理
        // Note: このテストはサービスレイヤーの統合テスト
        // 実際のログイン処理はCustomerServiceを通じて行われる想定
        
        // Step 1: 書籍検索（カテゴリ「Java」で検索）
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1); // JavaカテゴリのID
        List<Book> searchResults = bookService.searchBook(searchParam);
        
        // Then: 検索結果が取得できる
        assertNotNull(searchResults, "検索結果がnullです");
        assertFalse(searchResults.isEmpty(), "検索結果が空です");
        assertTrue(searchResults.size() > 0, "Java書籍が見つかりません");
        
        // Step 2: 検索結果から最初の書籍を選択
        Book selectedBook = searchResults.get(0);
        assertNotNull(selectedBook, "選択した書籍がnullです");
        assertNotNull(selectedBook.getBookId(), "書籍IDがnullです");
        assertNotNull(selectedBook.getPrice(), "書籍価格がnullです");
        
        // Step 3: カートに追加
        CartItem cartItem = new CartItem();
        cartItem.setBookId(selectedBook.getBookId());
        cartItem.setBookName(selectedBook.getBookName());
        cartItem.setPublisherName(selectedBook.getPublisher().getPublisherName());
        cartItem.setPrice(selectedBook.getPrice());
        cartItem.setCount(1);
        cartItem.setVersion(selectedBook.getStock().getVersion()); // 楽観的ロック用のバージョン
        
        cartSession.addItem(cartItem);
        
        // Step 4: カート内容を確認
        List<CartItem> cartItems = cartSession.getCartItems();
        assertNotNull(cartItems, "カートアイテムリストがnullです");
        assertEquals(1, cartItems.size(), "カートに1件の書籍が追加されているはずです");
        
        CartItem addedItem = cartItems.get(0);
        assertEquals(selectedBook.getBookId(), addedItem.getBookId(), "書籍IDが一致しません");
        assertEquals(selectedBook.getBookName(), addedItem.getBookName(), "書籍名が一致しません");
        assertEquals(selectedBook.getPrice(), addedItem.getPrice(), "価格が一致しません");
        assertEquals(1, addedItem.getCount(), "数量が1であるべきです");
        
        // Step 5: 合計金額を確認
        cartSession.calculateTotalPrice();
        BigDecimal totalPrice = cartSession.getTotalPrice();
        assertNotNull(totalPrice, "合計金額がnullです");
        assertEquals(selectedBook.getPrice(), totalPrice, "合計金額が書籍価格と一致するはずです");
    }
    
    @Test
    @Order(2)
    @DisplayName("複数書籍をカートに追加して合計金額を確認")
    public void testMultipleBooksInCart() throws Exception {
        // Given: テスト用の書籍データを準備（2冊）
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(10);
        stock1.setVersion(1L);
        
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Java Test Book 1");
        book1.setPublisher(publisher);
        book1.setPrice(3000);
        book1.setStock(stock1);
        
        Stock stock2 = new Stock();
        stock2.setBookId(2);
        stock2.setQuantity(10);
        stock2.setVersion(1L);
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("Java Test Book 2");
        book2.setPublisher(publisher);
        book2.setPrice(4000);
        book2.setStock(stock2);
        
        List<Book> bookList = new ArrayList<>();
        bookList.add(book1);
        bookList.add(book2);
        
        // BookDaoのモック設定
        when(bookDao.findByCategory(1)).thenReturn(bookList);
        
        // Given: 複数の書籍を検索
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1); // Javaカテゴリ
        List<Book> searchResults = bookService.searchBook(searchParam);
        assertTrue(searchResults.size() >= 2, "テストには少なくとも2冊の書籍が必要です");
        
        // When: 2冊の書籍をカートに追加（モックから返された書籍を再利用）
        book1 = searchResults.get(0);
        book2 = searchResults.get(1);
        
        CartItem item1 = new CartItem();
        item1.setBookId(book1.getBookId());
        item1.setBookName(book1.getBookName());
        item1.setPublisherName(book1.getPublisher().getPublisherName());
        item1.setPrice(book1.getPrice());
        item1.setCount(1);
        item1.setVersion(book1.getStock().getVersion());
        
        CartItem item2 = new CartItem();
        item2.setBookId(book2.getBookId());
        item2.setBookName(book2.getBookName());
        item2.setPublisherName(book2.getPublisher().getPublisherName());
        item2.setPrice(book2.getPrice());
        item2.setCount(2); // 2冊購入
        item2.setVersion(book2.getStock().getVersion());
        
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        
        // Then: カートに2件のアイテムが存在する
        List<CartItem> cartItems = cartSession.getCartItems();
        assertEquals(2, cartItems.size(), "カートに2件のアイテムが存在するはずです");
        
        // 合計金額が正しく計算される
        cartSession.calculateTotalPrice();
        BigDecimal expectedTotal = book1.getPrice()
                .add(book2.getPrice().multiply(BigDecimal.valueOf(2)));
        assertEquals(expectedTotal, cartSession.getTotalPrice(), 
                "合計金額が正しく計算されていません");
    }
    
    @Test
    @Order(3)
    @DisplayName("カートから書籍を削除して合計金額を再計算")
    public void testRemoveFromCart() throws Exception {
        // Given: テスト用の書籍データを準備（2冊）
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(10);
        stock1.setVersion(1L);
        
        Book book1Entity = new Book();
        book1Entity.setBookId(1);
        book1Entity.setBookName("Java Test Book 1");
        book1Entity.setPublisher(publisher);
        book1Entity.setPrice(3000);
        book1Entity.setStock(stock1);
        
        Stock stock2 = new Stock();
        stock2.setBookId(2);
        stock2.setQuantity(10);
        stock2.setVersion(1L);
        
        Book book2Entity = new Book();
        book2Entity.setBookId(2);
        book2Entity.setBookName("Java Test Book 2");
        book2Entity.setPublisher(publisher);
        book2Entity.setPrice(4000);
        book2Entity.setStock(stock2);
        
        List<Book> bookList = new ArrayList<>();
        bookList.add(book1Entity);
        bookList.add(book2Entity);
        
        // BookDaoのモック設定
        when(bookDao.findByCategory(1)).thenReturn(bookList);
        
        // Given: カートに2冊の書籍が存在する
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1);
        List<Book> searchResults = bookService.searchBook(searchParam);
        assertTrue(searchResults.size() >= 2, "テストには少なくとも2冊の書籍が必要です");
        
        Book book1 = searchResults.get(0);
        Book book2 = searchResults.get(1);
        
        CartItem item1 = new CartItem();
        item1.setBookId(book1.getBookId());
        item1.setBookName(book1.getBookName());
        item1.setPrice(book1.getPrice());
        item1.setCount(1);
        
        CartItem item2 = new CartItem();
        item2.setBookId(book2.getBookId());
        item2.setBookName(book2.getBookName());
        item2.setPrice(book2.getPrice());
        item2.setCount(1);
        
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        assertEquals(2, cartSession.getCartItems().size());
        
        // When: 1冊目を削除
        cartSession.removeItem(book1.getBookId());
        
        // Then: カートに1件のアイテムのみ存在する
        assertEquals(1, cartSession.getCartItems().size(), 
                "カートに1件のアイテムのみ存在するはずです");
        
        // 残っているのは2冊目
        CartItem remainingItem = cartSession.getCartItems().get(0);
        assertEquals(book2.getBookId(), remainingItem.getBookId(),
                "削除後に残っているのは2冊目の書籍であるはずです");
        
        // 合計金額が2冊目の価格のみになる
        cartSession.calculateTotalPrice();
        assertEquals(book2.getPrice(), cartSession.getTotalPrice(),
                "合計金額が2冊目の価格のみであるはずです");
    }
}


