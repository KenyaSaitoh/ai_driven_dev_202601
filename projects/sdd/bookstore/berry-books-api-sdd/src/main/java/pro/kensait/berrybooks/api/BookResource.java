package pro.kensait.berrybooks.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;

/**
 * 書籍APIリソース（プロキシパターン）
 * 
 * 書籍の一覧取得、詳細取得、検索を提供する。
 * すべてのリクエストはback-office-apiに転送される。
 */
@Path("/books")
@ApplicationScoped
public class BookResource {

    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * 全書籍取得
     * 
     * @return 書籍リスト
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");
        
        try {
            List<BookTO> books = backOfficeClient.findAllBooks();
            logger.info("Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getAllBooks ] Error", e);
            throw e;
        }
    }

    /**
     * 書籍詳細取得
     * 
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookById(@PathParam("id") Integer bookId) {
        logger.info("[ BookResource#getBookById ] bookId={}", bookId);
        
        try {
            BookTO book = backOfficeClient.findBookById(bookId);
            if (book == null) {
                logger.warn("Book not found: bookId={}", bookId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"書籍が見つかりません\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            return Response.ok(book).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getBookById ] Error: bookId={}", bookId, e);
            throw e;
        }
    }

    /**
     * 書籍検索（JPQL版）
     * 
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍リスト
     */
    @GET
    @Path("/search/jpql")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchBooksJpql(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeClient.searchBooksJpql(categoryId, keyword);
            logger.info("Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksJpql ] Error", e);
            throw e;
        }
    }

    /**
     * 書籍検索（Criteria API版）
     * 
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return 検索結果の書籍リスト
     */
    @GET
    @Path("/search/criteria")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchBooksCriteria(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId={}, keyword={}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeClient.searchBooksCriteria(categoryId, keyword);
            logger.info("Found {} books", books.size());
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksCriteria ] Error", e);
            throw e;
        }
    }
}
