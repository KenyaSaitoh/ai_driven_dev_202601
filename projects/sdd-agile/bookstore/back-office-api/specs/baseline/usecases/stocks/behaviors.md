# ユースケース: 在庫 - 振る舞い仕様書

ユースケースID: stocks  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

在庫一覧・取得・更新の振る舞い。楽観的ロックの競合シナリオを含む。

---

## 2. テストシナリオ（Gherkin）

### Feature: 在庫一覧・取得

```gherkin
Scenario: 在庫一覧を取得する
  Given DBに以下の在庫が存在する:
    テーブル: STOCK
    件数: 3件
    データセット: /datasets/stocks/initial-stocks.xml
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 10       | 1       |
      | 2       | 5        | 1       |
      | 3       | 8        | 2       |
  
  When GET /api/stocks を送る
  
  Then レスポンスは 200 OK
  And 在庫一覧が返る:
    件数: 3件
  
  And DBの状態は変化しない:
    テーブル: STOCK
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない

Scenario: 在庫詳細を取得する
  Given DBに書籍ID=1の在庫が存在する:
    テーブル: STOCK
    件数: 1件
    データセット: /datasets/stocks/initial-stock-detail.xml
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 10       | 1       |
  
  When GET /api/stocks/1 を送る
  
  Then レスポンスは 200 OK
  And quantity, version が含まれる:
    | bookId | quantity | version |
    | 1      | 10       | 1       |
  
  And DBの状態は変化しない:
    テーブル: STOCK
    件数: 1件（変更なし）
```

### Feature: 在庫更新（楽観的ロック）

```gherkin
Scenario: 在庫を更新する（version 一致）
  Given DBに書籍ID=1の在庫が存在する:
    テーブル: STOCK
    件数: 1件
    データセット: /datasets/stocks/initial-stock-before-update.xml
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 10       | 1       |
  
  When PUT /api/stocks/1 に {"quantity":8,"version":1} を送る
  
  Then レスポンスは 200 OK
  
  And DBの在庫テーブルは以下になる:
    テーブル: STOCK
    件数: 1件（変更なし）
    データセット: /datasets/stocks/expected-stock-updated.xml
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 8        | 2       |
    検証:
      - QUANTITY が 10 から 8 に更新される
      - VERSION が 1 から 2 にインクリメントされる
      - 楽観的ロックが正常に動作する

Scenario: 在庫更新で楽観的ロック競合（version 不一致）
  Given DBに書籍ID=1の在庫が存在する:
    テーブル: STOCK
    件数: 1件
    データセット: /datasets/stocks/initial-stock-version-conflict.xml
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 10       | 2       |
  
  When PUT /api/stocks/1 に {"quantity":8,"version":1} を送る（古いバージョン）
  
  Then レスポンスは 409 Conflict
  
  And DBの在庫テーブルは変化しない:
    テーブル: STOCK
    件数: 1件（変更なし）
    データ:
      | BOOK_ID | QUANTITY | VERSION |
      | 1       | 10       | 2       |
    検証:
      - 在庫は更新されない
      - VERSION は 2 のまま変化しない
      - 楽観的ロック競合が検知される
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 在庫一覧を取得する | `/datasets/stocks/initial-stocks.xml` | （変更なし） | STOCK |
| 在庫詳細を取得する | `/datasets/stocks/initial-stock-detail.xml` | （変更なし） | STOCK |
| 在庫を更新する（version一致） | `/datasets/stocks/initial-stock-before-update.xml` | `/datasets/stocks/expected-stock-updated.xml` | STOCK |
| 楽観的ロック競合（version不一致） | `/datasets/stocks/initial-stock-version-conflict.xml` | （変更なし） | STOCK |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
