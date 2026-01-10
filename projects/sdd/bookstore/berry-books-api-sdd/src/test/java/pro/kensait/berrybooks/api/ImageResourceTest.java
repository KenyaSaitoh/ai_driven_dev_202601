package pro.kensait.berrybooks.api;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImageResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageResource Unit Tests")
class ImageResourceTest {

    @Mock
    private ServletContext servletContext;

    @InjectMocks
    private ImageResource imageResource;

    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    private static final String NO_IMAGE_FILENAME = "no-image.jpg";

    @BeforeEach
    void setUp() throws Exception {
        // ServletContextをImageResourceに手動で設定（リフレクション使用）
        java.lang.reflect.Field field = ImageResource.class.getDeclaredField("servletContext");
        field.setAccessible(true);
        field.set(imageResource, servletContext);
    }

    @Test
    @DisplayName("正常系: 書籍表紙画像を取得できる")
    void testGetCoverImage_Success() throws IOException {
        // Given: 書籍ID=1の画像が存在する
        String bookId = "1";
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        byte[] mockImageData = "mock-image-data".getBytes();
        InputStream mockInputStream = new ByteArrayInputStream(mockImageData);

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(mockInputStream);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 200 OK、Content-Type: image/jpeg、画像バイナリが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/jpeg", response.getMediaType().toString());
        assertArrayEquals(mockImageData, (byte[]) response.getEntity());

        // Verify: ServletContextが正しく呼び出された
        verify(servletContext, times(1)).getResourceAsStream(imagePath);
    }

    @Test
    @DisplayName("正常系: 画像が存在しない場合、フォールバック画像を返す")
    void testGetCoverImage_FallbackImage() throws IOException {
        // Given: 書籍ID=999の画像が存在しない、フォールバック画像が存在する
        String bookId = "999";
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;
        byte[] mockFallbackImageData = "mock-fallback-image-data".getBytes();
        InputStream mockFallbackInputStream = new ByteArrayInputStream(mockFallbackImageData);

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(null);
        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(mockFallbackInputStream);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 200 OK、フォールバック画像が返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/jpeg", response.getMediaType().toString());
        assertArrayEquals(mockFallbackImageData, (byte[]) response.getEntity());

        // Verify: ServletContextが正しく呼び出された
        verify(servletContext, times(1)).getResourceAsStream(imagePath);
        verify(servletContext, times(1)).getResourceAsStream(fallbackPath);
    }

    @Test
    @DisplayName("異常系: フォールバック画像も存在しない場合、404を返す")
    void testGetCoverImage_NoImageAndNoFallback() {
        // Given: 書籍ID=999の画像もフォールバック画像も存在しない
        String bookId = "999";
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(null);
        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(null);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 404 Not Found
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Image not found", response.getEntity());

        // Verify: ServletContextが正しく呼び出された
        verify(servletContext, times(1)).getResourceAsStream(imagePath);
        verify(servletContext, times(1)).getResourceAsStream(fallbackPath);
    }

    @Test
    @DisplayName("異常系: InputStreamの読み取りに失敗した場合、500を返す")
    void testGetCoverImage_IOExceptionDuringRead() throws IOException {
        // Given: 書籍ID=1の画像が存在するが、読み取りに失敗する
        String bookId = "1";
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        InputStream mockInputStream = mock(InputStream.class);

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(mockInputStream);
        when(mockInputStream.readAllBytes()).thenThrow(new IOException("Mock IO Exception"));

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 500 Internal Server Error
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to read image", response.getEntity());

        // Verify: ServletContextが正しく呼び出された
        verify(servletContext, times(1)).getResourceAsStream(imagePath);
        verify(mockInputStream, times(1)).readAllBytes();
    }

    @Test
    @DisplayName("境界値: 書籍ID=0の場合")
    void testGetCoverImage_BookIdZero() throws IOException {
        // Given: 書籍ID=0の画像が存在する
        String bookId = "0";
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        byte[] mockImageData = "mock-image-data-0".getBytes();
        InputStream mockInputStream = new ByteArrayInputStream(mockImageData);

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(mockInputStream);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 200 OK
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertArrayEquals(mockImageData, (byte[]) response.getEntity());
    }

    @Test
    @DisplayName("境界値: 書籍ID=Integer.MAX_VALUEの場合")
    void testGetCoverImage_BookIdMaxValue() throws IOException {
        // Given: 書籍ID=Integer.MAX_VALUEの画像が存在しない、フォールバック画像が存在する
        String bookId = String.valueOf(Integer.MAX_VALUE);
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;
        byte[] mockFallbackImageData = "mock-fallback-image-data".getBytes();
        InputStream mockFallbackInputStream = new ByteArrayInputStream(mockFallbackImageData);

        when(servletContext.getResourceAsStream(imagePath)).thenReturn(null);
        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(mockFallbackInputStream);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 200 OK、フォールバック画像が返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertArrayEquals(mockFallbackImageData, (byte[]) response.getEntity());
    }

    @Test
    @DisplayName("正常系: 書籍ID='no-image'の場合、フォールバック画像を返す")
    void testGetCoverImage_NoImageString() throws IOException {
        // Given: "no-image" リクエスト、フォールバック画像が存在する
        String bookId = "no-image";
        String fallbackPath = IMAGE_BASE_PATH + NO_IMAGE_FILENAME;
        byte[] mockFallbackImageData = "mock-fallback-image-data".getBytes();
        InputStream mockFallbackInputStream = new ByteArrayInputStream(mockFallbackImageData);

        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(mockFallbackInputStream);

        // When: 画像取得APIを呼び出す
        Response response = imageResource.getCoverImage(bookId);

        // Then: 200 OK、フォールバック画像が返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/jpeg", response.getMediaType().toString());
        assertArrayEquals(mockFallbackImageData, (byte[]) response.getEntity());

        // Verify: ServletContextが正しく呼び出された（no-image.jpgのみ）
        verify(servletContext, times(1)).getResourceAsStream(fallbackPath);
    }
}
