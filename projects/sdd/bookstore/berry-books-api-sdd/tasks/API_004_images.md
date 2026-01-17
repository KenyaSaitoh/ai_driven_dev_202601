# API_004_images - 画像API実装タスク

担当者: 担当者D（1名）  
推奨スキル: JAX-RS, Servlet, WAR内リソースアクセス  
想定工数: 2時間  
依存タスク: [common_tasks.md](common_tasks.md)

---

## 概要

画像APIを実装する。書籍表紙画像の取得エンドポイントを提供する。WAR内リソースにServletContext経由でアクセスし、画像が存在しない場合はフォールバック画像を返却する。

---

## タスクリスト

### Resource層

#### T_API004_001: ImageResource の作成

* 目的: 画像APIのRESTエンドポイントを実装する
* 対象: ImageResource.java (JAX-RS Resourceクラス)
* 参照SPEC: 
  * [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「3.1 書籍表紙画像取得」
  * [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「5. 画像API (`/api/images`)」
* 注意事項: 
  * @Path("/images")
  * @ApplicationScoped
  * ServletContextをインジェクション（@Context）
  * getCoverImageメソッド（GET /covers/{bookId}）
  * ServletContext.getResourceAsStream()でWAR内リソースにアクセス
  * パス形式: `/resources/images/covers/{bookId}.jpg`
  * 画像が存在しない場合はno-image.jpgを返却
  * Content-Type: image/jpeg
  * 認証不要（公開API）

---

### ユニットテスト

#### T_API004_002: [P] ImageResourceのテスト作成

* 目的: ImageResourceのユニットテストを実装する
* 対象: ImageResourceTest.java (JUnit 5テストクラス)
* 参照SPEC: [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「5. 画像API (`/api/images`)」
* 注意事項: 
  * JUnit 5 + Mockitoを使用
  * ServletContextをモック化
  * 正常系・異常系テストケース
  * 画像取得成功、画像が存在しない場合（フォールバック画像返却）

---

## 完了基準

* [ ] ImageResource（書籍表紙画像取得）が実装されている
* [ ] ImageResourceのユニットテストが実装されている
* [ ] 書籍表紙画像取得エンドポイント（GET /api/images/covers/{bookId}）が動作する
* [ ] 画像が存在しない場合にフォールバック画像（no-image.jpg）が返却される
* [ ] ServletContextを使用してWAR内リソースに正しくアクセスできる

---

## 参考資料

* [API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) - 画像API機能設計書
* [API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) - 画像API受入基準
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
