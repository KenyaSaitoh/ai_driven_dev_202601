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
import pro.kensait.berrybooks.external.BackOfficeRestClient;

/**
 * カテゴリAPIリソース（プロキシパターン）
 * 
 * カテゴリの一覧取得を提供する。
 * すべてのリクエストはback-office-apiに転送される。
 */
@Path("/categories")
@ApplicationScoped
public class CategoryResource {

    private static final Logger logger = LoggerFactory.getLogger(CategoryResource.class);

    @Inject
    private BackOfficeRestClient backOfficeClient;

    /**
     * 全カテゴリ取得
     * 
     * @return カテゴリマップ（カテゴリ名 → カテゴリID）
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        logger.info("[ CategoryResource#getAllCategories ]");
        
        try {
            Map<String, Integer> categories = backOfficeClient.findAllCategories();
            logger.info("Found {} categories", categories.size());
            return Response.ok(categories).build();
        } catch (Exception e) {
            logger.error("[ CategoryResource#getAllCategories ] Error", e);
            throw e;
        }
    }
}
