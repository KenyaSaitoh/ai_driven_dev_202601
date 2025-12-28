# API_004_images - 画像API機能設計書

**API ID:** API_004_images  
**API名:** 画像API  
**ベースパス:** `/api/images`  
**バージョン:** 2.0.0  
**最終更新日:** 2025-12-27

---

## 1. 概要

画像APIは、書籍表紙画像の取得を提供する。認証不要で、公開APIとして利用できる。

**認証要否**: 不要（公開API）

---

## 2. エンドポイント一覧

| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/api/images/covers/{bookId}` | 書籍表紙画像取得 | 不要 |

---

## 3. API仕様

### 3.1 書籍表紙画像取得

#### 3.1.1 エンドポイント

```
GET /api/images/covers/{bookId}
```

#### 3.1.2 概要

指定された書籍IDの表紙画像を取得する。認証不要。

#### 3.1.3 リクエスト

**パスパラメータ**:

| パラメータ | 型 | 説明 |
|----------|---|------|
| bookId | integer | 書籍ID |

#### 3.1.4 レスポンス

**成功時 (200 OK)**:

**Content-Type**: `image/jpeg`

**Body**: 画像バイナリデータ

**エラー時 (404 Not Found)**:

フォールバック画像（`no-image.jpg`）を返す

#### 3.1.5 ビジネスルール

| ルールID | 説明 |
|---------|-------------|
| BR-IMAGE-001 | 画像ファイル名は{bookId}.jpg（例: 1.jpg, 2.jpg） |
| BR-IMAGE-002 | 画像が存在しない場合、no-image.jpgを返す |
| BR-IMAGE-003 | 画像は webapp/resources/images/covers/ に配置 |
| BR-IMAGE-004 | ServletContextを使用してWAR内リソースにアクセス |

#### 3.1.6 実装詳細

**リソースアクセス方法**:

ImageResourceは、デプロイ後のWARファイル内のリソースにアクセスするため、`ServletContext.getResourceAsStream()`を使用します。

```java
@Path("/images")
public class ImageResource {
    
    private static final String IMAGE_BASE_PATH = "/resources/images/covers/";
    
    @Context
    private ServletContext servletContext;
    
    @GET
    @Path("/covers/{bookId}")
    @Produces("image/jpeg")
    public Response getCoverImage(@PathParam("bookId") Integer bookId) {
        String imagePath = IMAGE_BASE_PATH + bookId + ".jpg";
        InputStream inputStream = servletContext.getResourceAsStream(imagePath);
        
        // フォールバック処理
        if (inputStream == null) {
            inputStream = servletContext.getResourceAsStream(
                IMAGE_BASE_PATH + "no-image.jpg");
        }
        
        // 画像バイナリを返却
        byte[] imageData = inputStream.readAllBytes();
        return Response.ok(imageData).type("image/jpeg").build();
    }
}
```

**重要な設計上の決定**:

1. **ファイルシステムの相対パスは使用しない**: `new File("src/main/webapp/...")` はデプロイ後に機能しない
2. **ServletContextを使用**: WARファイル内のリソースに正しくアクセス
3. **パスは `/` から始まる**: WAR内の絶対パス形式

---

## 4. ファイル命名規則

### 4.1 画像ファイル名

**形式**: `{bookId}.jpg`

**例**:
- `1.jpg` - Book ID 1の表紙画像
- `2.jpg` - Book ID 2の表紙画像
- `10.jpg` - Book ID 10の表紙画像
- `no-image.jpg` - フォールバック画像（必須）

### 4.2 画像格納場所

**ディレクトリ**: `src/main/webapp/resources/images/covers/`

**WAR内パス**: `/resources/images/covers/`

**フルパス例**: 
- ソースコード: `src/main/webapp/resources/images/covers/1.jpg`
- WAR内: `/resources/images/covers/1.jpg`

---

## 5. エラーハンドリング

### 5.1 画像が存在しない場合

**動作**: フォールバック画像（`no-image.jpg`）を返す

**HTTPステータス**: 200 OK（エラーではなく正常レスポンス）

---

## 6. 技術的な注意事項

### 6.1 WAR内リソースアクセス

**問題**: ファイルシステムの相対パス（`src/main/webapp/...`）はデプロイ後に機能しない

**理由**:
- WARファイルはアーカイブ（ZIPのような形式）
- ファイルシステムAPIでは直接アクセスできない
- デプロイ先サーバーのディレクトリ構造は開発環境と異なる

**解決策**: ServletContextを使用

```java
// ❌ 動作しない方法（開発環境でのみ動作）
File imageFile = new File("src/main/webapp/resources/images/covers/1.jpg");

// ✅ 正しい方法（デプロイ後も動作）
InputStream inputStream = servletContext.getResourceAsStream(
    "/resources/images/covers/1.jpg");
```

### 6.2 パフォーマンス考慮事項

**将来の拡張**:

- **ブラウザキャッシュ**: `Cache-Control` ヘッダーで長期キャッシュ（例: 1日）
- **CDN**: 静的画像をCDNで配信
- **圧縮**: 画像ファイルを最適化（例: WebP形式）
- **遅延ロード**: フロントエンドでLazy Loading実装

---

## 7. 関連ドキュメント

- [behaviors.md](behaviors.md) - 画像APIの受入基準
- [../../system/functional_design.md](../../system/functional_design.md) - 全体機能設計書
- [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書

