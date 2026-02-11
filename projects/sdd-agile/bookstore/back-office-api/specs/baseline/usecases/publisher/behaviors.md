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
  Given DBに以下の出版社が存在する:
    テーブル: PUBLISHER
    件数: 3件
    データセット: /datasets/publisher/initial-publishers.xml
    データ:
      | PUBLISHER_ID | PUBLISHER_NAME | ADDRESS      |
      | 1            | 技術評論社     | 東京都新宿区 |
      | 2            | オライリー     | 東京都渋谷区 |
      | 3            | 翔泳社         | 東京都千代田区 |
  
  When GET /api/publishers を送る
  
  Then レスポンスは 200 OK
  And 出版社一覧が返る:
    件数: 3件
    データ:
      | publisherId | publisherName |
      | 1           | 技術評論社    |
      | 2           | オライリー    |
      | 3           | 翔泳社        |
  
  And DBの状態は変化しない:
    テーブル: PUBLISHER
    件数: 3件（変更なし）
    検証:
      - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 出版社一覧を取得する | `/datasets/publisher/initial-publishers.xml` | （変更なし） | PUBLISHER |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
