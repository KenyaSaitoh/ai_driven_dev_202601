# API_004_images - 画像API 単体テスト用振る舞い仕様書

API ID: API_004_images  
API名: 画像API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 単体テスト仕様確定

---

## 1. 概要

本文書は、API_004_images（画像API）の単体テスト用の振る舞いを記述する。

* テストレベル: 単体テスト
* テストフレームワーク: JUnit 5 + Mockito
* モック対象: ServletContext
* 実装対象: ImageResource

注意: 
* このbehaviors.mdは単体テスト用であり、basic_design/behaviors.md（E2Eテスト用）とは別物です
* E2Eテスト用の振る舞い仕様は[basic_design/behaviors.md](../../basic_design/behaviors.md)を参照してください

---

## 2. ImageResource.getCoverImage() 単体テスト

### 2.1 テスト対象

* クラス: `pro.kensait.berrybooks.api.ImageResource`
* メソッド: `getCoverImage(int bookId)`

### 2.2 モック設定

* `@Mock private ServletContext servletContext;`
* `@InjectMocks private ImageResource imageResource;`

### 2.3 テストケース

#### T_IMG_001: 書籍画像が存在する場合、画像バイナリを返却

* Given（前提条件）:
  * bookId: 1
  * 画像パス: "/resources/images/covers/1.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/1.jpg") → InputStreamを返却（画像バイナリデータを含む）

* When（操作）:
  * imageResource.getCoverImage(1)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = byte[]（画像バイナリデータ）
  * Response.mediaType = "image/jpeg"
  * ログ出力: INFO "書籍表紙画像取得開始: bookId=1"
  * ログ出力: INFO "書籍表紙画像取得成功: bookId=1"

* モック検証:
  * servletContext.getResourceAsStream("/resources/images/covers/1.jpg") が1回呼ばれる

---

#### T_IMG_002: 書籍画像が存在しない場合、フォールバック画像を返却

* Given（前提条件）:
  * bookId: 999
  * 画像パス: "/resources/images/covers/999.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/999.jpg") → null（画像が存在しない）
  * フォールバックパス: "/resources/images/covers/no-image.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") → InputStreamを返却（フォールバック画像バイナリデータを含む）

* When（操作）:
  * imageResource.getCoverImage(999)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = byte[]（フォールバック画像バイナリデータ）
  * Response.mediaType = "image/jpeg"
  * ログ出力: INFO "書籍表紙画像取得開始: bookId=999"
  * ログ出力: WARN "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId=999"

* モック検証:
  * servletContext.getResourceAsStream("/resources/images/covers/999.jpg") が1回呼ばれる
  * servletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") が1回呼ばれる

---

#### T_IMG_003: IOException発生時、500エラーを返却

* Given（前提条件）:
  * bookId: 1
  * 画像パス: "/resources/images/covers/1.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/1.jpg") → IOExceptionをスロー

* When（操作）:
  * imageResource.getCoverImage(1)を呼び出す

* Then（期待結果）:
  * Response.status = 500 Internal Server Error
  * ログ出力: INFO "書籍表紙画像取得開始: bookId=1"
  * ログ出力: ERROR "画像取得エラー: bookId=1, exception={IOExceptionの詳細}"

* モック検証:
  * servletContext.getResourceAsStream("/resources/images/covers/1.jpg") が1回呼ばれる

---

#### T_IMG_004: フォールバック画像も存在しない場合、500エラーを返却

* Given（前提条件）:
  * bookId: 999
  * 画像パス: "/resources/images/covers/999.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/999.jpg") → null（画像が存在しない）
  * フォールバックパス: "/resources/images/covers/no-image.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") → null（フォールバック画像も存在しない）

* When（操作）:
  * imageResource.getCoverImage(999)を呼び出す

* Then（期待結果）:
  * Response.status = 500 Internal Server Error
  * ログ出力: INFO "書籍表紙画像取得開始: bookId=999"
  * ログ出力: WARN "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId=999"
  * ログ出力: ERROR "フォールバック画像が見つかりません: path=/resources/images/covers/no-image.jpg"

* モック検証:
  * servletContext.getResourceAsStream("/resources/images/covers/999.jpg") が1回呼ばれる
  * servletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") が1回呼ばれる

---

#### T_IMG_005: bookIdが0の場合の動作

* Given（前提条件）:
  * bookId: 0
  * 画像パス: "/resources/images/covers/0.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/0.jpg") → null（画像が存在しない）
  * フォールバックパス: "/resources/images/covers/no-image.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") → InputStreamを返却

* When（操作）:
  * imageResource.getCoverImage(0)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = byte[]（フォールバック画像バイナリデータ）
  * Response.mediaType = "image/jpeg"
  * ログ出力: WARN "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId=0"

---

#### T_IMG_006: bookIdが負の数の場合の動作

* Given（前提条件）:
  * bookId: -1
  * 画像パス: "/resources/images/covers/-1.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/-1.jpg") → null（画像が存在しない）
  * フォールバックパス: "/resources/images/covers/no-image.jpg"
  * ServletContext.getResourceAsStream("/resources/images/covers/no-image.jpg") → InputStreamを返却

* When（操作）:
  * imageResource.getCoverImage(-1)を呼び出す

* Then（期待結果）:
  * Response.status = 200 OK
  * Response.entity = byte[]（フォールバック画像バイナリデータ）
  * Response.mediaType = "image/jpeg"
  * ログ出力: WARN "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId=-1"

---

#### T_IMG_007: 複数の書籍画像を連続取得

* Given（前提条件）:
  * bookId: 1, 2, 3
  * ServletContext.getResourceAsStream("/resources/images/covers/1.jpg") → InputStreamを返却
  * ServletContext.getResourceAsStream("/resources/images/covers/2.jpg") → InputStreamを返却
  * ServletContext.getResourceAsStream("/resources/images/covers/3.jpg") → InputStreamを返却

* When（操作）:
  * imageResource.getCoverImage(1)を呼び出す
  * imageResource.getCoverImage(2)を呼び出す
  * imageResource.getCoverImage(3)を呼び出す

* Then（期待結果）:
  * すべてのResponse.status = 200 OK
  * すべてのResponse.entity = byte[]（各画像のバイナリデータ）
  * すべてのResponse.mediaType = "image/jpeg"

* モック検証:
  * servletContext.getResourceAsStream() が3回呼ばれる（各bookIdに対して1回ずつ）

---

## 3. 境界値テスト

### 3.1 bookIdの境界値

| テストケースID | bookId | 期待される動作 | 理由 |
|-------------|--------|-------------|------|
| T_IMG_BV_001 | Integer.MAX_VALUE | フォールバック画像を返却 | 最大値の動作確認 |
| T_IMG_BV_002 | Integer.MIN_VALUE | フォールバック画像を返却 | 最小値の動作確認 |
| T_IMG_BV_003 | 1 | 画像が存在すれば正常に返却 | 最小の正の整数 |

---

## 4. エラーハンドリングテスト

### 4.1 例外処理

| テストケースID | シナリオ | 期待される動作 |
|-------------|---------|-------------|
| T_IMG_ERR_001 | IOException発生 | 500 Internal Server Error、ERRORログ出力 |
| T_IMG_ERR_002 | NullPointerException発生（ServletContext未初期化） | 500 Internal Server Error、ERRORログ出力 |
| T_IMG_ERR_003 | OutOfMemoryError発生（画像サイズが大きすぎる） | 500 Internal Server Error、ERRORログ出力 |

---

## 5. ログ出力テスト

### 5.1 ログレベル検証

| テストケースID | シナリオ | 期待されるログレベル | ログメッセージ |
|-------------|---------|---------------|-------------|
| T_IMG_LOG_001 | 画像取得開始 | INFO | "書籍表紙画像取得開始: bookId={}" |
| T_IMG_LOG_002 | 画像取得成功 | INFO | "書籍表紙画像取得成功: bookId={}, path={}" |
| T_IMG_LOG_003 | 画像が見つからない | WARN | "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId={}" |
| T_IMG_LOG_004 | IOException発生 | ERROR | "画像取得エラー: bookId={}, exception={}" |
| T_IMG_LOG_005 | フォールバック画像も存在しない | ERROR | "フォールバック画像が見つかりません: path=/resources/images/covers/no-image.jpg" |

---

## 6. モックの実装例

### 6.1 ServletContextのモック設定

```java
@ExtendWith(MockitoExtension.class)
class ImageResourceTest {
    
    @Mock
    private ServletContext servletContext;
    
    @InjectMocks
    private ImageResource imageResource;
    
    @Test
    void testGetCoverImage_Success() throws IOException {
        // Given
        int bookId = 1;
        byte[] imageBytes = "dummy-image-data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(imageBytes);
        
        when(servletContext.getResourceAsStream("/resources/images/covers/1.jpg"))
            .thenReturn(inputStream);
        
        // When
        Response response = imageResource.getCoverImage(bookId);
        
        // Then
        assertEquals(200, response.getStatus());
        assertArrayEquals(imageBytes, (byte[]) response.getEntity());
        assertEquals("image/jpeg", response.getMediaType().toString());
        
        // Verify
        verify(servletContext).getResourceAsStream("/resources/images/covers/1.jpg");
    }
    
    @Test
    void testGetCoverImage_Fallback() throws IOException {
        // Given
        int bookId = 999;
        byte[] fallbackBytes = "dummy-fallback-image".getBytes();
        InputStream fallbackStream = new ByteArrayInputStream(fallbackBytes);
        
        when(servletContext.getResourceAsStream("/resources/images/covers/999.jpg"))
            .thenReturn(null);
        when(servletContext.getResourceAsStream("/resources/images/covers/no-image.jpg"))
            .thenReturn(fallbackStream);
        
        // When
        Response response = imageResource.getCoverImage(bookId);
        
        // Then
        assertEquals(200, response.getStatus());
        assertArrayEquals(fallbackBytes, (byte[]) response.getEntity());
        
        // Verify
        verify(servletContext).getResourceAsStream("/resources/images/covers/999.jpg");
        verify(servletContext).getResourceAsStream("/resources/images/covers/no-image.jpg");
    }
}
```

---

## 7. テスト実行順序

### 7.1 推奨テスト実行順序

1. **基本動作テスト**: T_IMG_001, T_IMG_002
2. **エラーハンドリングテスト**: T_IMG_003, T_IMG_004
3. **境界値テスト**: T_IMG_005, T_IMG_006, T_IMG_BV_001～003
4. **複数リクエストテスト**: T_IMG_007
5. **ログ出力テスト**: T_IMG_LOG_001～005

---

## 8. テストカバレッジ目標

| 項目 | 目標 | 備考 |
|------|------|------|
| 行カバレッジ | 90%以上 | ImageResourceクラス全体 |
| 分岐カバレッジ | 85%以上 | 条件分岐を網羅 |
| メソッドカバレッジ | 100% | getCoverImage()メソッド |

---

## 9. テスト実行前提条件

### 9.1 必要なモックライブラリ

* JUnit 5（org.junit.jupiter:junit-jupiter:5.10.x）
* Mockito（org.mockito:mockito-core:5.x）
* Mockito JUnit Jupiter（org.mockito:mockito-junit-jupiter:5.x）

### 9.2 テスト環境

* JDK: 21以上
* ビルドツール: Gradle 8.x
* テストランナー: JUnit Platform

---

## 10. 受入基準

### 10.1 単体テスト受入基準

| 基準ID | 内容 |
|-------|------|
| UT-IMG-001 | すべてのテストケースが成功すること |
| UT-IMG-002 | テストカバレッジが目標値を達成すること |
| UT-IMG-003 | モック検証（verify）がすべて成功すること |
| UT-IMG-004 | ログ出力が期待通りに行われること |
| UT-IMG-005 | ServletContextのモックが適切に動作すること |

---

## 11. 参考資料

### 11.1 関連ドキュメント

* [detailed_design.md](detailed_design.md) - API_004_images詳細設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
* [../../basic_design/architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書

### 11.2 技術標準ドキュメント

* [../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準

### 11.3 テストフレームワーク

* JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
* Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
