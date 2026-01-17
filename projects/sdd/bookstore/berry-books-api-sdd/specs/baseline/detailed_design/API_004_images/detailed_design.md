# berry-books-api - API_004_images 詳細設計書

API ID: API_004_images  
API名: 画像API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 詳細設計確定

---

## 1. API概要

本文書は、画像API（API_004_images）の詳細設計を記述する。

* 実装パターン: 独自実装（WAR内静的リソース配信）
* 実装範囲: JAX-RS Resource のみ（Entity、Dao、Serviceは不要）
* ベースパッケージ: `pro.kensait.berrybooks.api`
* ベースパス: `/api/images`
* 認証: 不要（公開エンドポイント）

* 参照仕様書:
  * [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
  * [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
  * [behaviors.md](behaviors.md) - 振る舞い仕様書（単体テスト用）

---

## 2. パッケージ構造

### 2.1 API固有パッケージ

```
pro.kensait.berrybooks
└── api                          # Presentation Layer (JAX-RS Resources)
    └── ImageResource.java
```

注意: 
* 画像ファイルはWAR内の静的リソースとして配置（`src/main/webapp/resources/images/covers/`）
* Entity、Dao、Serviceは実装しない
* ServletContextを使用してWAR内リソースにアクセス

---

## 3. Resourceクラス設計

### 3.1 ImageResource（画像API Resource）

#### 3.1.1 概要

* 責務: 画像APIエンドポイントの提供（WAR内静的リソース配信）
* パッケージ: `pro.kensait.berrybooks.api`
* ベースパス: `/api/images`

#### 3.1.2 クラス定義

* クラス名: `ImageResource`
* アノテーション:
  * `@Path("/images")` - ベースパス
  * `@ApplicationScoped` - CDI管理Bean（シングルトン）

#### 3.1.3 依存関係

* `@Context private ServletContext servletContext;` - ServletContext（WAR内リソースアクセス用）
* `private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);`

#### 3.1.4 メソッド設計

##### getCoverImage(int bookId) - 書籍表紙画像取得

* シグネチャ:
```java
@GET
@Path("/covers/{bookId}")
@Produces("image/jpeg")
public Response getCoverImage(@PathParam("bookId") int bookId)
```

* 目的: 書籍表紙画像をWAR内リソースから配信
* エンドポイント: `GET /api/images/covers/{bookId}`
* 認証: 不要（公開エンドポイント）

* パラメータ:
  * `@PathParam("bookId") int bookId` - 書籍ID

* 処理フロー:
  1. INFO ログ出力: "書籍表紙画像取得開始: bookId={}"
  2. 画像パス構築: `/resources/images/covers/{bookId}.jpg`
  3. ServletContextから画像リソース取得:
    * `servletContext.getResourceAsStream(imagePath)`
  4. 画像存在チェック:
    * 画像が存在する場合 → ステップ5へ
    * 画像が存在しない場合 → ステップ6へ
  5. 画像が存在する場合:
    * INFO ログ出力: "書籍表紙画像取得成功: bookId={}, path={}"
    * InputStreamからバイト配列を読み込み: `inputStream.readAllBytes()`
    * Response返却:
      * ステータス: 200 OK
      * Content-Type: image/jpeg
      * ボディ: 画像バイナリデータ（byte[]）
  6. 画像が存在しない場合（フォールバック）:
    * WARN ログ出力: "書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId={}"
    * フォールバック画像パス: `/resources/images/covers/no-image.jpg`
    * ServletContextからフォールバック画像取得
    * InputStreamからバイト配列を読み込み
    * Response返却:
      * ステータス: 200 OK
      * Content-Type: image/jpeg
      * ボディ: フォールバック画像バイナリデータ（byte[]）
  7. 例外処理（IOException等）:
    * ERROR ログ出力: "画像取得エラー: bookId={}", exception
    * 500 Internal Server Errorを返却

* レスポンス（成功）: `Response.ok(imageBytes).type("image/jpeg").build()`
  * HTTPステータス: 200 OK
  * Content-Type: image/jpeg
  * ボディ: 画像バイナリデータ（byte[]）

* レスポンス（フォールバック）: `Response.ok(noImageBytes).type("image/jpeg").build()`
  * HTTPステータス: 200 OK
  * Content-Type: image/jpeg
  * ボディ: no-image.jpgのバイナリデータ（byte[]）

* エラーケース:
  * IOException発生: 500 Internal Server Error
  * フォールバック画像も存在しない: 500 Internal Server Error

* 実装上の注意:
  * ファイルシステムAPI（`new File()`, `FileInputStream`）は使用しない
  * ServletContext.getResourceAsStream()を使用（デプロイ後も動作）
  * パス形式: WARルート相対パス（`/resources/...`）
  * InputStreamは必ずクローズ（try-with-resources使用）

---

## 4. 静的リソース配置

### 4.1 画像ファイル配置場所

* 配置ディレクトリ: `src/main/webapp/resources/images/covers/`
* WAR内パス: `/resources/images/covers/`

### 4.2 ファイル命名規則

* 書籍表紙画像: `{bookId}.jpg`
  * 例: `1.jpg`, `2.jpg`, `3.jpg`, ...
* フォールバック画像: `no-image.jpg`（必須）

### 4.3 画像形式

* フォーマット: JPEG
* Content-Type: `image/jpeg`

### 4.4 フォールバック画像

* ファイル名: `no-image.jpg`
* 目的: 書籍画像が存在しない場合のデフォルト画像
* 配置場所: `src/main/webapp/resources/images/covers/no-image.jpg`
* 必須: はい（このファイルが存在しない場合、500エラーを返却）

---

## 5. エラーハンドリング

### 5.1 エラーシナリオ

| エラーケース | HTTPステータス | ログレベル | エラーメッセージ |
|------------|--------------|----------|---------------|
| 書籍画像が存在しない | 200 OK（フォールバック） | WARN | "書籍表紙画像が見つかりません。フォールバック画像を返却します" |
| IOException発生 | 500 Internal Server Error | ERROR | "画像取得エラー: {exception}" |
| フォールバック画像も存在しない | 500 Internal Server Error | ERROR | "フォールバック画像が見つかりません" |

### 5.2 Exception Mapper

* 使用するException Mapper: `GenericExceptionMapper`（共通機能で定義済み）
* IOExceptionは`GenericExceptionMapper`で500エラーに変換される

---

## 6. ログ出力

### 6.1 ログ出力方針

| 処理 | ログレベル | ログメッセージ |
|------|----------|-------------|
| メソッド開始 | INFO | `[ ImageResource#getCoverImage ] 書籍表紙画像取得開始: bookId={}` |
| 画像取得成功 | INFO | `[ ImageResource#getCoverImage ] 書籍表紙画像取得成功: bookId={}, path={}` |
| 画像が見つからない | WARN | `[ ImageResource#getCoverImage ] 書籍表紙画像が見つかりません。フォールバック画像を返却します: bookId={}` |
| IOException発生 | ERROR | `[ ImageResource#getCoverImage ] 画像取得エラー: bookId={}, exception={}` |
| フォールバック画像も存在しない | ERROR | `[ ImageResource#getCoverImage ] フォールバック画像が見つかりません: path=/resources/images/covers/no-image.jpg` |

### 6.2 ログフォーマット

* SLF4J + Log4j2を使用
* パターン: `[ ClassName#methodName ] message`
* 例外スタックトレース: ERROR時のみ出力

---

## 7. ServletContext使用方法（重要）

### 7.1 WAR内リソースアクセスのベストプラクティス

#### 7.1.1 推奨方法（ServletContext使用）

* ServletContextをインジェクション: `@Context private ServletContext servletContext;`
* リソース取得: `servletContext.getResourceAsStream("/resources/images/covers/1.jpg")`
* パス形式: WARルート相対パス（`/`から始まる）

* 利点:
  * デプロイ後も正常に動作
  * 開発環境と本番環境で動作を統一
  * WARアーカイブ内のリソースに統一的にアクセス可能

#### 7.1.2 非推奨方法（ファイルシステムAPI）

* ❌ `new File("src/main/webapp/resources/images/covers/1.jpg")`
* ❌ `new FileInputStream("src/main/webapp/resources/images/covers/1.jpg")`

* 問題点:
  * 開発環境でのみ動作
  * WARファイルにデプロイすると動作しない
  * ファイルシステムAPIはWARアーカイブ内のリソースにアクセス不可

#### 7.1.3 実装例

```java
@Context
private ServletContext servletContext;

@GET
@Path("/covers/{bookId}")
@Produces("image/jpeg")
public Response getCoverImage(@PathParam("bookId") int bookId) {
    String imagePath = "/resources/images/covers/" + bookId + ".jpg";
    
    try (InputStream inputStream = servletContext.getResourceAsStream(imagePath)) {
        if (inputStream == null) {
            // フォールバック画像を返却
            logger.warn("書籍表紙画像が見つかりません: bookId={}", bookId);
            String fallbackPath = "/resources/images/covers/no-image.jpg";
            
            try (InputStream fallbackStream = servletContext.getResourceAsStream(fallbackPath)) {
                if (fallbackStream == null) {
                    logger.error("フォールバック画像が見つかりません");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                
                byte[] imageBytes = fallbackStream.readAllBytes();
                return Response.ok(imageBytes).type("image/jpeg").build();
            }
        }
        
        byte[] imageBytes = inputStream.readAllBytes();
        logger.info("書籍表紙画像取得成功: bookId={}", bookId);
        return Response.ok(imageBytes).type("image/jpeg").build();
        
    } catch (IOException e) {
        logger.error("画像取得エラー: bookId={}", bookId, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
```

---

## 8. 設計上の制約と前提条件

### 8.1 制約事項

| 制約ID | 内容 |
|-------|------|
| C-IMG-001 | 画像フォーマットはJPEG固定 |
| C-IMG-002 | ServletContextを使用してWAR内リソースにアクセス |
| C-IMG-003 | ファイルシステムAPIを使用しない |
| C-IMG-004 | フォールバック画像（no-image.jpg）は必須 |
| C-IMG-005 | 画像が存在しない場合、常にフォールバック画像を返却（404エラーを返さない） |

### 8.2 前提条件

| 前提ID | 内容 |
|-------|------|
| P-IMG-001 | 画像ファイルは`src/main/webapp/resources/images/covers/`に配置されている |
| P-IMG-002 | フォールバック画像（no-image.jpg）が配置されている |
| P-IMG-003 | 画像ファイル名は`{bookId}.jpg`形式 |
| P-IMG-004 | ServletContextがCDIコンテナでインジェクション可能 |

---

## 9. セキュリティ考慮事項

### 9.1 パストラバーサル攻撃対策

* bookIdはint型に限定（パスパラメータバインディングで自動検証）
* 相対パス（`../`）を含むリクエストはJAX-RSで拒否される
* WARルート相対パスのみを許可

### 9.2 認証・認可

* 画像APIは公開エンドポイント（認証不要）
* 理由: 書籍表紙画像は一般公開情報

---

## 10. パフォーマンス考慮事項

### 10.1 最適化戦略

| 項目 | 戦略 | 期待効果 |
|------|------|---------|
| InputStreamクローズ | try-with-resources使用 | リソースリーク防止 |
| バイナリデータ送信 | byte[]で一括送信 | ストリーミングオーバーヘッド削減 |

### 10.2 将来的な改善案

* 画像キャッシング（HTTP Cache-Controlヘッダー）
* CDN配信（外部サービス連携）
* 画像リサイズ（複数サイズ対応）

---

## 11. テスト要件

### 11.1 ユニットテスト

* 対象: `ImageResource`
* テストフレームワーク: JUnit 5 + Mockito

* テストケース:
  1. 書籍画像が存在する場合、正常に画像を返却できること
  2. 書籍画像が存在しない場合、フォールバック画像を返却できること
  3. IOException発生時、500エラーを返却できること
  4. ServletContextのモック化

* モック対象:
  * ServletContext（Mockito）

### 11.2 受入テスト

* 詳細: [behaviors.md](behaviors.md)を参照

---

## 12. 参考資料

### 12.1 システムレベルドキュメント

* [architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
  * 「2.3 静的リソース配信（画像API）」セクション参照
  * 「8.3 静的リソースアクセスのベストプラクティス」セクション参照
* [functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../../basic_design/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）

### 12.2 API単位ドキュメント

* [behaviors.md](behaviors.md) - API_004_images振る舞い仕様書（単体テスト用）

### 12.3 共通機能ドキュメント

* [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書

### 12.4 技術標準ドキュメント

* [architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * 「8.3 静的リソースアクセスのベストプラクティス」セクション参照
* [common_rules.md](../../../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

### 12.5 プロジェクト情報

* [README.md](../../../../README.md) - プロジェクトREADME
  * 「画像API」セクション参照

### 12.6 Jakarta EE仕様

* Jakarta RESTful Web Services 3.1: https://jakarta.ee/specifications/restful-ws/3.1/
* Jakarta Servlet 6.0: https://jakarta.ee/specifications/servlet/6.0/
