# 在庫API タスク

**担当者:** 担当者E（1名）  
**推奨スキル:** JAX-RS、JPA、楽観的ロック  
**想定工数:** 4時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API005_001: [P] StockTOの作成

- **目的**: 在庫情報のデータ転送オブジェクトを作成する
- **対象**: StockTO.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「6.1 StockTO」
- **注意事項**: 
  - bookId、quantity、versionを含める
  - レコード型（immutable）で実装する

---

### T_API005_002: [P] StockUpdateRequestの作成

- **目的**: 在庫更新リクエスト用のDTOクラスを作成する
- **対象**: StockUpdateRequest.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「6.2 StockUpdateRequest」
- **注意事項**: 
  - version（必須）、quantity（必須）を含める
  - Bean Validationアノテーションを付与する
  - レコード型（immutable）で実装する

---

### T_API005_003: [P] StockResourceの作成

- **目的**: 在庫APIのエンドポイントを実装するResourceクラスを作成する
- **対象**: StockResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「2. エンドポイント一覧」
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「3. エンドポイント詳細」
- **注意事項**: 
  - `@Path("/stocks")`でベースパスを設定する
  - getAllStocks()、getStockById()、updateStock()メソッドを実装する

---

### T_API005_004: getAllStocks()メソッドの実装

- **目的**: 在庫一覧取得機能を実装する
- **対象**: StockResource#getAllStocks()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「3.1 在庫一覧取得」
- **注意事項**: 
  - StockDaoのfindAll()を呼び出す
  - StockエンティティをStockTOに変換する
  - ログ出力（INFO）を行う

---

### T_API005_005: getStockById()メソッドの実装

- **目的**: 在庫情報取得機能を実装する
- **対象**: StockResource#getStockById()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「3.2 在庫情報取得」
- **注意事項**: 
  - パスパラメータから書籍IDを取得する
  - StockDaoのfindById()を呼び出す
  - 在庫が見つからない場合は404 Not Foundを返す
  - ログ出力（INFO、WARN）を行う

---

### T_API005_006: updateStock()メソッドの実装（楽観的ロック対応）

- **目的**: 在庫更新機能を実装する（楽観的ロック対応）
- **対象**: StockResource#updateStock()
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「3.3 在庫更新（楽観的ロック）」
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「4. 楽観的ロックの仕組み」
- **注意事項**: 
  - **重要**: バージョンチェックを実装する
  - パスパラメータから書籍IDを取得する
  - リクエストボディからversionとquantityを取得する
  - StockDaoのfindById()で在庫情報を取得する
  - リクエストのversionとDBのversionを比較する
  - バージョンが一致しない場合は409 Conflictを返す
  - 在庫数を更新する（stock.setQuantity()）
  - JPA `@Version`により自動的にversionがインクリメントされる
  - トランザクションコミット時にUPDATE実行される
  - ログ出力（INFO、WARN）を行う

---

### T_API005_007: [P] 在庫API単体テストの作成

- **目的**: 在庫APIの単体テストを作成する
- **対象**: StockResourceTest.java（テストクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「9. テスト仕様」
  - [functional_design.md](../specs/baseline/api/API_005_stocks/functional_design.md) の「11. 動的振る舞い」
- **注意事項**: 
  - 正常系: 在庫更新（正しいバージョン） → 200 OK + versionインクリメント
  - 異常系: 在庫更新（古いバージョン） → 409 Conflict
  - 異常系: 存在しない書籍ID → 404 Not Found
  - 並行更新シナリオのテスト（2ユーザーが同時に更新を試みる）

---

## API実装完了チェックリスト

- [ ] StockTOが作成された
- [ ] StockUpdateRequestが作成された
- [ ] StockResourceが作成された
- [ ] getAllStocks()メソッドが実装された
  - [ ] ログ出力が実装された
- [ ] getStockById()メソッドが実装された
  - [ ] 404エラーハンドリング
  - [ ] ログ出力が実装された
- [ ] updateStock()メソッドが実装された
  - [ ] **バージョンチェックが実装された**
  - [ ] 409 Conflictエラーハンドリング
  - [ ] 在庫数更新が実装された
  - [ ] ログ出力が実装された
- [ ] 在庫API単体テストが作成された
  - [ ] 正常系テストが実装された
  - [ ] 異常系テストが実装された
  - [ ] 並行更新シナリオテストが実装された

---

## 次のステップ

在庫APIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [認証API](API_001_auth.md)
- [書籍API](API_002_books.md)
- [カテゴリAPI](API_003_categories.md)
- [出版社API](API_004_publishers.md)
- [ワークフローAPI](API_006_workflows.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
