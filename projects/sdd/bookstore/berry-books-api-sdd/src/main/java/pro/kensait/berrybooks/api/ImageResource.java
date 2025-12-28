package pro.kensait.berrybooks.api;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 画像API Resource
 * 
 * ベースパス: /api/images
 * 
 * エンドポイント:
 * - GET /covers/{bookId}: 書籍表紙画像取得
 */
@Path("/images")
public class ImageResource {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);
    
    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    private static final String NO_IMAGE_PATH = IMAGE_BASE_PATH + "no-image.jpg";
    
    @Context
    private ServletContext servletContext;
    
    /**
     * 書籍表紙画像取得
     * 
     * @param bookId 書籍ID
     * @return 画像バイナリ（image/jpeg）
     */
    @GET
    @Path("/covers/{bookId}")
    @Produces("image/jpeg")
    public Response getCoverImage(@PathParam("bookId") Integer bookId) {
        logger.info("[ ImageResource#getCoverImage ] bookId={}", bookId);
        
        // 画像ファイルパス（WAR内の相対パス）
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        InputStream inputStream = servletContext.getResourceAsStream(imagePath);
        
        // 画像が存在しない場合はno-image.jpgを返す
        if (inputStream == null) {
            logger.warn("[ ImageResource#getCoverImage ] Image not found: {}, using fallback", imagePath);
            inputStream = servletContext.getResourceAsStream(NO_IMAGE_PATH);
            
            // フォールバック画像も存在しない場合
            if (inputStream == null) {
                logger.error("[ ImageResource#getCoverImage ] Fallback image not found: {}", NO_IMAGE_PATH);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("画像が見つかりません")
                        .build();
            }
        }
        
        try {
            byte[] imageData = inputStream.readAllBytes();
            inputStream.close();
            
            logger.info("[ ImageResource#getCoverImage ] Image returned: size={} bytes", imageData.length);
            
            return Response.ok(imageData)
                    .type("image/jpeg")
                    .build();
        } catch (IOException e) {
            logger.error("[ ImageResource#getCoverImage ] Failed to read image file: {}", imagePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("画像の読み込みに失敗しました")
                    .build();
        }
    }
}

