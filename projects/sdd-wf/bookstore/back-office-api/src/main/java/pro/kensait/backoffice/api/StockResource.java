package pro.kensait.backoffice.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.backoffice.api.dto.StockTO;
import pro.kensait.backoffice.api.dto.StockUpdateRequest;
import pro.kensait.backoffice.dao.StockDao;
import pro.kensait.backoffice.entity.Stock;

/**
 * Books Stock API - 在庫APIリソースクラス
 */
@Path("/stocks")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class StockResource {
    private static final Logger logger = LoggerFactory.getLogger(StockResource.class);

    @Inject
    private StockDao stockDao;

    /**
     * 在庫一覧取得
     */
    @GET
    public Response getAllStocks() {
        logger.info("[ StockResource#getAllStocks ]");

        java.util.List<Stock> stocks = stockDao.findAll();
        java.util.List<StockTO> stockTOs = stocks.stream()
                .map(this::convertToStockTO)
                .collect(java.util.stream.Collectors.toList());
        return Response.ok(stockTOs).build();
    }

    /**
     * 在庫情報取得
     */
    @GET
    @Path("{bookId}")
    public Response getStock(@PathParam("bookId") Integer bookId) {
        logger.info("[ StockResource#getStock ] bookId: {}", bookId);

        Stock stock = stockDao.findById(bookId);
        if (stock == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"在庫情報が見つかりません\"}")
                    .build();
        }
        StockTO stockTO = convertToStockTO(stock);
        return Response.ok(stockTO).build();
    }

    /**
     * 在庫更新（楽観的ロック対応）
     * 
     * @param bookId 書籍ID
     * @param request 在庫更新リクエスト（version, quantity）
     * @return 更新後の在庫情報
     */
    @PUT
    @Path("{bookId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStock(
            @PathParam("bookId") Integer bookId,
            StockUpdateRequest request) {
        logger.info("[ StockResource#updateStock ] bookId: {}, version: {}, quantity: {}", 
                bookId, request.getVersion(), request.getQuantity());

        try {
            // 既存の在庫エンティティを取得
            Stock stock = stockDao.findById(bookId);
            
            if (stock == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"在庫情報が見つかりません\"}")
                        .build();
            }

            // 楽観的ロックチェック：バージョンが一致するか確認
            if (!stock.getVersion().equals(request.getVersion())) {
                logger.warn("Optimistic lock failure for bookId: {}. Expected version: {}, Actual version: {}", 
                        bookId, request.getVersion(), stock.getVersion());
                throw new OptimisticLockException("Version mismatch");
            }

            // 在庫数を更新（JPAが自動的に更新を検出）
            stock.setQuantity(request.getQuantity());
            // バージョンは@Versionアノテーションにより自動的にインクリメントされる

            // トランザクションコミット時に自動的にUPDATE文が発行される
            // 明示的にmerge()を呼ぶ必要はない（管理下のエンティティなので）

            // 更新後の在庫情報を返す（versionがインクリメントされている）
            StockTO stockTO = convertToStockTO(stock);
            return Response.ok(stockTO).build();

        } catch (OptimisticLockException e) {
            logger.warn("Optimistic lock failure for bookId: {}", bookId, e);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"在庫が他のユーザーによって更新されました。再度お試しください。\"}")
                    .build();
        } catch (Exception e) {
            logger.error("Error updating stock for bookId: {}", bookId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"在庫更新中にエラーが発生しました。\"}")
                    .build();
        }
    }

    /**
     * StockエンティティをStockTOに変換するヘルパーメソッド
     */
    private StockTO convertToStockTO(Stock stock) {
        return new StockTO(
                stock.getBookId(),
                stock.getQuantity(),
                stock.getVersion()
        );
    }
}

