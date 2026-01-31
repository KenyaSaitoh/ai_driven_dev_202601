package pro.kensait.berrybooks.api;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * 画像API リソースクラス
 * WAR内の画像リソースを直接配信する
 */
@Path("/images")
@ApplicationScoped
public class ImageResource {
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);
    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    private static final String NO_IMAGE = "no-image.jpg";

    @Context
    private ServletContext servletContext;

    /**
     * 書籍表紙画像取得
     * GET /api/images/covers/{bookId}
     * 
     * @param bookId 書籍ID (画像ファイル名: {bookId}.jpg)
     * @return 画像データまたはno-image.jpg
     */
    @GET
    @Path("/covers/{bookId}")
    public Response getBookCover(@PathParam("bookId") Integer bookId) {
        logger.info("[ ImageResource#getBookCover ] bookId: {}", bookId);
        
        try {
            // 画像ファイルパス: /resources/images/covers/{bookId}.jpg
            String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
            InputStream imageStream = servletContext.getResourceAsStream(imagePath);
            
            // 画像が見つからない場合はno-imageを返す
            if (imageStream == null) {
                logger.warn("Image not found for bookId: {}, returning no-image", bookId);
                imagePath = IMAGE_BASE_PATH + NO_IMAGE;
                imageStream = servletContext.getResourceAsStream(imagePath);
                
                if (imageStream == null) {
                    logger.error("No-image file not found: {}", imagePath);
                    ErrorResponse errorResponse = new ErrorResponse(
                            Response.Status.NOT_FOUND.getStatusCode(),
                            "Not Found",
                            "画像が見つかりません",
                            "/api/images/covers/" + bookId
                    );
                    return Response.status(Response.Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(errorResponse)
                            .build();
                }
            }
            
            // 画像データを返す
            byte[] imageData = imageStream.readAllBytes();
            imageStream.close();
            
            return Response.ok(imageData)
                    .type("image/jpeg")
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error fetching image for bookId: " + bookId, e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "画像の取得に失敗しました",
                    "/api/images/covers/" + bookId
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }
}

