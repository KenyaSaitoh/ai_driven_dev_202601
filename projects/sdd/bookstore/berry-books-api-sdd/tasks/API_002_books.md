# 書籍API (API_002_books) タスク

**担当者:** 担当者B（1名）  
**推奨スキル:** Jakarta EE、JAX-RS、JPA、JPQL  
**想定工数:** 5時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## 概要

書籍APIを実装します。書籍一覧取得、詳細取得、検索、カテゴリ一覧取得の4つのエンドポイントを含みます。N+1問題を回避するためのJOIN FETCHクエリを実装します。

---

## タスクリスト

### 3.1 DAOクラス実装

- [ ] **T_API002_001**: BookDao の作成
  - **目的**: 書籍データアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.BookDao`
  - **参照SPEC**: 
    - [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「6. パフォーマンス最適化」
    - [data_model.md](../specs/baseline/system/data_model.md) の「5.1 書籍検索（N+1問題回避）」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - JOIN FETCH で Category、Publisher、Stock を一括取得
    - findAll(), findById(), findByCategoryId(), searchBooks() メソッドを実装

---

### 3.2 Serviceクラス実装

- [ ] **T_API002_002**: BookService の作成
  - **目的**: 書籍ビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.book.BookService`
  - **参照SPEC**: [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. API仕様」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - BookDao と CategoryDao を注入
    - getAllBooks(), getBookById(), searchBooks(), getCategoryMap() メソッドを実装

- [ ] **T_API002_003**: CategoryService の作成
  - **目的**: カテゴリ管理サービスを実装する
  - **対象**: `pro.kensait.berrybooks.service.category.CategoryService`
  - **参照SPEC**: [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 カテゴリ一覧取得」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - CategoryDao を注入
    - getCategoryMap() メソッドを実装（Map<String, Integer> 形式）

---

### 3.3 Resourceクラス実装

- [ ] **T_API002_004**: BookResource の作成（一覧取得機能）
  - **目的**: 書籍一覧取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.BookResource` の getAllBooks() メソッド
  - **参照SPEC**: 
    - [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.1 書籍一覧取得」
    - [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「2. 書籍一覧取得」
  - **注意事項**: 
    - @Path("/books"), @GET を使用
    - BookService を注入
    - 認証不要

- [ ] **T_API002_005**: BookResource の作成（詳細取得機能）
  - **目的**: 書籍詳細取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.BookResource` の getBookById() メソッド
  - **参照SPEC**: 
    - [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.2 書籍詳細取得」
    - [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「3. 書籍詳細取得」
  - **注意事項**: 
    - @GET, @Path("/{id}") を使用
    - 書籍が見つからない場合は404 Not Found

- [ ] **T_API002_006**: BookResource の作成（検索機能）
  - **目的**: 書籍検索エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.BookResource` の searchBooks() メソッド
  - **参照SPEC**: 
    - [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.3 書籍検索」
    - [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「4. 書籍検索」
  - **注意事項**: 
    - @GET, @Path("/search") を使用
    - @QueryParam で categoryId と keyword を取得
    - categoryId=0 は全カテゴリを対象

- [ ] **T_API002_007**: BookResource の作成（カテゴリ一覧取得機能）
  - **目的**: カテゴリ一覧取得エンドポイントを実装する
  - **対象**: `pro.kensait.berrybooks.api.BookResource` の getCategories() メソッド
  - **参照SPEC**: 
    - [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 カテゴリ一覧取得」
    - [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) の「5. カテゴリ一覧取得」
  - **注意事項**: 
    - @GET, @Path("/categories") を使用
    - CategoryService を注入
    - Map<String, Integer> 形式で返す

---

### 3.4 ユニットテスト

- [ ] [P] **T_API002_008**: BookService のユニットテスト
  - **目的**: 書籍ビジネスロジックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.service.book.BookServiceTest`
  - **参照SPEC**: [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md)
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - BookDao をモック
    - 全書籍取得、ID検索、キーワード検索のテスト

- [ ] [P] **T_API002_009**: BookResource のユニットテスト
  - **目的**: 書籍APIエンドポイントのテストを実装する
  - **対象**: `pro.kensait.berrybooks.api.BookResourceTest`
  - **参照SPEC**: [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md)
  - **注意事項**: 
    - JUnit 5 + Mockito を使用
    - BookService をモック
    - 正常系、404エラーのテスト

---

### 3.5 APIテスト（E2E）

- [ ] **T_API002_010**: 書籍APIのE2Eテスト
  - **目的**: 書籍API全体のE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（JUnit 5またはPlaywright）
  - **参照SPEC**: [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md)
  - **注意事項**: 
    - 書籍一覧取得のテスト
    - 書籍詳細取得のテスト
    - 書籍検索（カテゴリ、キーワード、両方）のテスト
    - カテゴリ一覧取得のテスト

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] BookDao、BookService、CategoryServiceが実装されている
- [ ] BookResourceの全てのエンドポイント（一覧、詳細、検索、カテゴリ一覧）が実装されている
- [ ] N+1問題がJOIN FETCHで回避されている
- [ ] ユニットテストが実装され、カバレッジが80%以上である
- [ ] E2Eテストが実装され、主要フローが正常に動作する

---

## 参考資料

- [api/API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) - 書籍API機能設計書
- [api/API_002_books/behaviors.md](../specs/baseline/api/API_002_books/behaviors.md) - 書籍API受入基準
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
