package pro.kensait.berrybooks.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 画像APIのE2Eテスト
 * 
 * 注意: このテストは実際のアプリケーションサーバーが起動している必要があります。
 * 通常のビルドでは実行されず、個別に実行する必要があります。
 * 
 * 実行方法:
 * ./gradlew :berry-books-api-sdd:e2eTest
 */
@Tag("e2e")
@DisplayName("Image API E2E Tests")
class ImageApiE2ETest {

    private static final String API_BASE_URL = "http://localhost:8080/berry-books-api-sdd";
    private static final String IMAGES_ENDPOINT = API_BASE_URL + "/api/images/covers";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    @DisplayName("E2E: 書籍表紙画像を取得できる")
    void testGetCoverImage_Success() throws IOException, InterruptedException {
        // Given: 書籍ID=1の画像が存在する
        Integer bookId = 1;
        String url = IMAGES_ENDPOINT + "/" + bookId;

        // When: 画像取得APIを呼び出す
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Then: 200 OK、Content-Type: image/jpeg、画像バイナリが返される
        assertEquals(200, response.statusCode(), "HTTP status should be 200 OK");
        
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("image/jpeg"), 
                   "Content-Type should be image/jpeg, but was: " + contentType);
        
        byte[] imageData = response.body();
        assertNotNull(imageData, "Image data should not be null");
        assertTrue(imageData.length > 0, "Image data should not be empty");
        
        // JPEG画像のマジックナンバー（FFD8FF）を確認
        assertTrue(imageData.length >= 3, "Image data should be at least 3 bytes");
        assertEquals((byte) 0xFF, imageData[0], "JPEG magic number first byte should be 0xFF");
        assertEquals((byte) 0xD8, imageData[1], "JPEG magic number second byte should be 0xD8");
        assertEquals((byte) 0xFF, imageData[2], "JPEG magic number third byte should be 0xFF");
    }

    @Test
    @DisplayName("E2E: 画像が存在しない場合、フォールバック画像を返す")
    void testGetCoverImage_FallbackImage() throws IOException, InterruptedException {
        // Given: 書籍ID=999の画像が存在しない
        Integer bookId = 999;
        String url = IMAGES_ENDPOINT + "/" + bookId;

        // When: 画像取得APIを呼び出す
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Then: 200 OK、フォールバック画像（no-image.jpg）が返される
        assertEquals(200, response.statusCode(), "HTTP status should be 200 OK");
        
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("image/jpeg"), 
                   "Content-Type should be image/jpeg, but was: " + contentType);
        
        byte[] imageData = response.body();
        assertNotNull(imageData, "Fallback image data should not be null");
        assertTrue(imageData.length > 0, "Fallback image data should not be empty");
    }

    @Test
    @DisplayName("E2E: 複数の書籍画像を連続取得できる")
    void testGetCoverImage_MultipleBooks() throws IOException, InterruptedException {
        // Given: 複数の書籍IDが存在する
        Integer[] bookIds = {1, 2, 3, 5, 10};

        // When & Then: 各書籍の画像を取得
        for (Integer bookId : bookIds) {
            String url = IMAGES_ENDPOINT + "/" + bookId;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            // 200 OKが返されることを確認（画像が存在するか、フォールバック画像が返される）
            assertEquals(200, response.statusCode(), 
                         "HTTP status should be 200 OK for bookId: " + bookId);
            
            byte[] imageData = response.body();
            assertNotNull(imageData, "Image data should not be null for bookId: " + bookId);
            assertTrue(imageData.length > 0, "Image data should not be empty for bookId: " + bookId);
        }
    }

    @Test
    @DisplayName("E2E: Content-Typeが正しく設定されている")
    void testGetCoverImage_ContentType() throws IOException, InterruptedException {
        // Given: 書籍ID=1の画像が存在する
        Integer bookId = 1;
        String url = IMAGES_ENDPOINT + "/" + bookId;

        // When: 画像取得APIを呼び出す
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Then: Content-Typeがimage/jpegである
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("image/jpeg"), 
                   "Content-Type should be image/jpeg, but was: " + contentType);
    }

    @Test
    @DisplayName("E2E: 認証なしでアクセスできる")
    void testGetCoverImage_NoAuthenticationRequired() throws IOException, InterruptedException {
        // Given: 認証トークンなしでリクエスト
        Integer bookId = 1;
        String url = IMAGES_ENDPOINT + "/" + bookId;

        // When: 認証ヘッダーなしで画像取得APIを呼び出す
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                // 認証ヘッダーなし
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Then: 200 OK（認証不要で画像が取得できる）
        assertEquals(200, response.statusCode(), 
                     "HTTP status should be 200 OK without authentication");
    }
}
