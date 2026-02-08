package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;

import java.util.List;

/**
 * 書籍API（back-office-apiへのプロキシ転送）
 */
@Path("/books")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * 全書籍の一覧取得（在庫・カテゴリ・出版社情報含む）
     *
     * @return 200 OK + List&lt;BookTO&gt;
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ] Getting all books");

        try {
            List<BookTO> books = backOfficeClient.findAllBooks();
            logger.info("[ BookResource#getAllBooks ] Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getAllBooks ] Error occurred", e);
            throw e;
        }
    }

    /**
     * 書籍IDで書籍詳細を取得
     *
     * @param bookId 書籍ID
     * @return 200 OK + BookTO, 404 Not Found
     */
    @GET
    @Path("/{bookId}")
    public Response getBookById(@PathParam("bookId") Integer bookId) {
        logger.info("[ BookResource#getBookById ] bookId={}", bookId);

        try {
            BookTO book = backOfficeClient.findBookById(bookId);
            return Response.ok(book).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getBookById ] Error occurred", e);
            throw e;
        }
    }

    /**
     * カテゴリIDまたはキーワードで書籍検索（JPQL版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 200 OK + List&lt;BookTO&gt;
     */
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);

        try {
            List<BookTO> books = backOfficeClient.searchBooksJpql(categoryId, keyword);
            logger.info("[ BookResource#searchBooksJpql ] Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksJpql ] Error occurred", e);
            throw e;
        }
    }

    /**
     * カテゴリIDまたはキーワードで書籍検索（Criteria API版）
     *
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 200 OK + List&lt;BookTO&gt;
     */
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
        @QueryParam("categoryId") Integer categoryId,
        @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId={}, keyword={}", categoryId, keyword);

        try {
            List<BookTO> books = backOfficeClient.searchBooksCriteria(categoryId, keyword);
            logger.info("[ BookResource#searchBooksCriteria ] Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksCriteria ] Error occurred", e);
            throw e;
        }
    }
}
