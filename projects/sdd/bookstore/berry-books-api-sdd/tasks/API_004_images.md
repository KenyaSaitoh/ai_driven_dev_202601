# API_004_images - 画像APIタスク

担当者: 担当者D（1名）  
推奨スキル: JAX-RS、ServletContext、WAR内リソースアクセス  
想定工数: 2時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## タスク一覧

### Resource作成

* [X] T_API004_001: ImageResourceの作成
  * 目的: 画像APIエンドポイントを実装する
  * 対象: ImageResource.java（@Path("/images"), @ApplicationScoped）
  * 参照SPEC: 
    * [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「3. API仕様」
    * [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「5. 画像API」
  * 注意事項: ServletContextをインジェクション（@Context）、画像ベースパス定数を定義
  * 完了日: 2026-01-10

---

### エンドポイント実装

* [X] T_API004_002: getCoverImageメソッドの実装
  * 目的: 書籍表紙画像取得エンドポイントを実装する
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: 
    * [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「3.1 書籍表紙画像取得」
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 静的リソース配信」
  * 注意事項: ServletContext.getResourceAsStream()を使用、画像が存在しない場合はno-image.jpgを返却、Content-Type: image/jpeg
  * 完了日: 2026-01-10

---

### 単体テスト

* [X] [P] T_API004_003: ImageResourceのテスト
  * 目的: ImageResourceの単体テストを実装する
  * 対象: ImageResourceTest.java（JUnit 5 + Mockito）
  * 参照SPEC: [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「5. 画像API」
  * 注意事項: ServletContextをモック、画像が存在する場合・存在しない場合のテスト
  * 完了日: 2026-01-10

---

### APIテスト（オプション）

* [X] [P] T_API004_004: 画像APIのE2Eテスト
  * 目的: 画像APIのE2Eテストを実装する（オプション）
  * 対象: ImageApiE2ETest.java（JUnit 5 + HttpClient）
  * 参照SPEC: [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「5. 画像API」
  * 注意事項: 実際の画像ファイルが返却されることを確認、Content-Typeが image/jpeg であることを確認、@Tag("e2e")でタグ付け
  * 完了日: 2026-01-10

---

## 実装時の注意事項

### WAR内リソースアクセス

* 問題: ファイルシステムの相対パス（`src/main/webapp/...`）はデプロイ後に機能しない

理由:
* WARファイルはアーカイブ（ZIPのような形式）
* ファイルシステムAPIでは直接アクセスできない
* デプロイ先サーバーのディレクトリ構造は開発環境と異なる

* 解決策: ServletContextを使用

非推奨アプローチ:
```java
// ❌ デプロイ後に動作しない
File imageFile = new File("src/main/webapp/resources/images/covers/1.jpg");
```

推奨アプローチ:
```java
// ✅ デプロイ後も正常に動作
@Context
private ServletContext servletContext;

public Response getCoverImage(@PathParam("bookId") Integer bookId) {
    String imagePath = "/resources/images/covers/" + bookId + ".jpg";
    InputStream inputStream = servletContext.getResourceAsStream(imagePath);
    
    if (inputStream == null) {
        // フォールバック画像
        imagePath = "/resources/images/covers/no-image.jpg";
        inputStream = servletContext.getResourceAsStream(imagePath);
    }
    
    byte[] imageData = inputStream.readAllBytes();
    return Response.ok(imageData, "image/jpeg").build();
}
```

### パス形式

WAR内リソースパス:
* `/resources/images/covers/{bookId}.jpg`
* `/` から始まる（WARルート相対）
* `src/main/webapp/` を除いた部分

### ファイル命名規則

形式: `{bookId}.jpg`

例:
* `1.jpg` - Book ID 1の表紙画像
* `2.jpg` - Book ID 2の表紙画像
* `no-image.jpg` - フォールバック画像（必須）

### フォールバック処理

画像が存在しない場合、`no-image.jpg`を返却します。HTTPステータスは200 OK（エラーではない）。

### 認証要否

全てのエンドポイントは認証不要です。JwtAuthenFilterの公開エンドポイントリストに含まれます。

---

## 参考資料

* [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) - 画像API機能設計書
* [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) - 画像API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 静的リソース配信」
* [architecture_design.md](../specs/baseline/system/architecture_design.md) の「16.2.1 静的リソースアクセスのベストプラクティス」
