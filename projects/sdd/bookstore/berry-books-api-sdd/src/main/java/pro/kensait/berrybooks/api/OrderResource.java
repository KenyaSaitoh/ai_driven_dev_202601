package pro.kensait.berrybooks.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.api.dto.OrderDetailResponse;
import pro.kensait.berrybooks.api.dto.OrderHistoryResponse;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.dto.OrderResponse;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.AuthenContext;
import pro.kensait.berrybooks.service.order.OrderService;

/**
 * 注文APIリソース
 * 
 * 注文の作成、注文履歴の取得、注文詳細の取得を提供する。
 */
@Path("/orders")
@ApplicationScoped
public class OrderResource {

    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);

    @Inject
    private OrderService orderService;

    @Inject
    private AuthenContext authenContext;

    /**
     * 注文を作成する
     * 
     * @param request 注文リクエスト
     * @return 注文レスポンス
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(@Valid OrderRequest request) {
        Integer customerId = authenContext.getCustomerId();
        logger.info("[ OrderResource#createOrder ] customerId={}, cartItems={}", 
                customerId, request.cartItems().size());

        try {
            OrderTran orderTran = orderService.orderBooks(request, customerId);
            
            // OrderResponseを作成（詳細含む）
            OrderResponse response = orderService.findOrderById(orderTran.getOrderTranId());
            
            logger.info("[ OrderResource#createOrder ] Order created successfully: orderTranId={}", 
                    orderTran.getOrderTranId());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("[ OrderResource#createOrder ] Failed to create order", e);
            // 例外は各ExceptionMapperで処理される
            throw e;
        }
    }

    /**
     * 注文履歴を取得する
     * 
     * @return 注文履歴リスト
     */
    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderHistory() {
        Integer customerId = authenContext.getCustomerId();
        logger.info("[ OrderResource#getOrderHistory ] customerId={}", customerId);

        List<OrderHistoryResponse> history = orderService.findOrderHistoryByCustomerId(customerId);
        
        logger.info("[ OrderResource#getOrderHistory ] Found {} history items", history.size());
        
        return Response.ok(history).build();
    }

    /**
     * 注文詳細を取得する
     * 
     * @param tranId 注文ID
     * @return 注文詳細
     */
    @GET
    @Path("/{tranId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderById(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderById ] tranId={}", tranId);

        OrderResponse order = orderService.findOrderById(tranId);
        
        if (order == null) {
            logger.warn("[ OrderResource#getOrderById ] Order not found: tranId={}", tranId);
            ErrorResponse error = new ErrorResponse(
                404,
                "Not Found",
                "注文が見つかりません",
                "/api/orders/" + tranId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        logger.info("[ OrderResource#getOrderById ] Order found: tranId={}", tranId);
        
        return Response.ok(order).build();
    }

    /**
     * 注文明細を取得する
     * 
     * @param tranId 注文ID
     * @param detailId 明細ID
     * @return 注文明細
     */
    @GET
    @Path("/{tranId}/details/{detailId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderDetailById(
            @PathParam("tranId") Integer tranId,
            @PathParam("detailId") Integer detailId) {
        logger.info("[ OrderResource#getOrderDetailById ] tranId={}, detailId={}", tranId, detailId);

        OrderDetailResponse detail = orderService.findOrderDetailById(tranId, detailId);
        
        if (detail == null) {
            logger.warn("[ OrderResource#getOrderDetailById ] Order detail not found: tranId={}, detailId={}", 
                    tranId, detailId);
            ErrorResponse error = new ErrorResponse(
                404,
                "Not Found",
                "注文明細が見つかりません",
                "/api/orders/" + tranId + "/details/" + detailId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        logger.info("[ OrderResource#getOrderDetailById ] Order detail found: tranId={}, detailId={}", 
                tranId, detailId);
        
        return Response.ok(detail).build();
    }
}
