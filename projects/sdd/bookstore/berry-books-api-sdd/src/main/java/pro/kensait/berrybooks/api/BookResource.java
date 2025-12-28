package pro.kensait.berrybooks.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.service.book.BookService;
import pro.kensait.berrybooks.service.category.CategoryService;

import java.util.List;
import java.util.Map;

/**
 * 書籍API Resource
 * 
 * ベースパス: /api/books
 * 
 * エンドポイント:
 * - GET /: 書籍一覧取得
 * - GET /{id}: 書籍詳細取得
 * - GET /search: 書籍検索
 * - GET /categories: カテゴリ一覧取得
 */
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {
    
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);
    
    @Inject
    private BookService bookService;
    
    @Inject
    private CategoryService categoryService;
    
    /**
     * 書籍一覧取得
     * 
     * @return 書籍リスト
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ] Retrieving all books");
        
        List<Book> books = bookService.getAllBooks();
        
        return Response.ok(books).build();
    }
    
    /**
     * 書籍詳細取得
     * 
     * @param id 書籍ID
     * @return 書籍詳細
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Integer id) {
        logger.info("[ BookResource#getBookById ] bookId={}", id);
        
        Book book = bookService.getBookById(id);
        
        if (book == null) {
            logger.warn("[ BookResource#getBookById ] Book not found: {}", id);
            ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                "書籍が見つかりません",
                "/api/books/" + id
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
        
        return Response.ok(book).build();
    }
    
    /**
     * 書籍検索
     * 
     * @param categoryId カテゴリID（0または未指定=全カテゴリ）
     * @param keyword キーワード（書籍名、著者名で部分一致検索）
     * @return 書籍リスト
     */
    @GET
    @Path("/search")
    public Response searchBooks(
            @QueryParam("categoryId") @DefaultValue("0") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooks ] categoryId={}, keyword={}", categoryId, keyword);
        
        List<Book> books = bookService.searchBooks(categoryId, keyword);
        
        return Response.ok(books).build();
    }
    
    /**
     * カテゴリ一覧取得
     * 
     * @return カテゴリMap<カテゴリ名, カテゴリID>
     */
    @GET
    @Path("/categories")
    public Response getCategories() {
        logger.info("[ BookResource#getCategories ] Retrieving categories");
        
        Map<String, Integer> categoryMap = categoryService.getCategoryMap();
        
        return Response.ok(categoryMap).build();
    }
}

