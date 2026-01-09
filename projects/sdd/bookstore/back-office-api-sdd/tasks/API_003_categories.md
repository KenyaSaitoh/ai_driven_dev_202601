# カテゴリAPI タスク

**担当者:** 担当者C（1名）  
**推奨スキル:** JAX-RS、JPA  
**想定工数:** 2時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API003_001: [P] CategoryTOの作成

- **目的**: カテゴリ情報のデータ転送オブジェクトを作成する
- **対象**: CategoryTO.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「4. データ転送オブジェクト（DTO）」
- **注意事項**: 
  - categoryId、categoryNameを含める
  - レコード型（immutable）で実装する

---

### T_API003_002: [P] CategoryResourceの作成

- **目的**: カテゴリAPIのエンドポイントを実装するResourceクラスを作成する
- **対象**: CategoryResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「2. エンドポイント一覧」
- **注意事項**: 
  - `@Path("/categories")`でベースパスを設定する
  - getAllCategories()メソッドを実装する

---

### T_API003_003: getAllCategories()メソッドの実装

- **目的**: カテゴリ一覧取得機能を実装する
- **対象**: CategoryResource#getAllCategories()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「3.1 カテゴリ一覧取得」
- **注意事項**: 
  - CategoryDaoのfindAll()を呼び出す
  - CategoryエンティティをCategoryTOに変換する
  - 配列形式でレスポンスを返す
  - ログ出力（INFO）を行う

---

### T_API003_004: [P] カテゴリAPI単体テストの作成

- **目的**: カテゴリAPIの単体テストを作成する
- **対象**: CategoryResourceTest.java（テストクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_003_categories/functional_design.md) の「6. テスト仕様」
- **注意事項**: 
  - 正常系: カテゴリ一覧取得 → 200 OK + カテゴリ配列

---

## API実装完了チェックリスト

- [ ] CategoryTOが作成された
- [ ] CategoryResourceが作成された
- [ ] getAllCategories()メソッドが実装された
  - [ ] 配列形式のレスポンス
  - [ ] ログ出力が実装された
- [ ] カテゴリAPI単体テストが作成された
  - [ ] 正常系テストが実装された

---

## 次のステップ

カテゴリAPIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [認証API](API_001_auth.md)
- [書籍API](API_002_books.md)
- [出版社API](API_004_publishers.md)
- [在庫API](API_005_stocks.md)
- [ワークフローAPI](API_006_workflows.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
