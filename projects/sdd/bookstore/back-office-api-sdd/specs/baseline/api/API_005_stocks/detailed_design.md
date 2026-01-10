# API_005 在庫API - 詳細設計書

**API ID**: API_005  
**API名**: 在庫API  
**バージョン**: 1.0.0  
**最終更新**: 2025-01-10

---

## 1. パッケージ構造

```
pro.kensait.backoffice
├── api
│   ├── StockResource.java           # 在庫リソース
│   ├── dto
│   │   ├── StockTO.java             # 在庫転送オブジェクト
│   │   └── StockUpdateRequest.java  # 在庫更新リクエスト
│   └── exception
│       └── OptimisticLockExceptionMapper.java  # 楽観的ロック例外マッパー
├── dao
│   └── StockDao.java                # 在庫データアクセス
└── entity
    └── Stock.java                   # 在庫エンティティ
```

---

## 2. クラス設計

### 2.1 StockResource（JAX-RS Resource）

**ベースパス**: `/stocks`

**エンドポイント**:
- `GET /stocks` - 全在庫取得
- `GET /stocks/{bookId}` - 在庫取得
- `PUT /stocks/{bookId}` - 在庫更新（楽観的ロック対応）

### 2.2 StockTO（DTO - Record）

```java
public record StockTO(
    Integer bookId,
    Integer quantity,
    Integer version
) {}
```

### 2.3 StockUpdateRequest（DTO - Record）

```java
public record StockUpdateRequest(
    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上である必要があります")
    Integer quantity,
    
    @NotNull(message = "バージョンは必須です")
    Integer version
) {}
```

### 2.4 StockDao

**主要メソッド**:
- `List<Stock> findAll()` - 全在庫取得
- `Stock findById(Integer bookId)` - 在庫取得
- `void update(Stock stock)` - 在庫更新（楽観的ロック）

**JPQL**:
```sql
SELECT s FROM Stock s ORDER BY s.bookId
```

### 2.5 Stock（エンティティ）

**テーブル**: `STOCK`

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `bookId` | `Integer` | `BOOK_ID` | `@Id` | 書籍ID（主キー） |
| `quantity` | `Integer` | `QUANTITY` | `@Column(nullable=false)` | 在庫数 |
| `version` | `Integer` | `VERSION` | `@Version` | 楽観的ロックバージョン |

**アノテーション**:
```java
@Entity
@Table(name = "STOCK")
```

---

## 3. 楽観的ロック実装

### 3.1 楽観的ロックの仕組み

1. **読み取り時**: `version`フィールドの値を取得
2. **更新時**: WHERE句に`version`を含める
   ```sql
   UPDATE STOCK SET quantity = ?, version = version + 1 
   WHERE book_id = ? AND version = ?
   ```
3. **競合検出**: 更新行数が0の場合、`OptimisticLockException`をスロー
4. **バージョン自動インクリメント**: JPAが自動的に`version`をインクリメント

### 3.2 エラーハンドリング

**OptimisticLockExceptionMapper**:
- `OptimisticLockException`を捕捉
- HTTP 409 Conflictを返却
- エラーメッセージ: "在庫が他のユーザーによって更新されました。最新の在庫情報を確認してください。"

---

## 4. セキュリティ考慮事項

### 4.1 並行制御

- 楽観的ロックにより、同時更新時の不整合を防止
- クライアントは最新の`version`を送信する必要がある
- 競合時はクライアントに再試行を促す

---

## 5. テスト要件

### 5.1 ユニットテスト

**対象**: `StockDao`

- 在庫更新テスト（正常系）
- 在庫更新テスト（楽観的ロック競合）

### 5.2 統合テスト

**対象**: `StockResource`

- 在庫更新API呼び出し（正常系）
- 在庫更新API呼び出し（バージョン不一致）

---

## 6. 参考資料

- [functional_design.md](functional_design.md) - 機能設計書
- [behaviors.md](behaviors.md) - 振る舞い仕様書
- [JPA楽観的ロック](https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html#a12275)
