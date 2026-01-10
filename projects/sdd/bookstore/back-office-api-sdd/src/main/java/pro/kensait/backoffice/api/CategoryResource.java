package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.CategoryTO;
import pro.kensait.backoffice.service.CategoryService;

import java.util.List;

/**
 * カテゴリAPIリソース
 * 
 * ベースパス: /api/categories
 */
@Path("/categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private CategoryService categoryService;

    /**
     * 全カテゴリ取得
     * 
     * GET /api/categories
     * 
     * @return カテゴリTOのリスト（配列形式）
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");
        
        List<CategoryTO> categories = categoryService.getAllCategories();
        
        logger.info("[ CategoryResource#getAllCategories ] Returning {} categories", categories.size());
        return Response.ok(categories).build();
    }

    /**
     * カテゴリ詳細取得
     * 
     * GET /api/categories/{id}
     * 
     * @param id カテゴリID
     * @return カテゴリTO、存在しない場合は404 Not Found
     */
    @GET
    @Path("/{id}")
    public Response getCategoryById(@PathParam("id") Integer id) {
        logger.info("[ CategoryResource#getCategoryById ] id: {}", id);
        
        CategoryTO category = categoryService.getCategoryById(id);
        
        if (category == null) {
            logger.warn("[ CategoryResource#getCategoryById ] Category not found: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Not Found", "カテゴリが見つかりません"))
                    .build();
        }
        
        logger.info("[ CategoryResource#getCategoryById ] Category found: {}", id);
        return Response.ok(category).build();
    }

    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {}
}
