package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.OrderDetailResponse;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.dto.OrderResponse;
import pro.kensait.berrybooks.common.ErrorResponse;
import pro.kensait.berrybooks.common.exception.OutOfStockException;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.AuthenInfo;
import pro.kensait.berrybooks.service.order.OrderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注文APIエンドポイント
 * 
 * 認証必須エンドポイント:
 * - POST /api/orders（注文作成）
 * - GET /api/orders/history（注文履歴取得）
 * 
 * 認証不要エンドポイント:
 * - GET /api/orders/{tranId}（注文詳細取得）
 * - GET /api/orders/{tranId}/details/{detailId}（注文明細取得）
 */
@Path("/orders")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);
    
    @Inject
    private OrderService orderService;
    
    @Inject
    private AuthenInfo authenInfo;
    
    /**
     * 注文を作成する（認証必須）
     * 
     * @param request 注文リクエスト
     * @return 201 Created + OrderResponse
     * @throws OutOfStockException 在庫不足（409 Conflict）
     * @throws OptimisticLockException 楽観的ロック競合（409 Conflict）
     */
    @POST
    public Response createOrder(@Valid OrderRequest request) 
            throws OutOfStockException, OptimisticLockException {
        
        logger.info("[ OrderResource#createOrder ] START");
        
        // 認証チェック
        Integer customerId = authenInfo.getCustomerId();
        if (customerId == null) {
            logger.warn("[ OrderResource#createOrder ] Unauthorized access");
            ErrorResponse error = new ErrorResponse(
                401, "Unauthorized", "認証が必要です", "/api/orders"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }
        
        logger.info("[ OrderResource#createOrder ] customerId={}", customerId);
        
        // 注文作成
        OrderTran orderTran = orderService.createOrder(request, customerId);
        
        // レスポンス生成
        OrderResponse response = convertToOrderResponse(orderTran);
        
        logger.info("[ OrderResource#createOrder ] END: orderTranId={}", 
                    orderTran.getOrderTranId());
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }
    
    /**
     * 注文履歴を取得する（認証必須）
     * 
     * @return 200 OK + List<OrderResponse>
     */
    @GET
    @Path("/history")
    public Response getOrderHistory() {
        logger.info("[ OrderResource#getOrderHistory ] START");
        
        // 認証チェック
        Integer customerId = authenInfo.getCustomerId();
        if (customerId == null) {
            logger.warn("[ OrderResource#getOrderHistory ] Unauthorized access");
            ErrorResponse error = new ErrorResponse(
                401, "Unauthorized", "認証が必要です", "/api/orders/history"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }
        
        logger.info("[ OrderResource#getOrderHistory ] customerId={}", customerId);
        
        // 注文履歴取得
        List<OrderTran> orderHistory = orderService.getOrderHistory(customerId);
        
        // レスポンス生成
        List<OrderResponse> response = orderHistory.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        
        logger.info("[ OrderResource#getOrderHistory ] END: ordersCount={}", 
                    response.size());
        
        return Response.ok(response).build();
    }
    
    /**
     * 注文詳細を取得する（認証不要）
     * 
     * @param tranId 注文トランザクションID
     * @return 200 OK + OrderResponse、404 Not Found
     */
    @GET
    @Path("/{tranId}")
    public Response getOrderById(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderById ] START: tranId={}", tranId);
        
        // 注文詳細取得
        OrderTran orderTran = orderService.getOrderById(tranId);
        
        if (orderTran == null) {
            logger.warn("[ OrderResource#getOrderById ] Order not found: tranId={}", tranId);
            ErrorResponse error = new ErrorResponse(
                404, "Not Found", "注文が見つかりません", "/api/orders/" + tranId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        
        // レスポンス生成
        OrderResponse response = convertToOrderResponse(orderTran);
        
        logger.info("[ OrderResource#getOrderById ] END: orderTranId={}", 
                    orderTran.getOrderTranId());
        
        return Response.ok(response).build();
    }
    
    /**
     * 注文明細を取得する（認証不要）
     * 
     * @param tranId 注文トランザクションID
     * @param detailId 注文明細ID
     * @return 200 OK + OrderDetailResponse、404 Not Found
     */
    @GET
    @Path("/{tranId}/details/{detailId}")
    public Response getOrderDetailById(
            @PathParam("tranId") Integer tranId,
            @PathParam("detailId") Integer detailId) {
        
        logger.info("[ OrderResource#getOrderDetailById ] START: tranId={}, detailId={}", 
                    tranId, detailId);
        
        // 注文詳細取得
        OrderTran orderTran = orderService.getOrderById(tranId);
        
        if (orderTran == null) {
            logger.warn("[ OrderResource#getOrderDetailById ] Order not found: tranId={}", 
                        tranId);
            ErrorResponse error = new ErrorResponse(
                404, "Not Found", "注文が見つかりません", 
                "/api/orders/" + tranId + "/details/" + detailId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        
        // 注文明細を検索
        OrderDetail orderDetail = orderTran.getOrderDetails().stream()
                .filter(detail -> detail.getId().getOrderDetailId().equals(detailId))
                .findFirst()
                .orElse(null);
        
        if (orderDetail == null) {
            logger.warn("[ OrderResource#getOrderDetailById ] Order detail not found: tranId={}, detailId={}", 
                        tranId, detailId);
            ErrorResponse error = new ErrorResponse(
                404, "Not Found", "注文明細が見つかりません", 
                "/api/orders/" + tranId + "/details/" + detailId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        
        // レスポンス生成
        OrderDetailResponse response = convertToOrderDetailResponse(orderDetail);
        
        logger.info("[ OrderResource#getOrderDetailById ] END: tranId={}, detailId={}", 
                    tranId, detailId);
        
        return Response.ok(response).build();
    }
    
    /**
     * OrderTranをOrderResponseに変換する
     * 
     * @param orderTran 注文トランザクション
     * @return 注文レスポンス
     */
    private OrderResponse convertToOrderResponse(OrderTran orderTran) {
        List<OrderDetailResponse> orderDetails = orderTran.getOrderDetails().stream()
                .map(this::convertToOrderDetailResponse)
                .collect(Collectors.toList());
        
        return new OrderResponse(
                orderTran.getOrderTranId(),
                orderTran.getOrderDate(),
                orderTran.getCustomerId(),
                orderTran.getTotalPrice(),
                orderTran.getDeliveryPrice(),
                orderTran.getDeliveryAddress(),
                orderTran.getSettlementType(),
                orderDetails
        );
    }
    
    /**
     * OrderDetailをOrderDetailResponseに変換する
     * 
     * @param orderDetail 注文明細
     * @return 注文明細レスポンス
     */
    private OrderDetailResponse convertToOrderDetailResponse(OrderDetail orderDetail) {
        return new OrderDetailResponse(
                orderDetail.getId().getOrderDetailId(),
                orderDetail.getBookId(),
                orderDetail.getBookName(),
                orderDetail.getPublisherName(),
                orderDetail.getPrice(),
                orderDetail.getCount()
        );
    }
}
