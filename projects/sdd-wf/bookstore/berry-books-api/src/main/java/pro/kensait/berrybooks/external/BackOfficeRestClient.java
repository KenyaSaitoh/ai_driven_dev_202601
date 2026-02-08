package pro.kensait.berrybooks.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.CategoryTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.util.List;

/**
 * back-office-api連携クライアント
 */
@ApplicationScoped
public class BackOfficeRestClient {
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);
    
    @Inject
    @RestClient
    private BackOfficeApi backOfficeApi;
    
    /**
     * 書籍一覧を取得する
     * 
     * @return 書籍一覧
     */
    public List<BookTO> findAllBooks() {
        logger.info("[ BackOfficeRestClient#findAllBooks ] Calling back-office-api");
        List<BookTO> books = backOfficeApi.findAllBooks();
        logger.debug("[ BackOfficeRestClient#findAllBooks ] Found {} books", books.size());
        return books;
    }
    
    /**
     * 書籍を検索する
     * 
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    public BookTO findBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findBookById ] bookId={}", bookId);
        return backOfficeApi.findBookById(bookId);
    }
    
    /**
     * 書籍を検索する（JPQL版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍一覧
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] categoryId={}, keyword={}",
            categoryId, keyword);
        List<BookTO> books = backOfficeApi.searchBooksJpql(categoryId, keyword);
        logger.debug("[ BackOfficeRestClient#searchBooksJpql ] Found {} books", books.size());
        return books;
    }

    /**
     * 書籍を検索する（Criteria API版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍一覧
     */
    public List<BookTO> searchBooksCriteria(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksCriteria ] categoryId={}, keyword={}",
            categoryId, keyword);
        List<BookTO> books = backOfficeApi.searchBooksCriteria(categoryId, keyword);
        logger.debug("[ BackOfficeRestClient#searchBooksCriteria ] Found {} books", books.size());
        return books;
    }

    /**
     * カテゴリ一覧を取得する
     *
     * @return カテゴリ一覧
     */
    public List<CategoryTO> findAllCategories() {
        logger.info("[ BackOfficeRestClient#findAllCategories ] Calling back-office-api");
        List<CategoryTO> categories = backOfficeApi.findAllCategories();
        logger.debug("[ BackOfficeRestClient#findAllCategories ] Found {} categories", categories.size());
        return categories;
    }

    /**
     * 在庫を検索する
     *
     * @param bookId 書籍ID
     * @return 在庫情報
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] bookId={}", bookId);
        return backOfficeApi.findStockById(bookId);
    }
    
    /**
     * 在庫を更新する（楽観的ロック対応）
     * 
     * @param bookId 書籍ID
     * @param version バージョン番号
     * @param newQuantity 新しい在庫数
     * @return 更新後の在庫情報
     */
    public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) {
        logger.info("[ BackOfficeRestClient#updateStock ] bookId={}, version={}, newQuantity={}", 
            bookId, version, newQuantity);
        StockTO result = backOfficeApi.updateStock(bookId, version, newQuantity);
        logger.info("[ BackOfficeRestClient#updateStock ] Stock updated successfully, new version={}", 
            result.getVersion());
        return result;
    }
}