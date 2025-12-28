package pro.kensait.berrybooks.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.*;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.SecuredResource;
import pro.kensait.berrybooks.service.order.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 注文API Resource
 * 
 * ベースパス: /api/orders
 * 
 * エンドポイント:
 * - POST /: 注文作成（認証必須）
 * - GET /history: 注文履歴取得（認証必須）
 * - GET /{tranId}: 注文詳細取得
 * - GET /{tranId}/details/{detailId}: 注文明細取得
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);
    
    @Inject
    private OrderService orderService;
    
    @Inject
    private SecuredResource securedResource;
    
    /**
     * 注文作成（認証必須）
     * 
     * @param request 注文リクエスト
     * @return 注文レスポンス
     */
    @POST
    public Response createOrder(@Valid OrderRequest request) {
        Integer customerId = securedResource.getCustomerId();
        logger.info("[ OrderResource#createOrder ] customerId={}", customerId);
        
        // CartItemリストを変換
        List<CartItem> cartItems = new ArrayList<>();
        for (CartItemRequest item : request.cartItems()) {
            CartItem cartItem = new CartItem(
                item.bookId(),
                item.bookName(),
                item.publisherName(),
                item.price(),
                item.count(),
                item.version()
            );
            cartItems.add(cartItem);
        }
        
        // OrderTOを作成
        OrderTO orderTO = new OrderTO(
            customerId,
            cartItems,
            request.totalPrice(),
            request.deliveryPrice(),
            request.deliveryAddress(),
            request.settlementType()
        );
        
        // 注文処理（トランザクション）
        OrderSummaryTO summary = orderService.orderBooks(orderTO);
        
        // 注文詳細を取得してレスポンスを作成
        OrderTran orderTran = orderService.getOrderDetail(summary.orderTranId());
        
        List<OrderDetailResponse> orderDetails = new ArrayList<>();
        for (OrderDetail od : orderTran.getOrderDetails()) {
            OrderDetailResponse detailResponse = new OrderDetailResponse(
                od.getOrderDetailId(),
                od.getBook().getBookId(),
                od.getBook().getBookName(),
                od.getBook().getPublisher().getPublisherName(),
                od.getPrice(),
                od.getCount()
            );
            orderDetails.add(detailResponse);
        }
        
        OrderResponse response = new OrderResponse(
            summary.orderTranId(),
            summary.orderDate(),
            summary.totalPrice(),
            summary.deliveryPrice(),
            summary.deliveryAddress(),
            summary.settlementType(),
            orderDetails
        );
        
        return Response.ok(response).build();
    }
    
    /**
     * 注文履歴取得（認証必須）
     * 
     * @return 注文履歴リスト
     */
    @GET
    @Path("/history")
    public Response getOrderHistory() {
        Integer customerId = securedResource.getCustomerId();
        logger.info("[ OrderResource#getOrderHistory ] customerId={}", customerId);
        
        List<OrderHistoryTO> history = orderService.getOrderHistory(customerId);
        
        return Response.ok(history).build();
    }
    
    /**
     * 注文詳細取得
     * 
     * @param tranId 注文トランザクションID
     * @return 注文詳細
     */
    @GET
    @Path("/{tranId}")
    public Response getOrderDetail(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderDetail ] orderTranId={}", tranId);
        
        OrderTran orderTran = orderService.getOrderDetail(tranId);
        
        if (orderTran == null) {
            logger.warn("[ OrderResource#getOrderDetail ] Order not found: {}", tranId);
            ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                "注文が見つかりません",
                "/api/orders/" + tranId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
        
        // レスポンスを作成
        List<OrderDetailResponse> orderDetails = new ArrayList<>();
        for (OrderDetail od : orderTran.getOrderDetails()) {
            OrderDetailResponse detailResponse = new OrderDetailResponse(
                od.getOrderDetailId(),
                od.getBook().getBookId(),
                od.getBook().getBookName(),
                od.getBook().getPublisher().getPublisherName(),
                od.getPrice(),
                od.getCount()
            );
            orderDetails.add(detailResponse);
        }
        
        OrderResponse response = new OrderResponse(
            orderTran.getOrderTranId(),
            orderTran.getOrderDate(),
            orderTran.getTotalPrice(),
            orderTran.getDeliveryPrice(),
            orderTran.getDeliveryAddress(),
            orderTran.getSettlementType(),
            orderDetails
        );
        
        return Response.ok(response).build();
    }
    
    /**
     * 注文明細取得
     * 
     * @param tranId 注文トランザクションID
     * @param detailId 注文明細ID
     * @return 注文明細
     */
    @GET
    @Path("/{tranId}/details/{detailId}")
    public Response getOrderDetailItem(
            @PathParam("tranId") Integer tranId,
            @PathParam("detailId") Integer detailId) {
        logger.info("[ OrderResource#getOrderDetailItem ] orderTranId={}, orderDetailId={}", 
                   tranId, detailId);
        
        OrderDetail orderDetail = orderService.getOrderDetailItem(tranId, detailId);
        
        if (orderDetail == null) {
            logger.warn("[ OrderResource#getOrderDetailItem ] Order detail not found: tranId={}, detailId={}", 
                       tranId, detailId);
            ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                "注文明細が見つかりません",
                "/api/orders/" + tranId + "/details/" + detailId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
        
        OrderDetailResponse response = new OrderDetailResponse(
            orderDetail.getOrderDetailId(),
            orderDetail.getBook().getBookId(),
            orderDetail.getBook().getBookName(),
            orderDetail.getBook().getPublisher().getPublisherName(),
            orderDetail.getPrice(),
            orderDetail.getCount()
        );
        
        return Response.ok(response).build();
    }
}

