# [API_ID] - API詳細設計書

ファイル名: detailed_design.md（api/[API_ID]/配下）  
API ID: [API_ID]  
API名: [API_NAME]  
バージョン: 1.0.0  
最終更新: [DATE]

---

## 1. API概要

* ベースパス: `/api/[path]`
* 認証: [要/不要]
* 実装パターン: [完全なCRUD実装 / プロキシ転送 / 独自実装 + 外部連携 / 静的リソース配信]

---

## 2. パッケージ構造

### 2.1 API固有パッケージ

```
[BASE_PACKAGE]
├── api
│   ├── [Resource名].java
│   ├── dto
│   │   ├── [Request名].java
│   │   └── [Response名].java
│   └── exception
│       └── [ExceptionMapper名].java（該当する場合）
├── service（API固有のビジネスロジックがある場合）
│   └── [パッケージ]
│       └── [Service名].java
└── external（外部API連携がある場合）
    ├── [RestClient名].java
    └── dto
        └── [外部API用DTO名].java
```

注意: エンティティ、Dao、共通Serviceは[../../system/detailed_design.md](../../system/detailed_design.md)を参照

---

## 3. Resourceクラス設計

### 3.1 [Resource名]（JAX-RS Resource）

* 責務: [責務の説明]

* アノテーション:
  * `@Path("[path]")` - ベースパス
  * `@ApplicationScoped` - CDIスコープ（依存性注入）

* 主要メソッド:

#### [メソッド名]() - [機能名]

```
@[HTTPメソッド]
@Path("[パス]")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
```

* パラメータ:
  * `[型] [変数名]` - [説明]

* 処理フロー:
  1. [ステップ1]
  2. [ステップ2]
  3. [ステップ3]

* レスポンス: `[型]`

* エラーケース:
  * [エラーケース1] → `[HTTPステータス]`

---

## 4. DTO設計

### 4.1 [Request名]（リクエストDTO - Record）

```java
public record [名前](
    @NotBlank(message = "[メッセージ]")
    String [フィールド名],
    
    [その他フィールド]
) {}
```

### 4.2 [Response名]（レスポンスDTO - Record）

```java
public record [名前](
    String [フィールド名],
    
    [その他フィールド]
) {}
```

---

## 5. API固有のビジネスロジック

注意: このセクションには、このAPI固有のビジネスロジックを記述します。複数のAPIで共有されるビジネスロジックは、system/detailed_design.mdに記述してください。

### 5.1 [API固有のService名]

* 責務: [責務の説明]（このAPIのみで使用される）

* アノテーション:
  * `@ApplicationScoped`（依存性注入）
  * `@Transactional`（該当する場合）

* 主要メソッド:

#### [メソッド名]()

* シグネチャ:
```java
public [戻り値型] [メソッド名]([引数])
```

* 処理:
  1. [ステップ1]
  2. [ステップ2]

* トランザクション境界: [説明]

* 依存関係:
  * 共通Serviceの使用: [該当する場合]
  * Daoの使用: [該当する場合]
  * 外部APIクライアントの使用: [該当する場合]

---

### 5.2 [API固有のService名2]

[必要に応じて追加のAPI固有のServiceを記述]

注意: 共通Serviceメソッドは、このセクションには含めません。system/detailed_design.mdを参照してください。

---

## 6. 外部API連携（該当する場合）

### 6.1 [RestClient名]

* 責務: [外部API名]との連携

* 主要メソッド:

#### [メソッド名]()

* シグネチャ:
```java
public [戻り値型] [メソッド名]([引数])
```

* 外部APIエンドポイント: `[メソッド] [パス]`

* エラーハンドリング:
  * [エラーケース1]
  * [エラーケース2]

---

## 7. エラーハンドリング

### 7.1 エラーシナリオ

| エラーケース | HTTPステータス | レスポンス |
|------------|--------------|----------|
| [エラー1] | [ステータス] | `[レスポンス]` |

---

## 8. テスト要件

### 8.1 ユニットテスト

* 対象: `[クラス名]`

* [テストケース1]
* [テストケース2]

### 8.2 統合テスト

* 対象: [対象の説明]

* [テストケース1]
* [テストケース2]

---

## 9. 参考資料

* [functional_design.md](functional_design.md) - API機能設計書
* [behaviors.md](behaviors.md) - API振る舞い仕様書
* [../../system/detailed_design.md](../../system/detailed_design.md) - システム詳細設計書（エンティティ、Dao、共通Service）
* [../../system/functional_design.md](../../system/functional_design.md) - システム機能設計書
