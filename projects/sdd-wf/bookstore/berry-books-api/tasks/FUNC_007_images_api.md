# FUNC_007_images_api - 画像API

## メタデータ

* タスクID: FUNC_007
* 機能タイプ: API（静的リソース配信）
* 依存タスク: FUNC_001
* 並行実行可能: FUNC_004, FUNC_005, FUNC_006
* 担当者: 担当者F
* 推奨スキル: JAX-RS, ServletContext, WAR内リソースアクセス
* 想定工数: 4時間

## 実装内容

画像APIエンドポイントを実装する。
WAR内に配置された書籍カバー画像を配信する機能を提供する。

---

## タスクリスト

### 1. Resourceクラスの作成

* [ ] T_FUNC007_001: ImageResourceの作成
  * 目的: 画像APIエンドポイントを実装する
  * 対象: pro.kensait.berrybooks.api.ImageResource
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「3.1 API実装方式」
    * [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * @Path("/images")
    * @ApplicationScoped
    * @Context ServletContext注入
    * 認証不要（認証除外エンドポイント）

* [ ] T_FUNC007_002: GET /images/covers/{filename} エンドポイントの実装
  * 目的: 書籍カバー画像を配信する
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * @GET, @Path("/covers/{filename}")
    * @PathParam("filename") String filename
    * @Produces("image/jpeg")
    * ServletContext.getResourceAsStream()を使用
    * WARルート相対パス: /resources/images/covers/{filename}

### 2. WAR内リソースアクセスの実装

* [ ] T_FUNC007_003: ServletContext.getResourceAsStream()の使用
  * 目的: WAR内リソースへの安全なアクセスを実装する
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * ServletContext.getResourceAsStream("/resources/images/covers/{filename}")
    * InputStreamからbyte[]への変換
    * Response.ok(byte[]).type("image/jpeg").build()

* [ ] T_FUNC007_004: フォールバック画像の実装
  * 目的: 画像が存在しない場合にno-image.jpgを返却する
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * 画像が見つからない場合: /resources/images/covers/no-image.jpg を返却
    * InputStreamがnullの場合の処理

### 3. セキュリティ対策

* [ ] T_FUNC007_005: パストラバーサル対策の実装
  * 目的: 不正なパスでのファイルアクセスを防ぐ
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * "../" を含むパスの拒否
    * ファイル名バリデーション（英数字、ハイフン、ドット、アンダースコアのみ許可）
    * 絶対パスでの指定を防ぐ

* [ ] T_FUNC007_006: ファイルタイプの制限
  * 目的: 画像ファイル以外のアクセスを防ぐ
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: なし（セキュリティベストプラクティス）
  * 注意事項:
    * .jpgファイルのみ許可
    * その他の拡張子は拒否

### 4. エラーハンドリング

* [ ] T_FUNC007_007: 画像未検出時の処理
  * 目的: 画像が見つからない場合にフォールバック画像を返す
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.3 静的リソース配信（画像API）」
  * 注意事項:
    * 画像が見つからない場合: no-image.jpg を返却
    * no-image.jpg も見つからない場合: 500 Internal Server Error

* [ ] T_FUNC007_008: 不正なファイル名の処理
  * 目的: 不正なファイル名の場合に400 Bad Requestを返す
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: なし（セキュリティベストプラクティス）
  * 注意事項:
    * "../" を含む場合: 400 Bad Request
    * バリデーション失敗: 400 Bad Request

### 5. キャッシュ制御

* [ ] T_FUNC007_009: HTTPキャッシュヘッダーの設定
  * 目的: ブラウザキャッシュを有効化してパフォーマンスを向上する
  * 対象: ImageResource.getCoverImage()
  * 参照SPEC: なし（パフォーマンス最適化）
  * 注意事項:
    * Cache-Control: max-age=86400（24時間）
    * ETag: 画像ファイルのハッシュ値
    * Last-Modified: 画像ファイルの更新日時

---

## 完了条件

* [ ] 書籍カバー画像が正常に配信される
* [ ] 画像が存在しない場合にno-image.jpgが返される
* [ ] パストラバーサル攻撃を防げる
* [ ] 不正なファイル名の場合に400 Bad Requestが返される
* [ ] HTTPキャッシュヘッダーが正しく設定される
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
