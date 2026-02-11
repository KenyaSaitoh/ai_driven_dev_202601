package pro.kensait.backoffice.api;

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
import pro.kensait.backoffice.service.category.CategoryService;

/**
 * Books Stock API - カテゴリAPIリソースクラス
 */
@Path("/categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private CategoryService categoryService;

    /**
     * カテゴリ一覧取得（マップ形式：カテゴリ名→IDのマップ）
     * 注: BFF側が {"Java": 1, "SpringBoot": 2} 形式を期待しているため、マップ形式で返す
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");

        Map<String, Integer> categoryMap = categoryService.getCategoryMap();
        return Response.ok(categoryMap).build();
    }
}

