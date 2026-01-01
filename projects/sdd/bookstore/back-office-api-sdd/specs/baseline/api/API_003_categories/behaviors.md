# カテゴリAPI 振る舞い仕様書

## 1. 概要

本ドキュメントは、カテゴリAPI（`/api/categories`）の動的な振る舞いをシーケンス図とフローチャートで定義する。

## 2. カテゴリ一覧取得シーケンス

### 2.1 正常系: 全件取得

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │CategoryResource      │CategoryService      │CategoryDao│      │Database│
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                      │                    │               │
   │GET /api/categories  │                      │                    │               │
   ├────────────────────►│                      │                    │               │
   │                     │                      │                    │               │
   │                     │LOG INFO              │                    │               │
   │                     │──────────┐           │                    │               │
   │                     │          │           │                    │               │
   │                     │[ CategoryResource#   │                    │               │
   │                     │  getAllCategories ]  │                    │               │
   │                     │◄─────────┘           │                    │               │
   │                     │                      │                    │               │
   │                     │getAllCategories()    │                    │               │
   │                     ├─────────────────────►│                    │               │
   │                     │                      │                    │               │
   │                     │                      │LOG INFO            │               │
   │                     │                      │──────────┐         │               │
   │                     │                      │          │         │               │
   │                     │                      │[ CategoryService#  │               │
   │                     │                      │  getCategoriesAll ]│               │
   │                     │                      │◄─────────┘         │               │
   │                     │                      │                    │               │
   │                     │                      │getCategoriesAll()  │               │
   │                     │                      ├───────────────────►│               │
   │                     │                      │                    │               │
   │                     │                      │                    │findAll()      │
   │                     │                      │                    ├──────────────►│
   │                     │                      │                    │               │
   │                     │                      │                    │Named Query:   │
   │                     │                      │                    │Category.findAll│
   │                     │                      │                    │               │
   │                     │                      │                    │SELECT c       │
   │                     │                      │                    │FROM CATEGORY c│
   │                     │                      │                    │ORDER BY       │
   │                     │                      │                    │ c.CATEGORY_ID │
   │                     │                      │                    │               │
   │                     │                      │                    │◄──────────────┤
   │                     │                      │                    │List<Category> │
   │                     │                      │                    │ (4 records)   │
   │                     │                      │◄───────────────────┤               │
   │                     │                      │List<Category>      │               │
   │                     │◄─────────────────────┤                    │               │
   │                     │List<Category>        │                    │               │
   │                     │                      │                    │               │
   │                     │Stream & Map          │                    │               │
   │                     │──────────┐           │                    │               │
   │                     │          │           │                    │               │
   │                     │categories.stream()   │                    │               │
   │                     │ .map(c -> new        │                    │               │
   │                     │   CategoryTO(        │                    │               │
   │                     │    c.getCategoryId(),│                    │               │
   │                     │    c.getCategoryName()                    │               │
   │                     │   ))                 │                    │               │
   │                     │ .toList()            │                    │               │
   │                     │◄─────────┘           │                    │               │
   │                     │                      │                    │               │
   │◄────────────────────┤200 OK                │                    │               │
   │[{CategoryTO}]       │Content-Type:         │                    │               │
   │                     │application/json      │                    │               │
   │                     │                      │                    │               │
```

### 2.2 データ変換の詳細

```
Category Entity         →    CategoryTO
───────────────────────────────────────────
categoryId: 1           →    categoryId: 1
categoryName: "文学"     →    categoryName: "文学"

categoryId: 2           →    categoryId: 2
categoryName: "ビジネス"  →    categoryName: "ビジネス"

categoryId: 3           →    categoryId: 3
categoryName: "技術"     →    categoryName: "技術"

categoryId: 4           →    categoryId: 4
categoryName: "歴史"     →    categoryName: "歴史"
```

## 3. 処理フローチャート

```
┌──────────────┐
│  開始         │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│リクエスト受信  │
│GET /categories│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│CategoryResource│
│getAllCategories()│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│CategoryService│
│getCategoriesAll()│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│CategoryDao   │
│findAll()     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│DB Query実行   │
│SELECT c      │
│FROM CATEGORY │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│List<Category>│
│取得           │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│Stream API    │
│変換処理       │
│Category→     │
│CategoryTO    │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│List<CategoryTO│
│生成           │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│JSON Serialize│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│200 OK        │
│レスポンス返却│
└──────────────┘
```

## 4. データフロー全体図

```
┌──────────────────────────────────────────────────────┐
│                       Client                         │
└───────────────────────┬──────────────────────────────┘
                        │ HTTP GET
                        │ /api/categories
                        ▼
┌──────────────────────────────────────────────────────┐
│                 CategoryResource                     │
│  - getAllCategories()                                │
└───────────────────────┬──────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────┐
│                 CategoryService                      │
│  - getCategoriesAll()                                │
└───────────────────────┬──────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────┐
│                   CategoryDao                        │
│  - findAll()                                         │
└───────────────────────┬──────────────────────────────┘
                        │ EntityManager / JPQL
                        ▼
┌──────────────────────────────────────────────────────┐
│                     Database                         │
│  - CATEGORY table                                    │
└──────────────────────────────────────────────────────┘
```

## 5. パフォーマンス特性

### 5.1 レスポンスタイム分析

```
カテゴリ一覧取得 (Total: ~50ms)

┌────────────────────────────────────────────────────────┐
│ DB Query (SELECT)                        20ms (40%)   │
├────────────────────────────────────────────────────────┤
│ Entity → DTO Conversion                  10ms (20%)   │
├────────────────────────────────────────────────────────┤
│ JSON Serialization                       10ms (20%)   │
├────────────────────────────────────────────────────────┤
│ その他（ネットワーク、オーバーヘッド）     10ms (20%)   │
└────────────────────────────────────────────────────────┘
```

### 5.2 キャッシング効果（将来）

```
キャッシュなし:
┌────────────────────────────────────────┐
│ First Request   ████████████  50ms    │
│ Second Request  ████████████  50ms    │
│ Third Request   ████████████  50ms    │
└────────────────────────────────────────┘

キャッシュあり（実装後）:
┌────────────────────────────────────────┐
│ First Request   ████████████  50ms    │
│ Second Request  █░░░░░░░░░░░  5ms     │
│ Third Request   █░░░░░░░░░░░  5ms     │
└────────────────────────────────────────┘
```

**推奨**: マスタデータのため、アプリケーションレベルキャッシュを実装

## 6. ログ出力シーケンス

### 6.1 正常系ログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ CategoryResource#getAllCategories ]
t2      INFO    [ CategoryService#getCategoriesAll ]
t3      DEBUG   [ CategoryDao#findAll ] Executing JPQL: SELECT c FROM Category c
t4      DEBUG   [ CategoryDao#findAll ] Result count: 4
t5      DEBUG   [ CategoryResource ] Converting 4 entities to DTOs
t6      INFO    [ CategoryResource#getAllCategories ] Success: 4 categories returned
```

### 6.2 DBエラー時のログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ CategoryResource#getAllCategories ]
t2      INFO    [ CategoryService#getCategoriesAll ]
t3      DEBUG   [ CategoryDao#findAll ] Executing JPQL: SELECT c FROM Category c
t4      ERROR   [ CategoryDao#findAll ] Database error
                java.sql.SQLException: Connection refused
t5      ERROR   [ CategoryResource#getAllCategories ] Error retrieving categories
t6      INFO    [ CategoryResource#getAllCategories ] Returning 500 Internal Server Error
```

## 7. 並行処理シナリオ

### 7.1 同時リクエスト

```
時刻   ユーザーA                    ユーザーB                    ユーザーC
t1    GET /api/categories
t2                                GET /api/categories
t3                                                            GET /api/categories
t4    → DB Query (1)
t5                                → DB Query (2)
t6                                                            → DB Query (3)
t7    ← Result (4 categories)
t8                                ← Result (4 categories)
t9                                                            ← Result (4 categories)
```

**結果**: 独立したトランザクションで並行実行可能

### 7.2 大量リクエスト時

```
1000 concurrent requests

┌────────────────────────────────────────┐
│ Connection Pool (Max: 20)              │
├────────────────────────────────────────┤
│ All requests complete in ~3 seconds    │
│ Average response time: 150ms           │
│ (50ms query + 100ms wait)              │
└────────────────────────────────────────┘
```

**最適化案**:
- アプリケーションレベルキャッシュの実装
- CDNでの静的コンテンツ配信
- レスポンスの圧縮（gzip）

## 8. 比較: `/api/categories` vs `/api/books/categories`

### 8.1 レスポンス形式の違い

```
┌─────────────────────────────────────────────────────┐
│         `/api/categories`                           │
│         (配列形式 - 推奨)                             │
├─────────────────────────────────────────────────────┤
│ [                                                   │
│   {"categoryId": 1, "categoryName": "文学"},         │
│   {"categoryId": 2, "categoryName": "ビジネス"}      │
│ ]                                                   │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│         `/api/books/categories`                     │
│         (Map形式 - 後方互換性)                        │
├─────────────────────────────────────────────────────┤
│ {                                                   │
│   "文学": 1,                                         │
│   "ビジネス": 2                                      │
│ }                                                   │
└─────────────────────────────────────────────────────┘
```

### 8.2 用途の違い

```
┌──────────────┐
│  クライアント  │
└──────┬───────┘
       │
       ├────────────────────┐
       │                    │
       ▼                    ▼
┌──────────────┐    ┌──────────────┐
│新規UI        │    │既存UI        │
│(推奨)        │    │(後方互換)     │
└──────┬───────┘    └──────┬───────┘
       │                    │
       ▼                    ▼
┌──────────────┐    ┌──────────────┐
│/api/categories    │/api/books/   │
│              │    │categories    │
│配列形式       │    │Map形式       │
│標準REST      │    │            │
└──────────────┘    └──────────────┘
```

## 9. 状態管理

カテゴリAPIは状態を持たない（ステートレス）:

```
┌──────────────┐
│ Request 1    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Category │   │ Database │
                   │ Resource │   └──────────┘
┌──────────────┐   └──────────┘
│ Request 2    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Category │   │ Database │
                   │ Resource │   └──────────┘
┌──────────────┐   └──────────┘
│ Request 3    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Category │   │ Database │
                   │ Resource │   └──────────┘
                   └──────────┘

各リクエストは独立して処理される
```

## 10. 将来の拡張シーケンス

### 10.1 カテゴリ詳細取得（実装予定）

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌───────────┐
│Client│         │CategoryResource      │CategoryService      │CategoryDao│
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └─────┬─────┘
   │                     │                      │                    │
   │GET /api/categories/1│                      │                    │
   ├────────────────────►│                      │                    │
   │                     │                      │                    │
   │                     │getCategory(1)        │                    │
   │                     ├─────────────────────►│                    │
   │                     │                      │findById(1)         │
   │                     │                      ├───────────────────►│
   │                     │                      │                    │
   │                     │                      │◄───────────────────┤
   │                     │                      │Category            │
   │                     │◄─────────────────────┤                    │
   │                     │Category              │                    │
   │                     │                      │                    │
   │◄────────────────────┤200 OK                │                    │
   │{CategoryTO}         │                      │                    │
   │                     │                      │                    │
```

### 10.2 カテゴリ統計情報（実装予定）

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌────────┐
│Client│         │CategoryResource      │CategoryService      │BookDao │
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └───┬────┘
   │                     │                      │                 │
   │GET /api/categories  │                      │                 │
   │  ?includeStats=true │                      │                 │
   ├────────────────────►│                      │                 │
   │                     │                      │                 │
   │                     │getCategoriesWithStats()                │
   │                     ├─────────────────────►│                 │
   │                     │                      │                 │
   │                     │                      │countByCategory()│
   │                     │                      ├────────────────►│
   │                     │                      │                 │
   │                     │                      │◄────────────────┤
   │                     │                      │Map<CategoryId,  │
   │                     │                      │     Count>      │
   │                     │◄─────────────────────┤                 │
   │                     │                      │                 │
   │◄────────────────────┤200 OK                │                 │
   │[{categoryId: 1,     │                      │                 │
   │  categoryName: "文学",                      │                 │
   │  bookCount: 150}]   │                      │                 │
   │                     │                      │                 │
```

