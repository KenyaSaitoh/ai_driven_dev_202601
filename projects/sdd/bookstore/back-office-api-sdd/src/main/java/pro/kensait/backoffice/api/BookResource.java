package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.BookTO;
import pro.kensait.backoffice.service.BookService;

import java.util.List;

/**
 * 書籍APIリソース
 * 
 * ベースパス: /api/books
 */
@Path("/books")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BookService bookService;

    /**
     * 全書籍取得
     * 
     * GET /api/books
     * 
     * @return 書籍TOのリスト
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");
        
        List<BookTO> books = bookService.getBooksAll();
        
        logger.info("[ BookResource#getAllBooks ] Returning {} books", books.size());
        return Response.ok(books).build();
    }

    /**
     * 書籍詳細取得
     * 
     * GET /api/books/{id}
     * 
     * @param id 書籍ID
     * @return 書籍TO、存在しない場合は404 Not Found
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Integer id) {
        logger.info("[ BookResource#getBookById ] id: {}", id);
        
        BookTO book = bookService.getBook(id);
        
        if (book == null) {
            logger.warn("[ BookResource#getBookById ] Book not found: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Not Found", "書籍が見つかりません"))
                    .build();
        }
        
        logger.info("[ BookResource#getBookById ] Book found: {}", id);
        return Response.ok(book).build();
    }

    /**
     * 書籍検索（デフォルト - JPQL）
     * 
     * GET /api/books/search
     * 
     * @param categoryId カテゴリID（オプション、0の場合は全カテゴリ）
     * @param keyword キーワード（オプション）
     * @return 書籍TOのリスト
     */
    @GET
    @Path("/search")
    public Response searchBooks(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooks ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        // 内部的にJPQL検索を使用
        return searchBooksJpql(categoryId, keyword);
    }

    /**
     * 書籍検索（JPQL）
     * 
     * GET /api/books/search/jpql
     * 
     * @param categoryId カテゴリID（オプション、0の場合は全カテゴリ）
     * @param keyword キーワード（オプション）
     * @return 書籍TOのリスト
     */
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        List<BookTO> books;
        
        // 検索条件に応じて適切なメソッドを呼び出し
        if (categoryId != null && categoryId > 0 && keyword != null && !keyword.isEmpty()) {
            // 両方指定
            books = bookService.searchBook(categoryId, keyword);
        } else if (categoryId != null && categoryId > 0) {
            // カテゴリIDのみ
            books = bookService.searchBook(categoryId);
        } else if (keyword != null && !keyword.isEmpty()) {
            // キーワードのみ
            books = bookService.searchBook(keyword);
        } else {
            // 条件なし（全件取得）
            books = bookService.getBooksAll();
        }
        
        logger.info("[ BookResource#searchBooksJpql ] Found {} books", books.size());
        return Response.ok(books).build();
    }

    /**
     * 書籍検索（Criteria API）
     * 
     * GET /api/books/search/criteria
     * 
     * @param categoryId カテゴリID（オプション、0の場合は全カテゴリ）
     * @param keyword キーワード（オプション）
     * @return 書籍TOのリスト
     */
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        List<BookTO> books = bookService.searchBookWithCriteria(categoryId, keyword);
        
        logger.info("[ BookResource#searchBooksCriteria ] Found {} books", books.size());
        return Response.ok(books).build();
    }

    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {}
}
