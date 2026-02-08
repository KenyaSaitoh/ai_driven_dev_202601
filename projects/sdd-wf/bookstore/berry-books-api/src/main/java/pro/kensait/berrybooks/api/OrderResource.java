package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.OrderDetailResponse;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.dto.OrderResponse;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.AuthenticatedUser;
import pro.kensait.berrybooks.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注文APIリソース
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
    private AuthenticatedUser authenticatedUser;
    
    /**
     * 注文を作成する
     * 
     * @param orderRequest 注文リクエスト
     * @return 201 Created + OrderResponse
     */
    @POST
    public Response createOrder(OrderRequest orderRequest) {
        logger.info("[ OrderResource#createOrder ] customerId={}", authenticatedUser.getCustomerId());
        
        try {
            OrderTran orderTran = orderService.createOrder(orderRequest);
            OrderResponse response = convertToResponse(orderTran);
            
            return Response
                .status(Response.Status.CREATED)
                .entity(response)
                .build();
        } catch (RuntimeException e) {
            logger.error("[ OrderResource#createOrder ] Error: {}", e.getMessage(), e);
            
            if (e.getMessage().contains("在庫不足")) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("在庫不足", e.getMessage()))
                    .build();
            }
            
            if (e.getMessage().contains("OptimisticLock")) {
                return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("楽観的ロック失敗", "在庫が更新されました。再度お試しください。"))
                    .build();
            }
            
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("注文失敗", "注文の作成に失敗しました"))
                .build();
        }
    }
    
    /**
     * ログイン中の顧客の注文履歴を取得する
     * 
     * @return 200 OK + List<OrderResponse>
     */
    @GET
    @Path("/history")
    public Response getOrderHistory() {
        Integer customerId = authenticatedUser.getCustomerId();
        logger.info("[ OrderResource#getOrderHistory ] customerId={}", customerId);
        
        List<OrderTran> orderTrans = orderService.getOrderHistory(customerId);
        List<OrderResponse> responses = orderTrans.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * 注文詳細を取得する
     * 
     * @param tranId 注文トランザクションID
     * @return 200 OK + OrderResponse
     */
    @GET
    @Path("/{tranId}")
    public Response getOrderDetail(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderDetail ] orderTranId={}", tranId);
        
        OrderTran orderTran = orderService.getOrderDetail(tranId);
        
        if (orderTran == null) {
            logger.warn("[ OrderResource#getOrderDetail ] Order not found: orderTranId={}", tranId);
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("注文が見つかりません", "指定された注文は存在しません"))
                .build();
        }
        
        OrderResponse response = convertToResponse(orderTran);
        return Response.ok(response).build();
    }
    
    /**
     * OrderTranエンティティをOrderResponseに変換する
     * 
     * @param orderTran 注文トランザクションエンティティ
     * @return 注文レスポンスDTO
     */
    private OrderResponse convertToResponse(OrderTran orderTran) {
        List<OrderDetailResponse> orderDetails = orderTran.getOrderDetails().stream()
            .map(this::convertToDetailResponse)
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
     * OrderDetailエンティティをOrderDetailResponseに変換する
     * 
     * @param orderDetail 注文明細エンティティ
     * @return 注文明細レスポンスDTO
     */
    private OrderDetailResponse convertToDetailResponse(OrderDetail orderDetail) {
        return new OrderDetailResponse(
            orderDetail.getOrderDetailId(),
            orderDetail.getBookId(),
            orderDetail.getBookName(),
            orderDetail.getPublisherName(),
            orderDetail.getPrice(),
            orderDetail.getCount()
        );
    }
    
    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {
    }
}