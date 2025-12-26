package pro.kensait.berrybooks.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * 画像配信API リソースクラス
 */
@Path("/images")
@ApplicationScoped
public class ImageResource {
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);

    @Context
    private ServletContext servletContext;

    /**
     * 書籍表紙画像取得
     */
    @GET
    @Path("/covers/{bookId}")
    @Produces("image/jpeg")
    public Response getBookCover(@PathParam("bookId") Integer bookId) {
        logger.info("[ ImageResource#getBookCover ] bookId: {}", bookId);

        try {
            // 画像ファイルのパスを構築
            String imagePath = "/resources/images/covers/book_" + bookId + ".jpg";
            String realPath = servletContext.getRealPath(imagePath);

            File imageFile = new File(realPath);

            // 画像ファイルが存在しない場合は no-image.jpg を返す
            if (!imageFile.exists()) {
                logger.debug("Image not found: {}, using no-image.jpg", imagePath);
                imagePath = "/resources/images/covers/no-image.jpg";
                realPath = servletContext.getRealPath(imagePath);
                imageFile = new File(realPath);

                if (!imageFile.exists()) {
                    logger.warn("no-image.jpg not found");
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"画像が見つかりません\"}")
                            .build();
                }
            }

            // ファイルを読み込んで返す
            InputStream inputStream = new FileInputStream(imageFile);
            return Response.ok(inputStream)
                    .type("image/jpeg")
                    .build();

        } catch (IOException e) {
            logger.error("Error reading image file", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"画像の読み込みに失敗しました\"}")
                    .build();
        }
    }
}

