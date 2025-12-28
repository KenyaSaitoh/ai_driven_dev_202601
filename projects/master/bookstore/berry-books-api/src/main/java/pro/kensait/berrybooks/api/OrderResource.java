package pro.kensait.berrybooks.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.security.SecuredResource;
import pro.kensait.berrybooks.service.delivery.DeliveryFeeService;
import pro.kensait.berrybooks.service.order.CartItem;
import pro.kensait.berrybooks.service.order.OrderHistoryTO;
import pro.kensait.berrybooks.service.order.OrderServiceIF;
import pro.kensait.berrybooks.service.order.OrderTO;

/**
 * 注文API リソースクラス
 */
@Path("/orders")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);

    @Inject
    private OrderServiceIF orderService;

    @Inject
    private SecuredResource securedResource;

    @Inject
    private BackOfficeRestClient backOfficeClient;

    @Inject
    private DeliveryFeeService deliveryFeeService;

    /**
     * 注文作成
     */
    @POST
    public Response createOrder(@Valid OrderRequest request) {
        logger.info("[ OrderResource#createOrder ]");

        // JWT認証チェック
        if (!securedResource.isAuthenticated()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "認証が必要です",
                    "/api/orders"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        Integer customerId = securedResource.getCustomerId();

        // CartItemRequestをCartItemに変換
        List<CartItem> cartItems = request.cartItems().stream()
                .map(item -> new CartItem(
                        item.bookId(),
                        item.bookName(),
                        item.publisherName(),
                        item.price(),
                        item.count(),
                        false,
                        item.version().longValue()))
                .collect(Collectors.toList());

        // サーバーサイドで配送料を計算
        BigDecimal calculatedDeliveryFee = deliveryFeeService.calculateDeliveryFee(
                request.deliveryAddress(), 
                request.totalPrice());
        
        logger.info("[ OrderResource ] Calculated delivery fee: {}", calculatedDeliveryFee);

        // OrderTOを生成（サーバーで計算した配送料を使用）
        OrderTO orderTO = new OrderTO(
                customerId,
                LocalDate.now(),
                cartItems,
                request.totalPrice(),
                calculatedDeliveryFee,  // クライアントの値ではなく、サーバーで計算した値を使用
                request.deliveryAddress(),
                request.settlementType()
        );

        try {
            // 注文処理を実行
            OrderTran orderTran = orderService.orderBooks(orderTO);

            // レスポンス生成
            OrderResponse response = convertToOrderResponse(orderTran);

            return Response.ok(response).build();

        } catch (Exception e) {
            // OutOfStockException, OptimisticLockExceptionは
            // ExceptionMapperで処理される
            throw e;
        }
    }

    /**
     * 注文履歴取得
     */
    @GET
    @Path("/history")
    public Response getOrderHistory() {
        logger.info("[ OrderResource#getOrderHistory ]");

        // JWT認証チェック
        if (!securedResource.isAuthenticated()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "認証が必要です",
                    "/api/orders/history"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        Integer customerId = securedResource.getCustomerId();

        // 注文履歴を取得
        List<OrderHistoryTO> orderHistoryList = orderService.getOrderHistory2(customerId);

        // レスポンス生成（内部モデルから外部APIモデルへの変換）
        List<OrderHistoryResponse> response = orderHistoryList.stream()
                .map(history -> new OrderHistoryResponse(
                        history.orderDate(),
                        history.tranId(),      // tranId → orderTranId（API層で明示的に）
                        history.detailId(),    // detailId → orderDetailId（API層で明示的に）
                        history.bookName(),
                        history.publisherName(),
                        history.price(),
                        history.count()))
                .collect(Collectors.toList());

        return Response.ok(response).build();
    }

    /**
     * 注文詳細取得
     */
    @GET
    @Path("/{tranId}")
    public Response getOrderDetail(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderDetail ] tranId: {}", tranId);

        OrderTran orderTran = orderService.getOrderTran(tranId);
        if (orderTran == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "Not Found",
                    "注文が見つかりません",
                    "/api/orders/" + tranId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        OrderResponse response = convertToOrderResponse(orderTran);
        return Response.ok(response).build();
    }

    /**
     * 注文明細取得
     */
    @GET
    @Path("/{tranId}/details/{detailId}")
    public Response getOrderDetailItem(
            @PathParam("tranId") Integer tranId,
            @PathParam("detailId") Integer detailId) {
        logger.info("[ OrderResource#getOrderDetailItem ] tranId: {}, detailId: {}", tranId, detailId);

        OrderDetail orderDetail = orderService.getOrderDetail(tranId, detailId);
        if (orderDetail == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "Not Found",
                    "注文明細が見つかりません",
                    "/api/orders/" + tranId + "/details/" + detailId
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        // 書籍情報を外部APIから取得（出版社名を取得するため）
        BookTO book = backOfficeClient.findBookById(orderDetail.getBookId());
        String publisherName = (book != null && book.getPublisher() != null) 
                ? book.getPublisher().getPublisherName() : "不明";

        OrderDetailResponse response = new OrderDetailResponse(
                orderDetail.getOrderDetailId(),
                orderDetail.getBookId(),
                orderDetail.getBookName(),
                publisherName,
                orderDetail.getPrice(),
                orderDetail.getCount()
        );

        return Response.ok(response).build();
    }

    /**
     * OrderTranをOrderResponseに変換
     */
    private OrderResponse convertToOrderResponse(OrderTran orderTran) {
        List<OrderDetailResponse> orderDetails = new ArrayList<>();
        if (orderTran.getOrderDetails() != null) {
            orderDetails = orderTran.getOrderDetails().stream()
                    .map(detail -> {
                        // 書籍情報を外部APIから取得（出版社名を取得するため）
                        BookTO book = backOfficeClient.findBookById(detail.getBookId());
                        String publisherName = (book != null && book.getPublisher() != null) 
                                ? book.getPublisher().getPublisherName() : "不明";
                        
                        return new OrderDetailResponse(
                                detail.getOrderDetailId(),
                                detail.getBookId(),
                                detail.getBookName(),
                                publisherName,
                                detail.getPrice(),
                                detail.getCount());
                    })
                    .collect(Collectors.toList());
        }

        return new OrderResponse(
                orderTran.getOrderTranId(),
                orderTran.getOrderDate(),
                orderTran.getTotalPrice(),
                orderTran.getDeliveryPrice(),
                orderTran.getDeliveryAddress(),
                orderTran.getSettlementType(),
                orderDetails
        );
    }
}

