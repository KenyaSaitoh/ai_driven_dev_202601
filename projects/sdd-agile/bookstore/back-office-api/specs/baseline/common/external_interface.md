# back-office-api - 外部インターフェース仕様書（共通）

プロジェクトID: back-office-api  
バージョン: 2.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. 概要

本ドキュメントは、Books Stock API（back-office-api）が外部システムを呼び出す際のインターフェース仕様を定義する（共通SPEC）。

## 2. 外部システム連携状況

本システムは**外部システムを呼び出さない**独立したバックエンドサービスとして設計されている。

* データベース（HSQLDB）への接続のみ。外部API連携はなし。

---

## 3. 参考資料（アジャイル構成）

* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [data_model.md](data_model.md) - データモデル仕様書
* usecases/{名}/userstory.md - 各ユースケースのユーザーストーリー
