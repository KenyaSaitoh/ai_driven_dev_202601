# 共通設計 - 詳細設計書

プロジェクトID: jsf-person  
分類: common（共通設計）  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

本ドキュメントは、共通設計の詳細設計を記述する。

* 対象: Personエンティティ、PersonDao等
* 目的: 基本設計とコードの橋渡しとなる設計判断を簡潔に記載

---

## 1. Personエンティティ

### 1.1 クラス設計

* クラス名: `Person`
* パッケージ: `pro.kensait.jsfperson.entity`
* 責務: PERSON テーブルのエンティティ表現

### 1.2 アノテーション

* `@Entity`
* `@Table(name = "PERSON")`

### 1.3 フィールド設計

* `personId`: Integer, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`, `@Column(name = "PERSON_ID")`
* `personName`: String, `@Column(name = "PERSON_NAME", length = 30, nullable = false)`
* `age`: Integer, `@Column(name = "AGE", nullable = false)`
* `gender`: String, `@Column(name = "GENDER", length = 10, nullable = false)`

### 1.4 メソッド

* getter/setterのみ（標準的な実装）

---

## 2. PersonDao

### 2.1 クラス設計

* クラス名: `PersonDao`
* パッケージ: `pro.kensait.jsfperson.dao`
* 責務: Personエンティティのデータアクセス
* スコープ: `@RequestScoped`

### 2.2 依存関係

* `EntityManager` (via `@PersistenceContext`)

### 2.3 主要メソッド

* `List<Person> findAll()` - JPQL: `SELECT p FROM Person p ORDER BY p.personId`
* `Person findById(Long personId)` - `em.find(Person.class, personId)`
* `void persist(Person person)` - `em.persist(person)`
* `Person merge(Person person)` - `return em.merge(person)`
* `void remove(Person person)` - `em.remove(person)`

---

## 3. 関連ドキュメント

* [基本設計 - データモデル](../../basic_design/common/data_model.md)
* [基本設計 - アーキテクチャ設計](../../basic_design/common/architecture_design.md)
* [単体テスト - 振る舞い仕様](./behaviors.md)
