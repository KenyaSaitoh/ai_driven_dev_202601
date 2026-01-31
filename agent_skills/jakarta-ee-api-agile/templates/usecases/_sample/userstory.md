# ユースケース: [USECASE_NAME]

ユースケースID: [USECASE_ID]  
バージョン: 1.0.0  
最終更新日: [DATE]  
ステータス: [STATUS]

---

## 1. ユーザーストーリー

As a [役割（例: 顧客、管理者）]  
I want to [やりたいこと]  
So that [その結果得たい価値]

---

## 2. 受入基準

* AC1: [受入基準1]
* AC2: [受入基準2]
* AC3: [受入基準3]

---

## 3. 概要

[このユースケースの概要を1〜2段落で記述。common の data_model / external_interface / architecture_design と矛盾しないようにする。]

---

## 4. スコープ

### 4.1 含まれるもの

* [このユースケースで実装する機能・API・画面等]
* [common で定義されたエンティティ・外部IFのうち、本ユースケースで使用するもの]

### 4.2 含まれないもの

* [意図的にスコープ外とする機能]

---

## 5. API仕様（該当する場合）

### 5.1 エンドポイント一覧

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/xxx | [説明] |
| POST | /api/xxx | [説明] |

### 5.2 リクエスト / レスポンス

[必要に応じてリクエスト・レスポンスの構造、バリデーション、エラーコードを記述。common の data_model / external_interface を参照。]

---

## 6. ビジネスルール

* BR1: [ビジネスルール1]
* BR2: [ビジネスルール2]

---

## 7. 参照

* [../common/data_model.md](../../common/data_model.md) - 共通データモデル
* [../common/external_interface.md](../../common/external_interface.md) - 共通外部IF
* [../common/architecture_design.md](../../common/architecture_design.md) - 共通アーキテクチャ
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い（受入基準・テストシナリオ）
