# API_003 カテゴリAPI - 詳細設計書

**API ID**: API_003  
**API名**: カテゴリAPI  
**バージョン**: 1.0.0  
**最終更新**: 2025-01-10

---

## 1. パッケージ構造

```
pro.kensait.backoffice
├── api
│   ├── CategoryResource.java        # カテゴリリソース
│   └── dto
│       └── CategoryTO.java          # カテゴリ転送オブジェクト
├── service
│   └── category
│       └── CategoryService.java     # カテゴリサービス
├── dao
│   └── CategoryDao.java             # カテゴリデータアクセス
└── entity
    └── Category.java                # カテゴリエンティティ
```

---

## 2. クラス設計

### 2.1 CategoryResource（JAX-RS Resource）

**ベースパス**: `/categories`

**エンドポイント**:
- `GET /categories` - 全カテゴリ取得
- `GET /categories/{id}` - カテゴリ詳細取得

### 2.2 CategoryTO（DTO - Record）

```java
public record CategoryTO(
    Integer categoryId,
    String categoryName
) {}
```

### 2.3 CategoryService

**主要メソッド**:
- `List<CategoryTO> getCategoriesAll()` - 全カテゴリ取得
- `CategoryTO getCategoryById(Integer categoryId)` - カテゴリ詳細取得

### 2.4 CategoryDao

**主要メソッド**:
- `List<Category> findAll()` - 全カテゴリ取得
- `Category findById(Integer categoryId)` - カテゴリ取得

**JPQL**:
```sql
SELECT c FROM Category c ORDER BY c.categoryId
```

### 2.5 Category（エンティティ）

**テーブル**: `CATEGORY`

| フィールド名 | 型 | カラム名 | 制約 |
|------------|---|---------|-----|
| `categoryId` | `Integer` | `CATEGORY_ID` | `@Id @GeneratedValue` |
| `categoryName` | `String` | `CATEGORY_NAME` | `@Column(nullable=false, unique=true)` |

---

## 3. 参考資料

- [functional_design.md](functional_design.md) - 機能設計書
- [behaviors.md](behaviors.md) - 振る舞い仕様書
