# ユースケース: 画像配信

ユースケースID: images  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 顧客  
**I want to** 書籍の表紙画像を表示できる  
**So that** 書籍を視覚的に選べる

---

## 2. 受入基準

* AC1: GET /api/images/covers/{bookId} で書籍IDに対応する画像バイナリが返る（Content-Type: image/jpeg 等）
* AC2: 画像が存在しない場合は no-image.jpg を返す（または 404。プロジェクト方針に従う）
* AC3: 認証不要で利用可能
* AC4: WAR内リソース（ServletContext.getResourceAsStream）から配信し、パストラバーサル対策を行う

---

## 3. 概要

ImageResource が WAR 内の /resources/images/covers/ を参照して画像を配信。common/architecture_design の「静的リソース配信」に従う。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/images/covers/{bookId} | 書籍表紙画像（例: 1 → 1.jpg） |

* 画像が無い場合: no-image.jpg を返す等、architecture_design に記載のとおり

---

## 5. 参照

* [../../common/architecture_design.md](../../common/architecture_design.md) - 静的リソース配信・認証除外
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い
