package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.PublisherTO;
import pro.kensait.backoffice.service.PublisherService;

import java.util.List;

/**
 * 出版社APIリソース
 * 
 * ベースパス: /api/publishers
 */
@Path("/publishers")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherResource {

    private static final Logger logger = LoggerFactory.getLogger(PublisherResource.class);

    @Inject
    private PublisherService publisherService;

    /**
     * 全出版社取得
     * 
     * GET /api/publishers
     * 
     * @return 出版社TOのリスト（配列形式）
     */
    @GET
    public Response getAllPublishers() {
        logger.info("[ PublisherResource#getAllPublishers ]");
        
        List<PublisherTO> publishers = publisherService.getAllPublishers();
        
        logger.info("[ PublisherResource#getAllPublishers ] Returning {} publishers", publishers.size());
        return Response.ok(publishers).build();
    }

    /**
     * 出版社詳細取得
     * 
     * GET /api/publishers/{id}
     * 
     * @param id 出版社ID
     * @return 出版社TO、存在しない場合は404 Not Found
     */
    @GET
    @Path("/{id}")
    public Response getPublisherById(@PathParam("id") Integer id) {
        logger.info("[ PublisherResource#getPublisherById ] id: {}", id);
        
        PublisherTO publisher = publisherService.getPublisherById(id);
        
        if (publisher == null) {
            logger.warn("[ PublisherResource#getPublisherById ] Publisher not found: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Not Found", "出版社が見つかりません"))
                    .build();
        }
        
        logger.info("[ PublisherResource#getPublisherById ] Publisher found: {}", id);
        return Response.ok(publisher).build();
    }

    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {}
}
