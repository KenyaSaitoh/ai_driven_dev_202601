package pro.kensait.berrybooks.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * 画像配信API（WAR内リソースを直接配信）
 */
@Path("/images")
@ApplicationScoped
public class ImageResource {
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);

    private static final String RESOURCE_PATH_PREFIX = "/resources/images/";
    private static final String FALLBACK_IMAGE = "no-image.jpg";

    @Context
    private ServletContext servletContext;

    /**
     * 指定されたファイル名の画像をWAR内から取得して配信する
     *
     * @param filename 画像ファイル名
     * @return 200 OK + 画像バイナリ、または400/500エラー
     */
    @GET
    @Path("/{filename}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getImage(@PathParam("filename") String filename) {
        logger.info("[ ImageResource#getImage ] filename={}", filename);

        // パストラバーサル攻撃対策
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            logger.warn("[ ImageResource#getImage ] Invalid filename: {}", filename);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Invalid filename\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

        try {
            // WAR内リソースから画像を取得
            String resourcePath = RESOURCE_PATH_PREFIX + filename;
            logger.debug("[ ImageResource#getImage ] Resource path: {}", resourcePath);

            InputStream imageStream = servletContext.getResourceAsStream(resourcePath);

            // ファイルが存在しない場合はフォールバック画像を返す
            if (imageStream == null) {
                logger.warn("[ ImageResource#getImage ] Image not found: {}, fallback to {}", filename, FALLBACK_IMAGE);
                String fallbackPath = RESOURCE_PATH_PREFIX + FALLBACK_IMAGE;
                imageStream = servletContext.getResourceAsStream(fallbackPath);

                if (imageStream == null) {
                    logger.error("[ ImageResource#getImage ] Fallback image not found: {}", FALLBACK_IMAGE);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Image not found\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                }

                // フォールバック画像のContent-Typeを設定
                return Response.ok(imageStream)
                    .type(getContentType(FALLBACK_IMAGE))
                    .build();
            }

            // Content-Typeを拡張子から判定
            String contentType = getContentType(filename);
            logger.debug("[ ImageResource#getImage ] Content-Type: {}", contentType);

            // 画像バイナリを返却
            return Response.ok(imageStream)
                .type(contentType)
                .build();

        } catch (Exception e) {
            logger.error("[ ImageResource#getImage ] Error occurred", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Internal server error\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    /**
     * ファイル名の拡張子からContent-Typeを判定する
     *
     * @param filename ファイル名
     * @return Content-Type
     */
    private String getContentType(String filename) {
        if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
