# 共通設計 - 機能設計書

プロジェクトID: jsf-person  
分類: common（共通設計）  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 1. 概要

本ドキュメントは、共通設計の機能を定義する。共通設計は、他のすべての画面グループから参照される基盤となる機能を提供する。

* エンティティ（Person等）
* Dao（PersonDao等）
* 共通Service（該当する場合）

---

## 2. エンティティ一覧

共通エンティティの定義については、`data_model.md` を参照。

---

## 3. Dao一覧

### 3.1 PersonDao

* 責務: Personエンティティのデータアクセス
* 提供メソッド:
  * `List<Person> findAll()` - 全件取得
  * `Person findById(Long personId)` - ID検索
  * `void persist(Person person)` - 新規登録
  * `Person merge(Person person)` - 更新
  * `void remove(Person person)` - 削除

---

## 4. 共通Service

現時点では共通Serviceは該当なし。

---

## 5. 関連ドキュメント

* [アーキテクチャ設計書](./architecture_design.md)
* [データモデル](./data_model.md)
* [外部インターフェース仕様書](./external_interface.md)
