package pro.kensait.berrybooks.service.book;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.common.MessageUtil;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// 書籍検索を行うサービスクラス
@ApplicationScoped
@Transactional
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(
            BookService.class);

    @Inject
    private BookDao bookDao;

    // サービスメソッド：書籍検索（主キー検索）
    public Book getBook(Integer bookId) {
        logger.info("[ BookService#getBook ]");
        
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new RuntimeException(MessageUtil.get("error.book.not-found") + bookId);
        }
        return book;
    }

    // サービスメソッド：書籍検索（全件検索）
    public List<Book> getBooksAll() {
        logger.info("[ BookService#getBooksAll ]");
        return bookDao.findAll();
    }

    // サービスメソッド：書籍検索（カテゴリIDとキーワードによる条件検索）
    public List<Book> searchBook(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBook(categoryId, keyword) ]");
        return bookDao.query(categoryId, toLikeWord(keyword));
    }

    // サービスメソッド：書籍検索（カテゴリIDによる条件検索）
    public List<Book> searchBook(Integer categoryId) {
        logger.info("[ BookService#searchBook(categoryId) ]");
        return bookDao.queryByCategory(categoryId);
    }

    // サービスメソッド：書籍検索（キーワードによる条件検索）
    public List<Book> searchBook(String keyword) {
        logger.info("[ BookService#searchBook(keyword) ]");
        return bookDao.queryByKeyword(toLikeWord(keyword));
    }

    // サービスメソッド：書籍検索（動的クエリの構築）
    public List<Book> searchBookWithCriteria(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBookWithCriteria ]");
        
        String likeKeyword = (keyword != null && !keyword.isEmpty()) 
                ? toLikeWord(keyword) : null;
        
        return bookDao.searchWithCriteria(categoryId, likeKeyword);
    }

    private String toLikeWord(String keyword) {
        return "%" + keyword + "%";
    }
}


