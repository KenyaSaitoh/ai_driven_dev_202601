# 画像API (API_004_images) タスク

**担当者:** 担当者D（1名）  
**推奨スキル:** Jakarta EE、JAX-RS、ファイルI/O  
**想定工数:** 2時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

画像APIを実装します。書籍表紙画像取得の1つのエンドポイントを含みます。画像ファイルの読み込みとバイナリレスポンスを実装します。

---

## タスクリスト

### 5.1 Resourceクラス実装

- [ ] **T_API004_001**: ImageResource の作成（画像取得機能）
  - **目的**: 書籍表紙画像取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.ImageResource` の getCoverImage() メソッド
  - **参照SPEC**: 
    - [api/API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「3.1 書籍表紙画像取得」
    - [api/API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「2. 書籍表紙画像取得」
  - **注意事項**: 
    - @Path("/images"), @GET, @Path("/covers/{bookId}") を使用
    - Content-Type: image/jpeg を返す
    - 画像が存在しない場合はno-image.jpgを返す
    - 認証不要

---

### 5.2 画像ファイル配置

- [ ] **T_API004_002**: フォールバック画像の配置
  - **目的**: フォールバック画像ファイルを配置する
  - **対象**: `src/main/webapp/resources/images/covers/no-image.jpg`
  - **参照SPEC**: [api/API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) の「4.2 画像格納場所」
  - **注意事項**: 画像が見つからない場合に使用される

---

### 5.3 ユニットテスト

- [ ] [P] **T_API004_003**: ImageResource のユニットテスト
  - **目的**: 画像APIエンドポイントのテストを実装する
  - **対象**: `pro.kensait.berrybooks.api.ImageResourceTest`
  - **参照SPEC**: [api/API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) の「2. 書籍表紙画像取得」
  - **注意事項**: 
    - JUnit 5 を使用
    - 画像が存在する場合、存在しない場合のテスト
    - Content-Type: image/jpeg のチェック

---

### 5.4 APIテスト（E2E）

- [ ] **T_API004_004**: 画像APIのE2Eテスト
  - **目的**: 画像API全体のE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（JUnit 5またはPlaywright）
  - **参照SPEC**: [api/API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md)
  - **注意事項**: 
    - 画像取得のテスト
    - フォールバック画像のテスト
    - Content-Typeのチェック

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] ImageResourceが実装されている
- [ ] 画像ファイルが正常に返される
- [ ] フォールバック画像が正常に機能する
- [ ] ユニットテストが実装されている
- [ ] E2Eテストが実装され、正常に動作する

---

## 参考資料

- [api/API_004_images/functional_design.md](../specs/baseline/api/API_004_images/functional_design.md) - 画像API機能設計書
- [api/API_004_images/behaviors.md](../specs/baseline/api/API_004_images/behaviors.md) - 画像API受入基準
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
