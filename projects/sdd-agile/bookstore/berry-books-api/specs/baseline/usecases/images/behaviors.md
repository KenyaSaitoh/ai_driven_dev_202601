# ユースケース: 画像 - 振る舞い仕様書

ユースケースID: images  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

画像配信の振る舞い。WAR内リソースから配信するため、結合テストでは ServletContext のモックまたは実際のリソース配置で検証。

---

## 2. テストシナリオ（Gherkin）

### Feature: 画像取得

```gherkin
Scenario: 書籍表紙画像を取得する
  Given WAR に /resources/images/covers/1.jpg が存在する
  When GET /api/images/covers/1 を送る
  Then レスポンスは 200 OK
  And Content-Type は image/jpeg
  And バイナリボディが返る

Scenario: 画像が存在しない場合は no-image を返す
  Given 指定された bookId に対応する画像が存在しない
  When GET /api/images/covers/999 を送る
  Then レスポンスは 200 OK（または 404。方針に従う）
  And no-image.jpg が返る、または 404 Not Found
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
