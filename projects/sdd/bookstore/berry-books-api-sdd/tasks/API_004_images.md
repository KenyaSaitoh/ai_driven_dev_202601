# berry-books-api-sdd - API_004_images タスク

担当者: 担当者D（1名）  
推奨スキル: JAX-RS、Servlet、静的リソース配信  
想定工数: 4時間  
依存タスク: [共通機能タスク](common.md)

---

## 概要

本タスクは、画像APIの実装を行う。WAR内の静的リソース（書籍表紙画像）を配信する。

* 実装パターン: 独自実装
  * ServletContextを使用してWAR内リソースにアクセス
  * デプロイ後も正常に動作する実装

---

## タスクリスト

### 1. REST Resource

#### T_API004_001: ImageResource の作成

* [ ] T_API004_001: ImageResource の作成
  * 目的: 画像APIエンドポイントを実装する
  * 対象: ImageResource.java（JAX-RS Resource）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3. API一覧」
  * 注意事項:
    * @Path("/images")を付与
    * @ApplicationScopedを付与
    * ServletContextをインジェクション（@Context）
    * 実装メソッド:
      1. getCoverImage(int bookId): 書籍表紙画像取得
    * 処理フロー:
      1. ServletContext.getResourceAsStream()でWAR内リソースを取得
      2. パス形式: `/resources/images/covers/{bookId}.jpg`
      3. 画像が存在しない場合は`/resources/images/covers/no-image.jpg`を返却
      4. Content-Type: "image/jpeg"を設定
    * エラーハンドリング:
      * IOException発生時: 500 Internal Server Error
    * ログ出力: INFO（メソッド開始）、WARN（画像が見つからない）、ERROR（例外発生）
    * 重要: ファイルシステムAPIを使用しない（開発環境でのみ動作）

---

### 2. 静的リソースの配置

#### T_API004_002: 画像ファイルの配置

* [ ] T_API004_002: 画像ファイルの配置
  * 目的: 書籍表紙画像をWAR内に配置する
  * 対象: src/main/webapp/resources/images/covers/
  * 参照SPEC: [README.md](../README.md) の「画像API」
  * 注意事項:
    * 配置場所: `src/main/webapp/resources/images/covers/`
    * ファイル命名規則: `{bookId}.jpg`（例: 1.jpg、2.jpg）
    * フォールバック画像: `no-image.jpg`（必須）
    * 画像形式: JPEG

---

## 成果物チェックリスト

* [ ] REST Resource（ImageResource）が実装されている
* [ ] 画像ファイルがWAR内に配置されている
* [ ] 画像APIが正常に動作する（書籍表紙画像取得）
* [ ] ServletContextを使用してWAR内リソースにアクセスしている
* [ ] 画像が存在しない場合にフォールバック画像を返却する
* [ ] デプロイ後も正常に動作する

---

## 動作確認

### 書籍表紙画像取得（存在する画像）

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/images/covers/1 \
  --output 1.jpg
```

### 書籍表紙画像取得（存在しない画像）

```bash
curl -X GET http://localhost:8080/berry-books-api-sdd/api/images/covers/999 \
  --output no-image.jpg
```

---

## 参考資料

* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
* [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) の「8.3 静的リソースアクセスのベストプラクティス」
