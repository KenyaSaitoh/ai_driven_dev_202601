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
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;

/**
 * 書籍API リソースクラス（BFFプロキシ）
 * SPAからのリクエストを受け取り、back-office-apiに転送する
 */
@Path("/books")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * 書籍一覧取得（BFFプロキシ - そのまま転送）
     * GET /api/books
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");
        
        try {
            List<BookTO> books = backOfficeClient.findAllBooks();
            // back-office-apiから受け取ったネスト構造をそのままSPAに返す
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("Error fetching books", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "書籍一覧の取得に失敗しました",
                    "/api/books"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * 書籍詳細取得（BFFプロキシ - そのまま転送）
     * GET /api/books/{id}
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Integer id) {
        logger.info("[ BookResource#getBookById ] id: {}", id);
        
        try {
            BookTO book = backOfficeClient.findBookById(id);
            if (book == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                        Response.Status.NOT_FOUND.getStatusCode(),
                        "Not Found",
                        "指定された書籍が見つかりません",
                        "/api/books/" + id
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }
            // back-office-apiから受け取ったネスト構造をそのままSPAに返す
            return Response.ok(book).build();
        } catch (Exception e) {
            logger.error("Error fetching book", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "書籍情報の取得に失敗しました",
                    "/api/books/" + id
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * 書籍検索（JPQL）（BFFプロキシ - そのまま転送）
     * GET /api/books/search/jpql?categoryId=&keyword=
     */
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeClient.searchBooksJpql(categoryId, keyword);
            // back-office-apiから受け取ったネスト構造をそのままSPAに返す
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("Error searching books", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "書籍検索に失敗しました",
                    "/api/books/search/jpql"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * 書籍検索（Criteria API）（BFFプロキシ - そのまま転送）
     * GET /api/books/search/criteria?categoryId=&keyword=
     */
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        try {
            List<BookTO> books = backOfficeClient.searchBooksCriteria(categoryId, keyword);
            // back-office-apiから受け取ったネスト構造をそのままSPAに返す
            return Response.ok(books).build();
        } catch (Exception e) {
            logger.error("Error searching books", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "書籍検索に失敗しました",
                    "/api/books/search/criteria"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }
}

