# Struts to JSF マイグレーション憲章

バージョン: 1.0.0  
制定日: 2026-01-12  
最終更新日: 2026-01-12

## 概要

この憲章は、Apache Struts 1.xからJakarta Faces (JSF)へのマイグレーションを行うすべてのプロジェクトで共通的に適用される原則と手法を定義します。

* 対象移行元: Apache Struts 1.x（Struts 1.3.10等）
* 対象移行先: Jakarta Faces (JSF) 4.0 + Jakarta EE 10
* マイグレーションアプローチ: 仕様駆動マイグレーション（Spec-Driven Migration）

## マイグレーション哲学

### 哲学1: Code-to-Codeではなく、Code-to-Spec-to-Code

直接的なコード変換（Code-to-Code）ではなく、以下の3段階アプローチを採用します：

1. 既存コード分析（仕様書生成）: Strutsコードから抽象的・論理的な仕様書を生成
2. 仕様書の検証と調整: 仕様書をレビューし、JSFアーキテクチャに適応
3. 仕様駆動開発（コード生成）: 仕様書からJSFコードを生成

根拠: 直接的なコード変換は、レガシーな設計パターンや技術的負債を新システムに持ち込むリスクがあります。一度仕様書として抽象化することで、アーキテクチャを刷新し、品質の高いシステムを構築できます。

### 哲学2: アーキテクチャの刷新

Strutsの古いパターンをそのまま移植するのではなく、Jakarta EE 10の最新ベストプラクティスに従った設計に刷新します。

* Strutsの特徴:
  * ActionForm（POJO）
  * Action（コントローラー）
  * EJB 3.2（ステートレスセッションBean、JNDIルックアップ）
  * DAO（JDBC + DataSource）
  * JSPタグライブラリ（`<logic:iterate>`, `<bean:write>`, `<html:form>`等）

* JSFの特徴:
  * Managed Bean（`@Named`, `@ViewScoped`）
  * CDI（`@Inject`、依存性注入）
  * JPA（EntityManager、JPQL）
  * トランザクション（`@Transactional`）
  * Facelets XHTML（`<h:dataTable>`, `<h:outputText>`, `<h:form>`等）

根拠: 最新のアーキテクチャパターンを採用することで、保守性、テスタビリティ、拡張性を向上させます。

### 哲学3: ビジネスロジックの保全

UI技術やデータアクセス技術は変更しますが、ビジネスロジックとビジネスルールは可能な限り保全します。

根拠: ビジネスロジックは組織の資産であり、フレームワーク移行によって失われてはなりません。

### 哲学4: 段階的マイグレーション

大規模システムの場合、一度に全体をマイグレーションするのではなく、機能単位で段階的にマイグレーションします。

根拠: リスクを最小化し、各段階で検証とフィードバックを得ることができます。

## マイグレーション原則

### 原則1: 仕様書の完全性

既存コード分析で生成する仕様書は、以下を含む完全なものとします：

* requirements.md: システムの目的、機能要件、非機能要件
* architecture_design.md: 技術スタック、レイヤー構成、パッケージ構造
* functional_design.md: 画面一覧、画面遷移、コンポーネント設計
* data_model.md: エンティティ、テーブル定義、リレーション
* screen_design.md: 画面レイアウト、入力項目、ボタンアクション
* behaviors.md: 画面の振る舞い、バリデーション、エラーハンドリング

根拠: 完全な仕様書がなければ、仕様駆動開発で機能が欠落するリスクがあります。

### 原則2: マッピングの明示化

Strutsの構成要素とJSFの構成要素のマッピングを明示的に文書化します：

* Struts ActionForm → JSF Managed Beanのプロパティ
* Struts Action → JSF Managed Beanのアクションメソッド
* Struts JSPタグ → JSF Faceletsタグ
* EJB（JNDIルックアップ） → CDI（`@Inject`）
* DAO（JDBC） → JPA（EntityManager）

根拠: マッピングを明確にすることで、マイグレーションの一貫性と品質を保証します。

### 原則3: データモデルの継続性

データベーススキーマは基本的に変更しません。既存のテーブル構造をそのまま使用します。

根拠: データベーススキーマ変更は大きなリスクを伴います。マイグレーションの範囲をアプリケーション層に限定することで、リスクを最小化します。

### 原則4: テストによる検証

マイグレーション後のシステムが、元のシステムと同等の機能を持つことを、テストで検証します：

* 画面遷移テスト
* ビジネスロジックテスト
* データベースアクセステスト

根拠: テストによって、マイグレーションの正確性を保証します。

## マッピング規則

### Struts ActionForm → JSF Managed Bean

* Strutsの特徴:
  * ActionFormは`org.apache.struts.action.ActionForm`を継承
  * リクエストパラメータを文字列で保持
  * `reset()`メソッドでフォームをリセット

* JSFへの変換:
  * `@Named`と`@ViewScoped`を使用したManaged Bean
  * 適切な型（Integer, Date等）でプロパティを保持
  * `@PostConstruct`でライフサイクル管理

### Struts Action → JSF Managed Beanアクションメソッド

* Strutsの特徴:
  * Actionは`org.apache.struts.action.Action`を継承
  * `execute()`メソッドでリクエストを処理
  * `ActionForward`で遷移先を指定
  * JNDIルックアップでEJBを取得

* JSFへの変換:
  * Managed Beanのアクションメソッド（戻り値は遷移先画面ID）
  * `@Inject`でサービスを注入
  * `return "画面ID"`で遷移先を指定

### Struts JSPタグ → JSF Faceletsタグ

* Strutsタグ → JSFタグのマッピング:
  * `<logic:iterate>` → `<h:dataTable>` または `<ui:repeat>`
  * `<bean:write>` → `<h:outputText>`
  * `<html:form>` → `<h:form>`
  * `<html:text>` → `<h:inputText>`
  * `<html:submit>` → `<h:commandButton>`

### EJB（JNDIルックアップ） → CDI（依存性注入）

* Strutsの特徴:
  * `InitialContext.lookup()`でEJBを取得
  * JNDI名を文字列で指定

* JSFへの変換:
  * `@Inject`アノテーションで依存性注入
  * JNDI名の指定は不要

### DAO（JDBC） → JPA（EntityManager）

* Strutsの特徴:
  * DataSourceをJNDIルックアップ
  * `PreparedStatement`でSQL実行
  * 手動でResultSetをオブジェクトにマッピング

* JSFへの変換:
  * `@PersistenceContext`でEntityManagerを注入
  * JPQLでクエリを実行
  * エンティティが自動的にマッピング

## Markdownフォーマット規約

すべての生成される仕様書ドキュメントは以下のフォーマット規約に従います：

* 箇条書きはアスタリスク（`*`）を使用する
  * ハイフン（`-`）は使用しない
  * 一貫性を保つため、すべての箇条書きで統一する
* 箇条書きは必要に応じてネストする
  * 親項目が「項目名:」で終わる場合、その配下の項目は2スペースインデントする
  * ネストは意味的なグループ化を明確にする
* ボールド（太字）は使用しない
  * 見出しレベル（`#`、`##`、`###`等）で構造化する
  * 強調が必要な場合は、箇条書きや見出しで表現する
* コード例の前には箇条書き形式で説明を記載する
  * 例: `* Java:`の後にコードブロック

根拠: 統一されたフォーマットにより、可読性が向上し、ドキュメントの保守性が高まります。

## 参考資料

* [Jakarta EE 10仕様](https://jakarta.ee/specifications/platform/10/)
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Apache Struts 1.x Documentation](https://struts.apache.org/struts1eol-announcement.html)
