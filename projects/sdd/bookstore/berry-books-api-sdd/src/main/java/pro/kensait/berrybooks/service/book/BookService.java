package pro.kensait.berrybooks.service.book;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 書籍ビジネスロジックサービス
 */
@ApplicationScoped
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    @Inject
    private BookDao bookDao;
    
    /**
     * 全書籍を取得
     * 
     * @return 書籍リスト
     */
    public List<Book> getAllBooks() {
        logger.info("[ BookService#getAllBooks ] Retrieving all books");
        return bookDao.findAll();
    }
    
    /**
     * 書籍IDで書籍を取得
     * 
     * @param bookId 書籍ID
     * @return Book（見つからない場合はnull）
     */
    public Book getBookById(Integer bookId) {
        logger.info("[ BookService#getBookById ] bookId={}", bookId);
        return bookDao.findById(bookId);
    }
    
    /**
     * 書籍検索（SearchParamオブジェクト使用）
     * 
     * @param searchParam 検索パラメータ
     * @return 書籍リスト
     */
    public List<Book> searchBook(SearchParam searchParam) {
        logger.info("[ BookService#searchBook ] searchParam: categoryId={}, keyword={}", 
                   searchParam.getCategoryId(), searchParam.getKeyword());
        
        Integer categoryId = searchParam.getCategoryId();
        String keyword = searchParam.getKeyword();
        
        // 空白キーワードはnullとして扱う
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        
        // 条件に応じて適切なDAOメソッドを呼び出す
        if (categoryId != null && categoryId != 0 && keyword != null && !keyword.isEmpty()) {
            // カテゴリとキーワード両方指定
            return bookDao.findByCategoryAndKeyword(categoryId, keyword);
        } else if (keyword != null && !keyword.isEmpty()) {
            // キーワードのみ指定
            return bookDao.findByKeyword(keyword);
        } else if (categoryId != null && categoryId != 0) {
            // カテゴリのみ指定
            return bookDao.findByCategory(categoryId);
        } else {
            // 何も指定されていない場合は全件取得
            return bookDao.findAll();
        }
    }
    
    /**
     * 書籍IDで書籍を取得
     * 
     * @param bookId 書籍ID
     * @return Book（見つからない場合はnull）
     */
    public Book findBookById(Integer bookId) {
        logger.info("[ BookService#findBookById ] bookId={}", bookId);
        return bookDao.findById(bookId);
    }
    
    /**
     * 書籍検索
     * 
     * @param categoryId カテゴリID（0または未指定=全カテゴリ）
     * @param keyword キーワード（書籍名、著者名で部分一致検索）
     * @return 書籍リスト
     */
    public List<Book> searchBooks(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBooks ] categoryId={}, keyword={}", categoryId, keyword);
        return bookDao.searchBooks(categoryId, keyword);
    }
}

