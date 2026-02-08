# 画像配信ドメイン - 詳細設計書

ドメイン名: images  
バージョン: 1.0.0  
最終更新: 2026-02-07

---

## 重要な原則

この詳細設計書は、**基本設計（what/why）と実装コード（how）の橋渡しとなる設計判断のみを簡潔に記載**します。

**記載すべき情報**:
* クラス名と責務（1行）
* 主要メソッドのシグネチャ（引数、戻り値、例外）
* 設計判断を示すアノテーション（@Transactional, @Path等）
* JPQLクエリ（WHERE句、JOIN等の設計判断）
* 依存関係（@Inject対象）

**記載すべきでない情報**:
* メソッドの実装詳細、処理ステップ
* すべてのフィールド定義、getter/setter
* バリデーションの詳細
* 基本設計SPECの内容の繰り返し

---

## 1. ドメイン概要

* ドメイン名: images
* 責務: WAR内の画像リソースを配信する
* 依存関係: commonドメインに依存（ErrorResponse）

---

## 2. クラス構成

### 2.1 パッケージ構造

```
pro.kensait.berrybooks
└── api/
    └── ImageResource.java
```

---

## 3. コンポーネント設計

### 3.1 Resource - ImageResource

**責務**: WAR内の画像ファイルを配信する

**アノテーション**:
* `@Path("/images")` - エンドポイントのベースパス
* `@ApplicationScoped` - アプリケーションスコープ
* `@Produces(MediaType.APPLICATION_OCTET_STREAM)` - バイナリデータを返却

**依存関係**:
* `@Context ServletContext servletContext` - WAR内リソースへのアクセス

**主要メソッド**:

#### getImage

```java
@GET
@Path("/{filename}")
public Response getImage(@PathParam("filename") String filename)
```

* **目的**: 指定されたファイル名の画像をWAR内から取得して配信する
* **処理フロー**:
  1. ServletContext.getResourceAsStream()でWAR内リソースを取得
  2. リソースパス: `/resources/images/{filename}`
  3. ファイルが存在しない場合はno-image.jpgをフォールバック
  4. Content-Typeを拡張子から判定（.png → image/png, .jpg → image/jpeg等）
  5. バイナリデータをストリームで返却
* **戻り値**: Response（200 OK + 画像バイナリ、または404 Not Found）
* **注意事項**: 
  * パストラバーサル攻撃対策として、ファイル名に `..` や `/` が含まれる場合は拒否
  * フォールバック画像（no-image.jpg）は必ず配置すること

---

## 4. Content-Type判定

画像の拡張子からContent-Typeを判定:

* `.png` → `image/png`
* `.jpg`, `.jpeg` → `image/jpeg`
* `.gif` → `image/gif`
* その他 → `application/octet-stream`

---

## 5. リソース配置

* 配置先: `src/main/webapp/resources/images/`
* アクセスパス: `/resources/images/{filename}`
* フォールバック画像: `src/main/webapp/resources/images/no-image.jpg`

---

## 6. セキュリティ考慮事項

* **認証**: 不要（認証除外エンドポイント）
* **パストラバーサル対策**: ファイル名に `..` や `/` が含まれる場合は400 Bad Requestを返却
* **アクセス制御**: `/resources/images/` 配下のファイルのみアクセス可能

---

## 7. エラーハンドリング

* **ファイルが存在しない場合**: no-image.jpgをフォールバック（200 OK）
* **不正なファイル名**: 400 Bad Request
* **その他のエラー**: 500 Internal Server Error

---

## 8. 参考資料

* [behaviors.md](behaviors.md) - 単体テスト用振る舞い仕様書
* [../../basic_design/images/functional_design.md](../../basic_design/images/functional_design.md) - ドメイン機能設計書
* [../../basic_design/common/architecture_design.md](../../basic_design/common/architecture_design.md) - アーキテクチャ設計書