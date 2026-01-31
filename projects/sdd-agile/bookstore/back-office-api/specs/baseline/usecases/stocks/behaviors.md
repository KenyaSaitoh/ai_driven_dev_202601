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
  Given DB に在庫が存在する
  When GET /api/stocks を送る
  Then レスポンスは 200 OK
  And 在庫一覧が返る

Scenario: 在庫詳細を取得する
  Given DB に書籍ID=1 の在庫が存在する
  When GET /api/stocks/1 を送る
  Then レスポンスは 200 OK
  And quantity, version が含まれる
```

### Feature: 在庫更新（楽観的ロック）

```gherkin
Scenario: 在庫を更新する（version 一致）
  Given DB に書籍ID=1 の在庫が存在する（quantity=10, version=1）
  When PUT /api/stocks/1 に {"quantity":8,"version":1} を送る
  Then レスポンスは 200 OK
  And 在庫が 8 に更新される
  And version が 2 になる

Scenario: 在庫更新で楽観的ロック競合（version 不一致）
  Given DB に書籍ID=1 の在庫が存在する（version=2）。クライアントは version=1 を送信
  When PUT /api/stocks/1 に {"quantity":8,"version":1} を送る
  Then レスポンスは 409 Conflict
  And 在庫は更新されない
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
