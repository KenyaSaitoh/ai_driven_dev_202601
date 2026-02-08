package pro.kensait.berrybooks.external;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.CategoryTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.util.List;

/**
 * back-office-api連携用REST Clientインターフェース
 */
@RegisterRestClient(configKey = "back-office-api")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BackOfficeApi {
    
    /**
     * 書籍一覧を取得する
     * 
     * @return 書籍一覧
     */
    @GET
    @Path("/books")
    List<BookTO> findAllBooks();
    
    /**
     * 書籍を検索する
     * 
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    @GET
    @Path("/books/{bookId}")
    BookTO findBookById(@PathParam("bookId") Integer bookId);
    
    /**
     * 書籍を検索する（JPQL版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍一覧
     */
    @GET
    @Path("/books/search/jpql")
    List<BookTO> searchBooksJpql(
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("keyword") String keyword
    );

    /**
     * 書籍を検索する（Criteria API版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍一覧
     */
    @GET
    @Path("/books/search/criteria")
    List<BookTO> searchBooksCriteria(
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("keyword") String keyword
    );

    /**
     * カテゴリ一覧を取得する
     *
     * @return カテゴリ一覧
     */
    @GET
    @Path("/categories")
    List<CategoryTO> findAllCategories();

    /**
     * 在庫を検索する
     *
     * @param bookId 書籍ID
     * @return 在庫情報
     */
    @GET
    @Path("/stocks/{bookId}")
    StockTO findStockById(@PathParam("bookId") Integer bookId);
    
    /**
     * 在庫を更新する（楽観的ロック対応）
     * 
     * @param bookId 書籍ID
     * @param version バージョン番号
     * @param newQuantity 新しい在庫数
     * @return 更新後の在庫情報
     */
    @PUT
    @Path("/stocks/{bookId}")
    StockTO updateStock(
        @PathParam("bookId") Integer bookId,
        @QueryParam("version") Long version,
        @QueryParam("newQuantity") Integer newQuantity
    );
}