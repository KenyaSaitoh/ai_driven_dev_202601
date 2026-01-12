# API_004 出版社API - 詳細設計書

* API ID: API_004  
* API名: 出版社API  
* バージョン: 1.0.0  
* 最終更新: 2025-01-10

---

## 1. パッケージ構造

```
pro.kensait.backoffice
├── api
│   ├── PublisherResource.java       # 出版社リソース
│   └── dto
│       └── PublisherTO.java         # 出版社転送オブジェクト
├── service
│   └── publisher
│       └── PublisherService.java    # 出版社サービス
├── dao
│   └── PublisherDao.java            # 出版社データアクセス
└── entity
    └── Publisher.java               # 出版社エンティティ
```

---

## 2. クラス設計

### 2.1 PublisherResource（JAX-RS Resource）

* ベースパス: `/publishers`

* エンドポイント:
  * `GET /publishers` - 全出版社取得
  * `GET /publishers/{id}` - 出版社詳細取得

### 2.2 PublisherTO（DTO - Record）

```java
public record PublisherTO(
    Integer publisherId,
    String publisherName
) {}
```

### 2.3 PublisherService

* 主要メソッド:
  * `List<PublisherTO> getPublishersAll()` - 全出版社取得
  * `PublisherTO getPublisherById(Integer publisherId)` - 出版社詳細取得

### 2.4 PublisherDao

* 主要メソッド:
  * `List<Publisher> findAll()` - 全出版社取得
  * `Publisher findById(Integer publisherId)` - 出版社取得

* JPQL:
```sql
SELECT p FROM Publisher p ORDER BY p.publisherId
```

### 2.5 Publisher（エンティティ）

* テーブル: `PUBLISHER`

| フィールド名 | 型 | カラム名 | 制約 |
|------------|---|---------|-----|
| `publisherId` | `Integer` | `PUBLISHER_ID` | `@Id @GeneratedValue` |
| `publisherName` | `String` | `PUBLISHER_NAME` | `@Column(nullable=false, unique=true)` |

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
