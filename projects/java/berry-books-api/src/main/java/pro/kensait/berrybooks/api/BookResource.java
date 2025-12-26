package pro.kensait.berrybooks.api;

import java.util.List;
import java.util.Map;

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
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.service.book.BookService;
import pro.kensait.berrybooks.service.category.CategoryService;

/**
 * 書籍API リソースクラス
 */
@Path("/books")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BookService bookService;

    @Inject
    private CategoryService categoryService;

    /**
     * 書籍一覧取得
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");

        List<Book> books = bookService.getBooksAll();
        return Response.ok(books).build();
    }

    /**
     * 書籍詳細取得
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Integer id) {
        logger.info("[ BookResource#getBookById ] id: {}", id);

        Book book = bookService.getBook(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"書籍が見つかりません\"}")
                    .build();
        }
        return Response.ok(book).build();
    }

    /**
     * 書籍検索
     */
    @GET
    @Path("/search")
    public Response searchBooks(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooks ] categoryId: {}, keyword: {}", categoryId, keyword);

        List<Book> books;

        if (categoryId != null && categoryId != 0) {
            if (keyword != null && !keyword.isEmpty()) {
                books = bookService.searchBook(categoryId, keyword);
            } else {
                books = bookService.searchBook(categoryId);
            }
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                books = bookService.searchBook(keyword);
            } else {
                books = bookService.getBooksAll();
            }
        }

        return Response.ok(books).build();
    }

    /**
     * カテゴリ一覧取得
     */
    @GET
    @Path("/categories")
    public Response getAllCategories() {
        logger.info("[ BookResource#getAllCategories ]");

        Map<String, Integer> categories = categoryService.getCategoryMap();
        return Response.ok(categories).build();
    }
}

