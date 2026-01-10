package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.StockTO;
import pro.kensait.backoffice.api.dto.StockUpdateRequest;
import pro.kensait.backoffice.dao.StockDao;
import pro.kensait.backoffice.entity.Book;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 在庫APIリソース
 * 
 * ベースパス: /api/stocks
 * 楽観的ロック対応
 */
@Path("/stocks")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StockResource {

    private static final Logger logger = LoggerFactory.getLogger(StockResource.class);

    @Inject
    private StockDao stockDao;

    /**
     * 全在庫取得
     * 
     * GET /api/stocks
     * 
     * @return 在庫TOのリスト（配列形式）
     */
    @GET
    public Response getAllStocks() {
        logger.info("[ StockResource#getAllStocks ]");
        
        List<Book> books = stockDao.findAll();
        List<StockTO> stocks = books.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
        
        logger.info("[ StockResource#getAllStocks ] Returning {} stocks", stocks.size());
        return Response.ok(stocks).build();
    }

    /**
     * 在庫取得（書籍ID指定）
     * 
     * GET /api/stocks/{bookId}
     * 
     * @param bookId 書籍ID
     * @return 在庫TO、存在しない場合は404 Not Found
     */
    @GET
    @Path("/{bookId}")
    public Response getStock(@PathParam("bookId") Integer bookId) {
        logger.info("[ StockResource#getStock ] bookId: {}", bookId);
        
        Book book = stockDao.findById(bookId);
        
        if (book == null) {
            logger.warn("[ StockResource#getStock ] Stock not found: bookId={}", bookId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Not Found", "在庫情報が見つかりません"))
                    .build();
        }
        
        StockTO stock = convertToTO(book);
        
        logger.info("[ StockResource#getStock ] Stock found: bookId={}", bookId);
        return Response.ok(stock).build();
    }

    /**
     * 在庫更新（楽観的ロック対応）
     * 
     * PUT /api/stocks/{bookId}
     * 
     * @param bookId 書籍ID
     * @param request 在庫更新リクエスト
     * @return 更新後の在庫TO
     */
    @PUT
    @Path("/{bookId}")
    @Transactional
    public Response updateStock(@PathParam("bookId") Integer bookId, 
                                 @Valid StockUpdateRequest request) {
        logger.info("[ StockResource#updateStock ] bookId: {}, quantity: {}, version: {}", 
                   bookId, request.quantity(), request.version());
        
        // 1. 在庫情報を取得
        Book book = stockDao.findById(bookId);
        
        if (book == null) {
            logger.warn("[ StockResource#updateStock ] Stock not found: bookId={}", bookId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Not Found", "在庫情報が見つかりません"))
                    .build();
        }
        
        // 2. バージョンチェック（楽観的ロック）
        if (!book.getVersion().equals(request.version())) {
            logger.warn("[ StockResource#updateStock ] Optimistic lock failure: bookId={}, expected version={}, actual version={}", 
                       bookId, request.version(), book.getVersion());
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("OptimisticLockException", 
                                            "在庫が他のユーザーによって更新されました。再度お試しください。"))
                    .build();
        }
        
        // 3. 在庫数を更新
        book.setQuantity(request.quantity());
        
        // 4. 更新を実行（JPAが自動的にversionをインクリメント）
        Book updatedBook = stockDao.update(book);
        
        StockTO stock = convertToTO(updatedBook);
        
        logger.info("[ StockResource#updateStock ] Success: bookId={}, quantity={}, version={}", 
                   updatedBook.getBookId(), updatedBook.getQuantity(), updatedBook.getVersion());
        
        return Response.ok(stock).build();
    }

    /**
     * BookエンティティをStockTOに変換
     * 
     * @param book Bookエンティティ
     * @return StockTO
     */
    private StockTO convertToTO(Book book) {
        if (book == null) {
            return null;
        }
        
        return new StockTO(
            book.getBookId(),
            book.getBookName(),
            book.getQuantity(),
            book.getVersion()
        );
    }

    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {}
}
