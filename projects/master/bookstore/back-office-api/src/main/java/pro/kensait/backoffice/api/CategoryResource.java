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
     * カテゴリ一覧取得
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");

        Map<String, Integer> categories = categoryService.getCategoryMap();
        return Response.ok(categories).build();
    }
}

