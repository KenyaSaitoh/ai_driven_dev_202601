package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.CategoryTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * カテゴリAPI（back-office-apiへのプロキシ転送）
 */
@Path("/categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * カテゴリ一覧をマップ形式で取得
     *
     * @return 200 OK + Map&lt;String, Integer&gt;（キー: カテゴリ名、値: カテゴリID）
     */
    @GET
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ] Getting all categories");

        try {
            List<CategoryTO> categories = backOfficeClient.findAllCategories();
            logger.info("[ CategoryResource#getAllCategories ] Found {} categories", categories.size());

            // List<CategoryTO> を Map<String, Integer> に変換
            Map<String, Integer> categoryMap = categories.stream()
                .collect(Collectors.toMap(
                    CategoryTO::getCategoryName,
                    CategoryTO::getCategoryId
                ));

            return Response.ok(categoryMap).build();
        } catch (Exception e) {
            logger.error("[ CategoryResource#getAllCategories ] Error occurred", e);
            throw e;
        }
    }
}
