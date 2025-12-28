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

