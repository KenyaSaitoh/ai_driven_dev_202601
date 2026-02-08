# Person Management System - アーキテクチャ設計書

プロジェクトID: jsf-person
バージョン: 1.0.0
最終更新日: 2026-02-08
ステータス: 基本設計

* 変更履歴:
  * v1.0.0 (2026-02-08): 初版（Struts 1.3.10からの移行SPEC）

---

## 1. 概要

本文書は、Person Management Systemのアーキテクチャ設計を定義する。技術スタック、アーキテクチャパターン、レイヤー構成、パッケージ構造、トランザクション管理、セキュリティアーキテクチャを記述する。

* フレームワーク: Jakarta Faces (JSF) 4.0
* アプローチ: サーバーサイドMVC
* 設計原則: 画面中心設計、レイヤードアーキテクチャ
* 移行元: Apache Struts 1.3.10 + EJB 3.2 + JDBC

---

## 2. 技術スタック

### 2.1 コアプラットフォーム

| カテゴリ | 技術 | バージョン | 目的 |
|---------|-----|----------|------|
| ランタイム | Java | 21 | 実行環境 |
| プラットフォーム | Jakarta EE | 10 | フレームワーク基盤 |
| アプリケーションサーバー | Payara Server | 6.x | デプロイ環境 |
| データベース | HSQLDB | 2.7.x | データ永続化 |
| ビルドツール | Gradle | 8.x | ビルド自動化 |

### 2.2 フレームワーク仕様

| カテゴリ | 仕様 | バージョン | 目的 |
|---------|-----|----------|------|
| UI層 | Jakarta Faces (JSF) | 4.0 | サーバーサイドMVC、画面レンダリング |
| ビジネスロジック | Jakarta CDI | 4.0 | 依存性注入、ライフサイクル管理 |
| 永続化層 | Jakarta Persistence (JPA) | 3.1 | O/Rマッピング、エンティティ管理 |
| トランザクション | Jakarta Transactions | 2.0 | トランザクション管理 |
| バリデーション | Jakarta Bean Validation | 3.0 | 入力検証 |

### 2.3 追加ライブラリ

| ライブラリ | バージョン | 目的 |
|-----------|----------|------|
| PrimeFaces | 14.x | JSF UIコンポーネント拡張（該当する場合） |

---

## 3. アーキテクチャ設計

### 3.1 アーキテクチャパターン

* **レイヤードアーキテクチャ**
  * プレゼンテーション層（Managed Bean + XHTML）
  * ビジネスロジック層（Service）
  * データアクセス層（Dao）
  * 永続化層（Entity + JPA）

* **サーバーサイドMVC**
  * Model: Managed Bean（@Named, @ViewScoped）+ Entity（JPA）
  * View: Facelets XHTML
  * Controller: Managed Bean のアクションメソッド

### 3.2 コンポーネントの責務

#### 3.2.1 プレゼンテーション層

* **Managed Bean** (`@Named`, `@ViewScoped`)
  * 画面の状態管理（プロパティ）
  * ユーザー操作の処理（アクションメソッド）
  * Service層の呼び出し
  * 画面遷移の制御
  * バリデーションメッセージの表示

* **XHTML** (Facelets)
  * UI構造の定義（h:form, h:dataTable等）
  * Managed Beanとのデータバインディング（EL式: #{bean.property}）
  * イベントハンドリング（action="#{bean.method()}"）
  * バリデーションルールの宣言（Bean Validation）

#### 3.2.2 ビジネスロジック層

* **Service** (`@ApplicationScoped`)
  * ビジネスロジックの実装
  * トランザクション境界の管理（@Transactional）
  * Daoの協調制御

#### 3.2.3 データアクセス層

* **Dao** (`@ApplicationScoped`)
  * データベースCRUD操作
  * JPQLクエリの実行
  * EntityManagerの管理

#### 3.2.4 永続化層

* **Entity** (JPA)
  * データベーステーブルとのマッピング（@Entity, @Table）
  * リレーションシップの定義（@OneToMany, @ManyToOne等）
  * バリデーションルール（@NotNull, @Size等）

---

## 4. パッケージ構造と命名規則

### 4.1 パッケージ構造

```
pro.kensait.person
├── bean/                      # Managed Beans（画面単位）
│   ├── PersonListBean.java   # @Named, @ViewScoped
│   ├── PersonInputBean.java
│   └── PersonConfirmBean.java
├── service/                   # ビジネスロジック
│   └── PersonService.java    # @ApplicationScoped, @Transactional
├── dao/                       # データアクセス
│   └── PersonDao.java         # @ApplicationScoped
├── entity/                    # JPAエンティティ
│   └── Person.java            # @Entity
└── exception/                 # カスタム例外（該当する場合）
    └── PersonNotFoundException.java
```

### 4.2 命名規則

* **Managed Bean**: `[画面名]Bean` (例: PersonListBean, PersonInputBean)
* **Service**: `[エンティティ名]Service` (例: PersonService)
* **Dao**: `[エンティティ名]Dao` (例: PersonDao)
* **Entity**: `[エンティティ名]` (例: Person)
* **XHTML**: `[小文字画面名].xhtml` (例: personList.xhtml, personInput.xhtml)

---

## 5. トランザクション管理

### 5.1 トランザクション境界

* トランザクション境界: **Service層**
* アノテーション: `@Transactional`
* 伝播レベル: `REQUIRED` (デフォルト)
* 分離レベル: `READ_COMMITTED` (デフォルト)

### 5.2 トランザクション設計方針

* Managed Beanはトランザクション管理しない（@Transactionalを付与しない）
* Service層のメソッド単位でトランザクション境界を定義
* Dao層はトランザクションに参加（新規トランザクションを開始しない）
* 例外発生時は自動ロールバック

---

## 6. 並行制御

### 6.1 楽観的ロック

* 方式: JPA `@Version`
* 適用対象: 更新が必要なエンティティ（該当する場合）
* 例外: `OptimisticLockException`

注意: 現在のStrutsプロジェクトでは楽観的ロックは実装されていない。JSF移行時に必要に応じて追加を検討する。

---

## 7. エラーハンドリング戦略

### 7.1 例外処理

* ビジネス例外: カスタム例外（checked exception）
  * 例: `PersonNotFoundException`（該当する場合）
* システム例外: Runtime例外
  * 例: `IllegalStateException`, `NullPointerException`

### 7.2 エラー画面遷移

* Managed Beanで例外をキャッチ
* FacesMessage でエラーメッセージを表示
* 重大なエラーの場合はエラー画面にリダイレクト

---

## 8. セキュリティアーキテクチャ

### 8.1 認証・認可

* 認証方式: 認証なし（現在のStrutsプロジェクトでは認証機能なし）
* 認可方式: 認可なし
* 実装: セキュリティ機能なし

注意: 現在のStrutsプロジェクトではセキュリティ機能は実装されていない。JSF移行時に必要に応じて追加を検討する。

### 8.2 セキュリティ対策

* XSS対策: Faceletsの自動エスケープ機能（`h:outputText`）
* CSRF対策: JSFの`ViewState`トークン
* SQLインジェクション対策: JPQLパラメータバインディング

---

## 9. テスト戦略

### 9.1 テストレベル

| テストレベル | 対象 | ツール | カバレッジ目標 |
|------------|------|--------|--------------|
| 単体テスト | Service, Dao, Entity | JUnit 5 + Mockito | 80%以上 |
| 結合テスト | Service + Dao + Entity + DB | JUnit 5 + Weld SE + HSQLDB | 70%以上 |
| E2Eテスト | 全レイヤー（Managed Bean + Service + Dao + DB） | Playwright | 主要フロー100% |

### 9.2 テスト方針

* Managed Beanは単体テストカバレッジ対象外（E2Eテストで検証）
* Serviceは単体テストで検証（Daoをモック化）
* Daoは結合テストで検証（実際のDBアクセス）
* E2Eテストは画面フローを検証（実際のブラウザ操作）

---

## 10. 非機能要件

### 10.1 パフォーマンス

* 応答時間: 一般的なレスポンス時間（要件定義時に明確化）
* 大量データ対策: ページネーション（該当する場合）

### 10.2 可用性

* ログ出力: SLF4J + Log4j2（標準的なログ出力）
* 監視: 標準的なJVMモニタリング

---

## 11. データソース設定

### 11.1 JNDI設定

* JNDI名: `jdbc/HsqldbDS`（Strutsプロジェクトから継承）
* 完全修飾JNDI名: `java:comp/env/jdbc/HsqldbDS`
* データソース種別: HSQLDB
* 接続プール: Payara Serverのコネクションプール設定に従う

注意: Strutsプロジェクトで使用していたJNDI名をそのまま継続する。web.xmlのresource-refおよびPersonDao.javaで確認済み。

### 11.2 persistence.xml設定

```xml
<persistence-unit name="personPU" transaction-type="JTA">
    <jta-data-source>jdbc/HsqldbDS</jta-data-source>
    <properties>
        <property name="jakarta.persistence.schema-generation.database.action" value="none"/>
        <property name="eclipselink.logging.level" value="FINE"/>
        <property name="eclipselink.logging.parameters" value="true"/>
    </properties>
</persistence-unit>
```

* Persistence Unit名: `personPU`
* トランザクションタイプ: JTA
* JNDI名: `jdbc/HsqldbDS`
* スキーマ生成: none（既存テーブルを使用）

---

## 12. 参考資料

* [data_model.md](data_model.md) - データモデル仕様書
* [functional_design.md](functional_design.md) - 機能設計書
* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
