package pro.kensait.backoffice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.BookTO;
import pro.kensait.backoffice.dao.BookDao;
import pro.kensait.backoffice.dao.BookDaoCriteria;
import pro.kensait.backoffice.entity.Book;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 書籍サービス
 * 
 * 書籍検索のビジネスロジックを提供する
 */
@ApplicationScoped
@Transactional
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Inject
    private BookDao bookDao;

    @Inject
    private BookDaoCriteria bookDaoCriteria;

    /**
     * 全書籍を取得（論理削除を除外）
     * 
     * @return 書籍TOのリスト
     */
    public List<BookTO> getBooksAll() {
        logger.info("[ BookService#getBooksAll ]");
        
        List<Book> books = bookDao.findAll();
        
        return books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * 書籍IDで書籍を取得
     * 
     * @param bookId 書籍ID
     * @return 書籍TO、存在しない場合はnull
     */
    public BookTO getBook(Integer bookId) {
        logger.info("[ BookService#getBook ] bookId: {}", bookId);
        
        Book book = bookDao.findById(bookId);
        
        if (book == null) {
            logger.warn("[ BookService#getBook ] Book not found: {}", bookId);
            return null;
        }
        
        logger.info("[ BookService#getBook ] Success: bookId={}", bookId);
        return convertToTO(book);
    }

    /**
     * カテゴリIDで書籍を検索（JPQL）
     * 
     * @param categoryId カテゴリID（0の場合は全カテゴリ）
     * @return 書籍TOのリスト
     */
    public List<BookTO> searchBook(Integer categoryId) {
        logger.info("[ BookService#searchBook ] categoryId: {}", categoryId);
        
        List<Book> books = bookDao.searchByJpql(categoryId, null);
        
        return books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * キーワードで書籍を検索（JPQL）
     * 
     * @param keyword キーワード
     * @return 書籍TOのリスト
     */
    public List<BookTO> searchBook(String keyword) {
        logger.info("[ BookService#searchBook ] keyword: {}", keyword);
        
        List<Book> books = bookDao.searchByJpql(null, keyword);
        
        return books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリIDとキーワードで書籍を検索（JPQL）
     * 
     * @param categoryId カテゴリID（0の場合は全カテゴリ）
     * @param keyword キーワード
     * @return 書籍TOのリスト
     */
    public List<BookTO> searchBook(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBook ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        List<Book> books = bookDao.searchByJpql(categoryId, keyword);
        
        return books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリIDとキーワードで書籍を検索（Criteria API）
     * 
     * @param categoryId カテゴリID（nullまたは0の場合は全カテゴリ）
     * @param keyword キーワード（nullまたは空の場合は条件なし）
     * @return 書籍TOのリスト
     */
    public List<BookTO> searchBookWithCriteria(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBookWithCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        List<Book> books = bookDaoCriteria.searchByCriteria(categoryId, keyword);
        
        return books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * BookエンティティをBookTOに変換
     * 
     * @param book Bookエンティティ
     * @return BookTO
     */
    private BookTO convertToTO(Book book) {
        if (book == null) {
            return null;
        }
        
        // カテゴリ情報を変換
        BookTO.CategoryInfo categoryInfo = null;
        if (book.getCategory() != null) {
            categoryInfo = new BookTO.CategoryInfo(
                book.getCategory().getCategoryId(),
                book.getCategory().getCategoryName()
            );
        }
        
        // 出版社情報を変換
        BookTO.PublisherInfo publisherInfo = null;
        if (book.getPublisher() != null) {
            publisherInfo = new BookTO.PublisherInfo(
                book.getPublisher().getPublisherId(),
                book.getPublisher().getPublisherName()
            );
        }
        
        return new BookTO(
            book.getBookId(),
            book.getBookName(),
            book.getAuthor(),
            book.getPrice(),
            book.getImageUrl(),
            book.getQuantity(),
            book.getVersion(),
            categoryInfo,
            publisherInfo
        );
    }
}
