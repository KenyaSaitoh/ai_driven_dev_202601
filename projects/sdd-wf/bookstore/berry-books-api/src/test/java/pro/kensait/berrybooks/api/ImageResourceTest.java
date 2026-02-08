package pro.kensait.berrybooks.api;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImageResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageResource 単体テスト")
class ImageResourceTest {

    @Mock
    private ServletContext servletContext;

    @InjectMocks
    private ImageResource imageResource;

    private InputStream sampleImageStream;
    private InputStream fallbackImageStream;

    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        sampleImageStream = new ByteArrayInputStream("sample image data".getBytes());
        fallbackImageStream = new ByteArrayInputStream("fallback image data".getBytes());
    }

    // ============================================================
    // 2.1 ImageResource - PNG画像を取得する
    // ============================================================

    @Test
    @DisplayName("PNG画像を正常に取得")
    void testGetImage_PngSuccess() {
        // Given: WAR内に /resources/images/book-cover-1.png が存在する
        //        ServletContextが正常にリソースを返却するようモック設定
        String filename = "book-cover-1.png";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(sampleImageStream);

        // When: GET /api/images/book-cover-1.png をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が image/png である
        //       レスポンスボディに画像のバイナリデータが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof InputStream);
        assertEquals("image/png", response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
    }

    // ============================================================
    // 2.2 ImageResource - JPEG画像を取得する
    // ============================================================

    @Test
    @DisplayName("JPEG画像を正常に取得")
    void testGetImage_JpegSuccess() {
        // Given: WAR内に /resources/images/book-cover-2.jpg が存在する
        //        ServletContextが正常にリソースを返却するようモック設定
        String filename = "book-cover-2.jpg";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(sampleImageStream);

        // When: GET /api/images/book-cover-2.jpg をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が image/jpeg である
        //       レスポンスボディに画像のバイナリデータが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof InputStream);
        assertEquals("image/jpeg", response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
    }

    @Test
    @DisplayName("JPEG画像を正常に取得（.jpeg拡張子）")
    void testGetImage_JpegExtensionSuccess() {
        // Given: WAR内に /resources/images/photo.jpeg が存在する
        //        ServletContextが正常にリソースを返却するようモック設定
        String filename = "photo.jpeg";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(sampleImageStream);

        // When: GET /api/images/photo.jpeg をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が image/jpeg である
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/jpeg", response.getMediaType().toString());
    }

    // ============================================================
    // 2.3 ImageResource - GIF画像を取得する
    // ============================================================

    @Test
    @DisplayName("GIF画像を正常に取得")
    void testGetImage_GifSuccess() {
        // Given: WAR内に /resources/images/icon.gif が存在する
        //        ServletContextが正常にリソースを返却するようモック設定
        String filename = "icon.gif";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(sampleImageStream);

        // When: GET /api/images/icon.gif をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が image/gif である
        //       レスポンスボディに画像のバイナリデータが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof InputStream);
        assertEquals("image/gif", response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
    }

    // ============================================================
    // 2.4 ImageResource - 画像ファイルが存在しない場合（フォールバック）
    // ============================================================

    @Test
    @DisplayName("存在しない画像はフォールバック画像を返す")
    void testGetImage_FallbackToNoImage() {
        // Given: WAR内に /resources/images/non-existent.png が存在しない
        //        WAR内に /resources/images/no-image.jpg が存在する
        //        ServletContextが最初のリクエストでnullを返し、2回目（フォールバック）で画像を返すようモック設定
        String filename = "non-existent.png";
        String resourcePath = "/resources/images/" + filename;
        String fallbackPath = "/resources/images/no-image.jpg";
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(null);
        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(fallbackImageStream);

        // When: GET /api/images/non-existent.png をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が image/jpeg である（no-image.jpgのため）
        //       レスポンスボディにフォールバック画像のバイナリデータが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof InputStream);
        assertEquals("image/jpeg", response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
        verify(servletContext, times(1)).getResourceAsStream(fallbackPath);
    }

    // ============================================================
    // 2.5 ImageResource - パストラバーサル攻撃対策
    // ============================================================

    @Test
    @DisplayName("パストラバーサルを拒否（..を含む）")
    void testGetImage_PathTraversalRejected() {
        // Given: なし

        // When: GET /api/images/../../../etc/passwd をリクエスト
        Response response = imageResource.getImage("../../../etc/passwd");

        // Then: ステータスコード 400 Bad Request が返される
        //       エラーメッセージ「Invalid filename」が含まれる
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity().toString().contains("Invalid filename"));
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        verify(servletContext, never()).getResourceAsStream(anyString());
    }

    @Test
    @DisplayName("パストラバーサルを拒否（バックスラッシュを含む）")
    void testGetImage_BackslashRejected() {
        // Given: なし

        // When: GET /api/images/..\..\..\..\windows\system32\config\sam をリクエスト
        Response response = imageResource.getImage("..\\..\\..\\..\\windows\\system32\\config\\sam");

        // Then: ステータスコード 400 Bad Request が返される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid filename"));
        verify(servletContext, never()).getResourceAsStream(anyString());
    }

    // ============================================================
    // 2.6 ImageResource - スラッシュを含むファイル名の拒否
    // ============================================================

    @Test
    @DisplayName("スラッシュを含むファイル名を拒否")
    void testGetImage_SlashRejected() {
        // Given: なし

        // When: GET /api/images/subdir/image.png をリクエスト
        Response response = imageResource.getImage("subdir/image.png");

        // Then: ステータスコード 400 Bad Request が返される
        //       エラーメッセージ「Invalid filename」が含まれる
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity().toString().contains("Invalid filename"));
        verify(servletContext, never()).getResourceAsStream(anyString());
    }

    @Test
    @DisplayName("nullファイル名を拒否")
    void testGetImage_NullFilenameRejected() {
        // Given: なし

        // When: GET /api/images/null をリクエスト
        Response response = imageResource.getImage(null);

        // Then: ステータスコード 400 Bad Request が返される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid filename"));
        verify(servletContext, never()).getResourceAsStream(anyString());
    }

    // ============================================================
    // 2.7 ImageResource - 不明な拡張子の扱い
    // ============================================================

    @Test
    @DisplayName("不明な拡張子はapplication/octet-streamとして扱う")
    void testGetImage_UnknownExtension() {
        // Given: WAR内に /resources/images/file.unknown が存在する
        //        ServletContextが正常にリソースを返却するようモック設定
        String filename = "file.unknown";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(sampleImageStream);

        // When: GET /api/images/file.unknown をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 200 OK が返される
        //       Content-Type が application/octet-stream である
        //       レスポンスボディにファイルのバイナリデータが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof InputStream);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
    }

    // ============================================================
    // 2.8 ImageResource - フォールバック画像も存在しない場合
    // ============================================================

    @Test
    @DisplayName("フォールバック画像も存在しない場合は500エラー")
    void testGetImage_FallbackImageNotFound() {
        // Given: WAR内に /resources/images/requested.png が存在しない
        //        WAR内に /resources/images/no-image.jpg も存在しない
        //        ServletContextが両方のリクエストでnullを返すようモック設定
        String filename = "requested.png";
        String resourcePath = "/resources/images/" + filename;
        String fallbackPath = "/resources/images/no-image.jpg";
        when(servletContext.getResourceAsStream(resourcePath)).thenReturn(null);
        when(servletContext.getResourceAsStream(fallbackPath)).thenReturn(null);

        // When: GET /api/images/requested.png をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 500 Internal Server Error が返される
        //       エラーメッセージ「Image not found」が含まれる
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity().toString().contains("Image not found"));
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        verify(servletContext, times(1)).getResourceAsStream(resourcePath);
        verify(servletContext, times(1)).getResourceAsStream(fallbackPath);
    }

    @Test
    @DisplayName("例外発生時は500エラー")
    void testGetImage_ExceptionOccurred() {
        // Given: ServletContextが例外をスローするようモック設定
        String filename = "error.png";
        String resourcePath = "/resources/images/" + filename;
        when(servletContext.getResourceAsStream(resourcePath))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When: GET /api/images/error.png をリクエスト
        Response response = imageResource.getImage(filename);

        // Then: ステータスコード 500 Internal Server Error が返される
        //       エラーメッセージ「Internal server error」が含まれる
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity().toString().contains("Internal server error"));
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }
}
