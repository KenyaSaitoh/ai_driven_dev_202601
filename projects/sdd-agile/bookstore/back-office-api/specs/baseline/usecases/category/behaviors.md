# ユースケース: カテゴリ - 振る舞い仕様書

ユースケースID: category  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

カテゴリ一覧取得の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: カテゴリ一覧

```gherkin
Scenario: カテゴリ一覧を取得する
  Given DB にカテゴリが存在する
  When GET /api/categories を送る
  Then レスポンスは 200 OK
  And カテゴリ一覧が返る
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
