package pro.kensait.berrybooks.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.external.BackOfficeRestClient;

/**
 * カテゴリAPI リソースクラス（BFFプロキシ）
 * SPAからのリクエストを受け取り、back-office-apiに転送する
 */
@Path("/categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * カテゴリ一覧取得（BFFプロキシ - そのまま転送）
     * GET /api/categories
     * 注: back-office-apiから {"Java": 1, "SpringBoot": 2} 形式（名前→ID）を受け取りそのまま返す
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");
        
        try {
            Map<String, Integer> categories = backOfficeClient.findAllCategories();
            return Response.ok(categories).build();
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "カテゴリ一覧の取得に失敗しました",
                    "/api/categories"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }
}

