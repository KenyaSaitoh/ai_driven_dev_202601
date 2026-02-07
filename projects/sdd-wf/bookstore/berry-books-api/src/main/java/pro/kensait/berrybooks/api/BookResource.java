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
import java.util.Map;

/**
 * 書籍APIエンドポイント（プロキシ）
 * 
 * back-office-apiから書籍情報を取得し、フロントエンドに提供する。
 * 
 * @since 1.0.0
 */
@Path("/books")
@ApplicationScoped
public class BookResource {
    
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);
    
    @Inject
    private BackOfficeRestClient backOfficeRestClient;
    
    /**
     * 全書籍を在庫情報と共に取得する
     * 
     * @return 書籍リスト
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");
        
        try {
            List<BookTO> books = backOfficeRestClient.getAllBooks();
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getAllBooks ] Error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 指定された書籍IDの詳細情報を取得する
     * 
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    @GET
    @Path("/{bookId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookById(@PathParam("bookId") Integer bookId) {
        logger.info("[ BookResource#getBookById ] bookId={}", bookId);
        
        try {
            BookTO book = backOfficeRestClient.getBookById(bookId);
            
            if (book == null) {
                logger.warn("[ BookResource#getBookById ] Book not found: bookId={}", bookId);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            return Response.ok(book).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getBookById ] Error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * カテゴリIDまたはキーワードで書籍を検索する（JPQL使用）
     * 
     * @param categoryId カテゴリID（省略可）
     * @param keyword キーワード（省略可）
     * @return 書籍リスト
     */
    @GET
    @Path("/search/jpql")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchBooksJpql(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeRestClient.searchBooksJpql(categoryId, keyword);
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksJpql ] Error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * カテゴリIDまたはキーワードで書籍を検索する（Criteria API使用）
     * 
     * @param categoryId カテゴリID（省略可）
     * @param keyword キーワード（省略可）
     * @return 書籍リスト
     */
    @GET
    @Path("/search/criteria")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchBooksCriteria(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId={}, keyword={}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeRestClient.searchBooksCriteria(categoryId, keyword);
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("[ BookResource#searchBooksCriteria ] Error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * カテゴリ一覧をマップ形式で取得する
     * 
     * @return カテゴリマップ（カテゴリ名 -> カテゴリID）
     */
    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        logger.info("[ BookResource#getAllCategories ]");
        
        try {
            Map<String, Integer> categories = backOfficeRestClient.getAllCategories();
            return Response.ok(categories).build();
        } catch (Exception e) {
            logger.error("[ BookResource#getAllCategories ] Error: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
