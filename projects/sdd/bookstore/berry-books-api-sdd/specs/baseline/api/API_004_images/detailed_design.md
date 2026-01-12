# API_004 画像API - 詳細設計書（静的リソース配信）

* API ID: API_004  
* API名: 画像API  
* パターン: 静的リソース配信（WAR内リソース）  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.berrybooks
└── api
    └── ImageResource.java            # 画像リソース
```

* 注: このAPIは外部API連携なし、DAO層なし、サービス層なし、エンティティなしです。WAR内の静的リソースを配信するだけです。

---

## 2. クラス設計

### 2.1 ImageResource（JAX-RS Resource）

* 責務: 画像ファイルの配信

* アノテーション:
  * `@Path("/images")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ

* 主要フィールド:
  * `ServletContext servletContext` - サーブレットコンテキスト（@Context）

* 主要メソッド:

#### getCoverImage() - 書籍表紙画像取得

```
@GET
@Path("/covers/{bookId}")
@Produces("image/jpeg")
```

* パラメータ:
  * `@PathParam("bookId") String bookIdStr` - 書籍ID（文字列、"no-image"の場合はフォールバック画像）

* 処理フロー:
  1. bookIdStrが"no-image"の場合、`getNoImage()`を呼び出してフォールバック画像を返却
  2. ServletContextから`/resources/images/covers/{bookId}.jpg`を取得
   ```java
   InputStream inputStream = servletContext.getResourceAsStream(
       "/resources/images/covers/" + bookIdStr + ".jpg");
   ```
  3. InputStreamが`null`の場合、フォールバック画像（no-image.jpg）を返却
  4. InputStreamから全バイトを読み取り、バイナリデータとして返却

* レスポンス: `byte[]`（画像バイナリ）

* Content-Type: `image/jpeg`

* フォールバック処理:
  * 画像が存在しない場合 → `no-image.jpg`を返却
  * `no-image.jpg`も存在しない場合 → `404 Not Found`

* 特別なリクエスト:
  * `GET /api/images/covers/no-image` → 直接フォールバック画像を返却

* エラーケース:
  * 画像読み取り失敗 → `500 Internal Server Error`
  * フォールバック画像も存在しない → `404 Not Found`

---

## 3. 画像ファイル配置

### 3.1 ディレクトリ構造

```
berry-books-api-sdd/
└── src/
    └── main/
        └── webapp/
            └── resources/
                └── images/
                    └── covers/
                        ├── 1.jpg
                        ├── 2.jpg
                        ├── 3.jpg
                        ├── ...
                        ├── 50.jpg
                        └── no-image.jpg  ← フォールバック画像
```

### 3.2 ファイル命名規則

* フォーマット: `{bookId}.jpg`

* 例:
  * `1.jpg` - 書籍ID: 1（Java SEディープダイブ）
  * `2.jpg` - 書籍ID: 2（JVMとバイトコードの探求）
  * `3.jpg` - 書籍ID: 3（Javaアーキテクトのための設計原理）

* フォールバック画像:
  * `no-image.jpg` - 画像が存在しない書籍用の代替画像

* 注: 書籍IDはゼロ埋めなし、数値のみ

---

## 4. 実装例

### 4.1 ImageResourceの完全実装

```java
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

@Path("/images")
@ApplicationScoped
public class ImageResource {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);
    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    private static final String NO_IMAGE_FILENAME = "no-image.jpg";

    @Context
    private ServletContext servletContext;

    @GET
    @Path("/{filename}")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    public Response getImage(@PathParam("filename") String filename) {
        // WAR内のimagesディレクトリから画像を取得
        String resourcePath = "/images/" + filename;
        InputStream inputStream = servletContext.getResourceAsStream(resourcePath);

        // ファイルが存在しない場合は404を返却
        if (inputStream == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Image not found: " + filename)
                           .build();
        }

        // 画像を返却
        return Response.ok(inputStream).build();
    }
}
```

---

## 5. セキュリティ考慮事項

### 5.1 パストラバーサル攻撃対策

* 脅威:
```
GET /api/images/../../../etc/passwd
```

* 対策:
  1. ServletContextは自動的にコンテキストルート外へのアクセスを防止
  2. 追加の検証が必要な場合:
   ```java
   if (filename.contains("..") || filename.contains("/")) {
       return Response.status(Response.Status.BAD_REQUEST).build();
   }
   ```

### 5.2 認証要否

* 現状: 認証不要（公開リソース）

* 理由:
  * 書籍の表紙画像は公開情報
  * パフォーマンスのため認証スキップ

---

## 6. パフォーマンス最適化

### 6.1 キャッシュ制御

* 推奨: HTTPヘッダーでキャッシュを設定

```java
return Response.ok(inputStream)
               .cacheControl(CacheControl.valueOf("max-age=86400"))  // 24時間キャッシュ
               .build();
```

### 6.2 CDN連携

* 将来の検討:
  * 画像をCDN（Cloudflare, AWS CloudFront等）に配置
  * ImageResourceをCDNリダイレクトに変更

---

## 7. エラーハンドリング

### 7.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| ファイルが存在しない | 404 Not Found | `"Image not found: book999.jpg"` |
| 不正なファイル名 | 400 Bad Request | `"Invalid filename"` |

---

## 8. テスト要件

### 8.1 ユニットテスト

* 対象: なし（ビジネスロジックなし）

### 8.2 統合テスト

* 対象: `ImageResource`

* 画像取得API呼び出し（正常系）
* 画像取得API呼び出し（存在しないファイル）
* Content-Type検証（JPEG）
* Content-Type検証（PNG）

---

## 9. 使用例

### 9.1 フロントエンドでの使用

* React/Vue/Angular:
```html
<img src="http://localhost:8080/berry-books-api-sdd/api/images/book001.jpg" alt="書籍画像">
```

* 動的な画像URL生成:
```javascript
const imageUrl = `${API_BASE_URL}/images/book${String(bookId).padStart(3, '0')}.jpg`;
```

---

## 10. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [ServletContext仕様](https://jakarta.ee/specifications/servlet/6.0/)
