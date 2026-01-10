package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
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
 * 書籍表紙画像の配信を担当する。
 * WAR内の静的リソースをServletContextを通じて配信する。
 * 
 * 認証: 不要（公開API）
 */
@Path("/images")
@ApplicationScoped
public class ImageResource {

    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);
    
    /**
     * 画像ベースパス（WAR内の絶対パス）
     */
    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    
    /**
     * フォールバック画像ファイル名
     */
    private static final String NO_IMAGE_FILENAME = "no-image.jpg";
    
    @Context
    private ServletContext servletContext;

    /**
     * 書籍表紙画像取得
     * 
     * 指定された書籍IDの表紙画像を取得する。
     * 画像が存在しない場合は、フォールバック画像（no-image.jpg）を返却する。
     * 
     * @param bookIdStr 書籍ID（文字列、"no-image"の場合はフォールバック画像）
     * @return 画像バイナリデータ（image/jpeg）
     */
    @GET
    @Path("/covers/{bookId}")
    @Produces("image/jpeg")
    public Response getCoverImage(@PathParam("bookId") String bookIdStr) {
        logger.info("[ ImageResource#getCoverImage ] bookId={}", bookIdStr);
        
        // "no-image" リクエストの場合、直接フォールバック画像を返す
        if ("no-image".equals(bookIdStr)) {
            return getNoImage();
        }
        
        // 画像パスを構築
        String imagePath = IMAGE_BASE_PATH + bookIdStr + ".jpg";
        
        // ServletContextからInputStreamを取得
        InputStream inputStream = servletContext.getResourceAsStream(imagePath);
        
        // 画像が存在しない場合、フォールバック画像を返す
        if (inputStream == null) {
            logger.warn("[ ImageResource#getCoverImage ] Image not found: {}, returning fallback image", imagePath);
            String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;
            inputStream = servletContext.getResourceAsStream(fallbackPath);
            
            // フォールバック画像も存在しない場合
            if (inputStream == null) {
                logger.error("[ ImageResource#getCoverImage ] Fallback image not found: {}", fallbackPath);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Image not found")
                               .build();
            }
        }
        
        try {
            // InputStreamから全バイトを読み取り
            byte[] imageData = inputStream.readAllBytes();
            
            // 画像バイナリを返却
            return Response.ok(imageData, "image/jpeg").build();
            
        } catch (IOException e) {
            logger.error("[ ImageResource#getCoverImage ] Failed to read image: {}", imagePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Failed to read image")
                           .build();
        } finally {
            // InputStreamをクローズ
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("[ ImageResource#getCoverImage ] Failed to close InputStream", e);
                }
            }
        }
    }
    
    /**
     * フォールバック画像（no-image）を取得
     * 
     * @return no-image.jpg のバイナリデータ
     */
    private Response getNoImage() {
        logger.info("[ ImageResource#getNoImage ] Returning fallback image");
        
        String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;
        InputStream inputStream = servletContext.getResourceAsStream(fallbackPath);
        
        if (inputStream == null) {
            logger.error("[ ImageResource#getNoImage ] Fallback image not found: {}", fallbackPath);
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Fallback image not found")
                           .build();
        }
        
        try {
            byte[] imageData = inputStream.readAllBytes();
            return Response.ok(imageData, "image/jpeg").build();
        } catch (IOException e) {
            logger.error("[ ImageResource#getNoImage ] Failed to read fallback image", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Failed to read fallback image")
                           .build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("[ ImageResource#getNoImage ] Failed to close InputStream", e);
                }
            }
        }
    }
}
