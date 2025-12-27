package pro.kensait.berrybooks.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;

/**
 * 画像配信API リソースクラス
 */
@Path("/images")
@ApplicationScoped
public class ImageResource {
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);

    @Context
    private ServletContext servletContext;
    
    @Inject
    private BookDao bookDao;

    /**
     * 書籍表紙画像取得
     */
    @GET
    @Path("/covers/{bookId}")
    public Response getBookCover(@PathParam("bookId") Integer bookId) {
        logger.info("[ ImageResource#getBookCover ] bookId: {}", bookId);

        try {
            // 書籍情報を取得
            Book book = bookDao.findById(bookId);
            if (book == null) {
                logger.warn("Book not found: bookId={}", bookId);
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.TEXT_PLAIN)
                        .entity("Book not found: bookId=" + bookId)
                        .build();
            }

            // 書籍タイトルをファイル名として使用（スペースをアンダースコアに変換）
            String imageFileName = book.getBookName().replace(" ", "_") + ".jpg";
            String imagePath = "/resources/images/covers/" + imageFileName;
            String realPath = servletContext.getRealPath(imagePath);

            logger.debug("Image path: {}", imagePath);
            logger.debug("Real path: {}", realPath);

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
                            .type(MediaType.TEXT_PLAIN)
                            .entity("Image not found: " + imageFileName + " (no-image.jpg also not found)")
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
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Error reading image file: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}

