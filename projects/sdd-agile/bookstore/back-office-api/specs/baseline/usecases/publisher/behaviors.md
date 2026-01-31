# ユースケース: 出版社 - 振る舞い仕様書

ユースケースID: publisher  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

出版社一覧取得の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: 出版社一覧

```gherkin
Scenario: 出版社一覧を取得する
  Given DB に出版社が存在する
  When GET /api/publishers を送る
  Then レスポンスは 200 OK
  And 出版社一覧が返る
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
