# 要件定義書 - JSF Person（人材管理システム）

---

## 1. 概要

本ドキュメントは、人材管理システム（JSF Person）のシステム要件を定義する。

---

## 2. システムの目的

人材情報（PERSON）の登録、参照、更新、削除を行うWebアプリケーションシステム。従業員や関係者の基本情報を一元管理し、効率的な人材管理業務を支援することを目的とする。

---

## 3. 機能要件（EARS記法）

### 3.1 PERSON一覧表示機能

FR-LIST-001: WHEN ユーザーがpersonList.xhtmlにアクセスしたとき、システムはデータベースに登録されている全PERSON情報を一覧表示しなければならない。

FR-LIST-002: WHEN PERSON一覧が表示されるとき、システムはID、名前、年齢、性別の情報を表形式で表示しなければならない。

FR-LIST-003: WHEN PERSON一覧が表示されるとき、システムは各PERSONに対して編集ボタンと削除ボタンを提供しなければならない。

FR-LIST-004: WHEN PERSON一覧が表示されるとき、システムは新規追加機能へのリンクを提供しなければならない。

FR-LIST-005: WHEN PersonListBeanが初期化されるとき（@PostConstruct）、システムはPersonServiceのgetAllPersons()メソッドを自動的に呼び出さなければならない。

FR-LIST-006: WHEN getAllPersons()が呼び出されたとき、システムはJPQLクエリ "SELECT p FROM Person p ORDER BY p.personId" を実行しなければならない。

FR-LIST-007: WHERE PERSON一覧が0件の場合、システムは空のテーブルを表示しなければならない。

---

### 3.2 PERSON追加機能

#### 3.2.1 入力画面

FR-ADD-001: WHEN ユーザーが一覧画面の「新規追加」ボタンをクリックしたとき、システムはpersonInput.xhtmlに遷移しなければならない。

FR-ADD-002: WHEN personInput.xhtmlが表示されたとき、システムはPersonInputBeanのinit()メソッドを実行しなければならない。

FR-ADD-003: WHERE personIdがnullの場合（新規追加モード）、システムは入力フィールドを初期化しなければならない。

FR-ADD-004: WHEN 入力画面が表示されたとき、システムは名前、年齢、性別の入力項目を提供しなければならない。

FR-ADD-005: WHEN 性別入力項目が表示されたとき、システムはラジオボタンで「男性（male）」と「女性（female）」の選択肢を提供しなければならない。

#### 3.2.2 確認画面

FR-ADD-006: WHEN ユーザーが入力画面で「確認画面へ」ボタンをクリックしたとき、システムはPersonInputBeanのconfirm()メソッドを実行しなければならない。

FR-ADD-007: WHEN confirm()メソッドが実行されたとき、システムはBean Validationによる入力検証を実行しなければならない。

FR-ADD-008: WHERE 入力検証が成功した場合、システムは入力データをPersonInputBeanのフィールドに保持し、personConfirm.xhtmlに遷移しなければならない。

FR-ADD-009: WHEN 確認画面が表示されたとき、システムは入力された名前、年齢、性別を表示しなければならない。

FR-ADD-010: WHEN 性別が"male"の場合、システムは"男性"と表示しなければならない。

FR-ADD-011: WHEN 性別が"female"の場合、システムは"女性"と表示しなければならない。

#### 3.2.3 登録処理

FR-ADD-012: WHEN ユーザーが確認画面で「登録」ボタンをクリックしたとき、システムはPersonConfirmBeanのsave()メソッドを実行しなければならない。

FR-ADD-013: WHEN save()メソッドが実行されたとき、システムはPersonオブジェクトを作成し、入力データを設定しなければならない。

FR-ADD-014: WHEN Personオブジェクトが作成されたとき、システムはPersonServiceのaddPerson(person)メソッドを呼び出さなければならない。

FR-ADD-015: WHEN addPerson()が呼び出されたとき、システムはEntityManagerのpersist(person)メソッドでデータベースに登録しなければならない。

FR-ADD-016: WHEN persist()が実行されたとき、システムはPERSON_IDを自動採番しなければならない（@GeneratedValue(strategy = GenerationType.IDENTITY)）。

FR-ADD-017: WHEN 登録が成功したとき、システムはトランザクションをコミットし、personList.xhtmlにリダイレクトしなければならない。

FR-ADD-018: WHEN 一覧画面が再表示されたとき、システムは新しいPERSONを含む一覧を表示しなければならない。

---

### 3.3 PERSON編集機能

#### 3.3.1 編集画面表示

FR-EDIT-001: WHEN ユーザーが一覧画面の「編集」ボタンをクリックしたとき、システムはpersonInput.xhtml?personId=xxxに遷移しなければならない。

FR-EDIT-002: WHEN personInput.xhtmlがpersonIdパラメータ付きで表示されたとき、システムはPersonInputBeanのinit()メソッドを実行しなければならない。

FR-EDIT-003: WHEN init()メソッドがpersonIdを検出したとき、システムはPersonServiceのgetPersonById(personId)メソッドを呼び出さなければならない。

FR-EDIT-004: WHEN getPersonById()が呼び出されたとき、システムはEntityManagerのfind(Person.class, personId)で既存データを取得しなければならない。

FR-EDIT-005: WHEN 既存データが取得されたとき、システムは取得したPersonデータをPersonInputBeanのフィールドに設定しなければならない。

FR-EDIT-006: WHEN 入力フォームが表示されたとき、システムは既存データをプリセットしなければならない。

#### 3.3.2 編集内容の確認と更新

FR-EDIT-007: WHEN ユーザーが編集内容を入力し「確認画面へ」ボタンをクリックしたとき、システムはPersonInputBeanのconfirm()メソッドを実行し、Bean Validationによる検証を行わなければならない。

FR-EDIT-008: WHERE 検証が成功した場合、システムは編集データをPersonInputBeanのフィールドに保持し、personConfirm.xhtmlに遷移しなければならない。

FR-EDIT-009: WHEN ユーザーが確認画面で「登録」ボタンをクリックしたとき、システムはPersonConfirmBeanのsave()メソッドを実行しなければならない。

FR-EDIT-010: WHEN save()メソッドがpersonIdを含むPersonオブジェクトを受け取ったとき、システムはPersonServiceのupdatePerson(person)メソッドを呼び出さなければならない。

FR-EDIT-011: WHEN updatePerson()が呼び出されたとき、システムはEntityManagerのmerge(person)メソッドでデータベースを更新しなければならない。

FR-EDIT-012: WHEN 更新が成功したとき、システムはトランザクションをコミットし、personList.xhtmlにリダイレクトしなければならない。

FR-EDIT-013: WHEN 一覧画面が再表示されたとき、システムは更新されたPERSONを含む一覧を表示しなければならない。

---

### 3.4 PERSON削除機能

FR-DELETE-001: WHEN ユーザーが一覧画面の「削除」ボタンをクリックしたとき、システムはJavaScriptのconfirm()ダイアログで「削除してもよろしいですか？」と確認しなければならない。

FR-DELETE-002: WHERE ユーザーが「キャンセル」をクリックした場合、システムは削除処理を実行せず、一覧画面を維持しなければならない。

FR-DELETE-003: WHEN ユーザーが「OK」をクリックしたとき、システムはPersonListBeanのdeletePerson(personId)メソッドを実行しなければならない。

FR-DELETE-004: WHEN deletePerson()が呼び出されたとき、システムはPersonServiceのdeletePerson(personId)メソッドを呼び出さなければならない。

FR-DELETE-005: WHEN deletePerson()がサービス層で呼び出されたとき、システムはEntityManagerのfind(Person.class, personId)で削除対象を取得しなければならない。

FR-DELETE-006: WHEN 削除対象が取得されたとき、システムはEntityManagerのremove(person)メソッドで削除しなければならない。

FR-DELETE-007: WHEN 削除が成功したとき、システムはトランザクションをコミットしなければならない。

FR-DELETE-008: WHEN 削除処理が完了したとき、システムはPersonListBeanのinit()メソッドを再度呼び出し、リストを更新しなければならない。

FR-DELETE-009: WHEN 一覧画面が再表示されたとき、システムは削除後のPERSONリストを表示しなければならない。

---

### 3.5 キャンセル・戻る機能

#### 3.5.1 入力画面からキャンセル

FR-CANCEL-001: WHEN ユーザーが入力画面で「キャンセル」ボタンをクリックしたとき、システムはPersonInputBeanのcancel()メソッドを実行しなければならない。

FR-CANCEL-002: WHEN cancel()メソッドが実行されたとき、システムは入力データを破棄し、personList.xhtmlにリダイレクトしなければならない。

#### 3.5.2 確認画面から戻る

FR-BACK-001: WHEN ユーザーが確認画面で「戻る」ボタンをクリックしたとき、システムはPersonConfirmBeanのback()メソッドを実行しなければならない。

FR-BACK-002: WHEN back()メソッドが実行されたとき、システムはJavaScriptのhistory.back()を実行しなければならない。

FR-BACK-003: WHEN history.back()が実行されたとき、システムはブラウザ履歴を使用して入力画面に戻らなければならない。

FR-BACK-004: WHERE PersonInputBeanが@ViewScopedである場合、システムは入力データを保持しなければならない。

---

## 4. データ管理要件（EARS記法）

### 4.1 PERSONエンティティ

FR-DATA-001: システムは、PERSONエンティティに以下のフィールドを持たなければならない：
* PERSON_ID: 自動採番される一意識別子（主キー）
* PERSON_NAME: 名前（最大30文字、必須）
* AGE: 年齢（整数、必須）
* GENDER: 性別（male/female、必須）

FR-DATA-002: WHEN 新規PERSON作成時、システムはPERSON_IDを自動採番しなければならない。

FR-DATA-003: システムは、すべてのフィールドを必須項目として扱わなければならない。

FR-DATA-004: システムは、データベースレベルでNOT NULL制約を持たなければならない。

---

## 5. ビジネスルール（EARS記法）

### 5.1 PERSON追加時

BR-ADD-001: WHEN PERSON追加リクエストが実行されたとき、システムはPERSON_IDを自動採番しなければならない（ユーザーは入力しない）。

BR-ADD-002: WHEN PERSON追加リクエストが実行されたとき、システムはすべてのフィールドが入力されていることを確認しなければならない。

BR-ADD-003: WHEN 確認画面で内容を確認した後、システムはデータベースに登録しなければならない。

### 5.2 PERSON編集時

BR-EDIT-001: WHEN PERSON編集リクエストが実行されたとき、システムは既存のPERSON_IDを維持しなければならない。

BR-EDIT-002: WHEN PERSON編集リクエストが実行されたとき、システムはすべてのフィールドが入力されていることを確認しなければならない。

BR-EDIT-003: WHEN 確認画面で内容を確認した後、システムはデータベースを更新しなければならない。

### 5.3 PERSON削除時

BR-DELETE-001: WHEN PERSON削除リクエストが実行されたとき、システムはJavaScriptで削除確認ダイアログを表示しなければならない。

BR-DELETE-002: WHEN ユーザーが確認後、システムは指定されたPERSON_IDのレコードを削除しなければならない。

BR-DELETE-003: WHERE 削除処理である場合、システムは確認画面なしで即座に実行しなければならない。

### 5.4 画面遷移ルール

BR-NAV-001: WHEN 入力画面でキャンセルボタンがクリックされたとき、システムは一覧画面に戻らなければならない。

BR-NAV-002: WHEN 確認画面で戻るボタンがクリックされたとき、システムはブラウザ履歴を使用して入力画面に戻らなければならない。

BR-NAV-003: WHEN 登録・更新・削除が完了したとき、システムは一覧画面に遷移しなければならない。

---

## 6. バリデーション（EARS記法）

### 6.1 personName（名前）

VAL-NAME-001: システムは、personNameが空文字列またはnullの場合、"名前を入力してください"というエラーメッセージを表示しなければならない（@NotNull、@Size(min = 1)）。

VAL-NAME-002: IF personNameが30文字を超える場合、THEN システムは"名前は30文字以内で入力してください"というエラーメッセージを表示しなければならない（@Size(max = 30)）。

### 6.2 age（年齢）

VAL-AGE-001: IF ageがnullの場合、THEN システムは"年齢を入力してください"というエラーメッセージを表示しなければならない（@NotNull）。

VAL-AGE-002: IF ageが数値型（Integer）でない場合、THEN システムは"年齢は数値で入力してください"というエラーメッセージを表示しなければならない（JSFコンバーター）。

VAL-AGE-003: IF ageが0未満の場合、THEN システムは"年齢は0以上で入力してください"というエラーメッセージを表示しなければならない（@Min(0)）。

VAL-AGE-004: IF ageが150を超える場合、THEN システムは"年齢は150以下で入力してください"というエラーメッセージを表示しなければならない（@Max(150)）。

### 6.3 gender（性別）

VAL-GENDER-001: IF genderがnullの場合、THEN システムは"性別を選択してください"というエラーメッセージを表示しなければならない（@NotNull）。

VAL-GENDER-002: システムは、genderの値が"male"または"female"のみであることをUIレベル（ラジオボタン）で保証しなければならない。

### 6.4 バリデーション実行タイミング

VAL-TIMING-001: WHEN ユーザーが「確認画面へ」ボタンをクリックしたとき、システムはBean Validationを実行しなければならない。

VAL-TIMING-002: WHEN バリデーションエラーが検出されたとき、システムはPersonInputBeanのconfirm()メソッドを実行せず、入力画面を再表示しなければならない。

VAL-TIMING-003: WHEN 入力画面が再表示されたとき、システムは<h:messages>コンポーネントにエラーメッセージを表示しなければならない。

VAL-TIMING-004: WHEN エラーメッセージが表示されたとき、システムは入力データを保持しなければならない（再入力不要）。

---

## 7. 非機能要件（EARS記法）

### 7.1 技術スタック

NFR-TECH-001: システムは、Java 21を使用しなければならない。

NFR-TECH-002: システムは、Jakarta EE 10に準拠しなければならない。

NFR-TECH-003: システムは、Jakarta Faces (JSF) 4.0を使用しなければならない。

NFR-TECH-004: システムは、Jakarta Persistence (JPA) 3.1を使用しなければならない。

NFR-TECH-005: システムは、Jakarta CDI 4.0を使用しなければならない。

NFR-TECH-006: システムは、Jakarta Transactions (JTA) 2.0を使用しなければならない。

NFR-TECH-007: システムは、Payara Server 6.xをアプリケーションサーバーとして使用しなければならない。

NFR-TECH-008: システムは、HSQLDB 2.7.xをデータベースとして使用しなければならない。

NFR-TECH-009: システムは、Gradle 8.xをビルドツールとして使用しなければならない。

### 7.2 データベース要件

NFR-DB-001: システムは、HSQLDB 2.7.xを既存のデータベースとして継続使用しなければならない。

NFR-DB-002: システムは、JDBC（JPA経由）で接続しなければならない。

NFR-DB-003: システムは、データソース名java:app/jdbc/testdbを使用しなければならない。

NFR-DB-004: システムは、接続プール管理をPayara Serverで行わなければならない。

NFR-DB-005: システムは、既存のPERSONテーブル構造を維持しなければならない。

NFR-DB-006: システムは、テーブル名、カラム名を変更してはならない。

NFR-DB-007: システムは、スキーマ移行を行ってはならない。

### 7.3 トランザクション要件

NFR-TXN-001: システムは、JTA（Jakarta Transactions）による宣言的トランザクション管理を使用しなければならない。

NFR-TXN-002: システムは、Serviceクラスのメソッドレベルで@Transactionalアノテーションを使用しなければならない。

NFR-TXN-003: システムは、デフォルトのトランザクション境界をServiceクラスのパブリックメソッドに設定しなければならない。

NFR-TXN-004: システムは、トランザクション分離レベルをREAD_COMMITTED（HSQLDBデフォルト）に設定しなければならない。

NFR-TXN-005: IF RuntimeExceptionが発生した場合、THEN システムは自動的にトランザクションをロールバックしなければならない。

NFR-TXN-006: WHERE チェック例外が発生した場合、システムはロールバックしない（必要に応じて@Transactional設定で指定）。

### 7.4 認証・認可要件

NFR-AUTH-001: 本システムは認証機能を持たない。

NFR-AUTH-002: 本システムは認可機能を持たない。

NFR-AUTH-003: システムは、すべてのユーザーが全機能にアクセス可能としなければならない。

NFR-AUTH-004: WHERE 将来的にJakarta Security機能を追加する可能性がある場合、システムは拡張可能な設計としなければならない。

### 7.5 パフォーマンス要件

NFR-PERF-001: システムは、一覧表示のレスポンスタイムを1秒以内にしなければならない。

NFR-PERF-002: システムは、登録・更新・削除のレスポンスタイムを2秒以内にしなければならない。

NFR-PERF-003: システムは、想定同時接続数10ユーザーをサポートしなければならない。

NFR-PERF-004: WHERE 教育目的のシステムである場合、システムは大規模トラフィックを想定しない。

### 7.6 可用性要件

NFR-AVAIL-001: WHERE 教育目的のシステムである場合、システムは特定の稼働率目標を設定しない。

### 7.7 保守性・拡張性要件

NFR-MAINT-001: システムは、レイヤードアーキテクチャを採用しなければならない。

NFR-MAINT-002: システムは、Presentation Layer、Business Logic Layer、Data Access Layerの3層構造を実装しなければならない。

NFR-MAINT-003: システムは、各レイヤーで明確な責任分離を行わなければならない。

NFR-MAINT-004: システムは、CDI（@Inject）による依存性注入を使用しなければならない。

NFR-MAINT-005: システムは、テスタビリティを考慮した設計を実装しなければならない。

NFR-MAINT-006: システムは、Jakarta EE 10の標準的なベストプラクティスに従わなければならない。

NFR-MAINT-007: システムは、クラス名、メソッド名に明確で理解しやすい命名を行わなければならない。

### 7.8 国際化要件

NFR-I18N-001: システムは、UTF-8を文字コードとして使用しなければならない。

NFR-I18N-002: システムは、日本語のみをサポートしなければならない。

NFR-I18N-003: WHERE 将来的に多言語対応の可能性がある場合、システムは拡張可能な設計としなければならない。

### 7.9 エラーハンドリング要件

NFR-ERR-001: WHEN ビジネスロジック層で例外が発生したとき、システムはRuntimeExceptionでラップしなければならない。

NFR-ERR-002: WHEN データベース例外が発生したとき、システムはRuntimeExceptionとして上位層に伝播しなければならない。

NFR-ERR-003: WHEN プレゼンテーション層でエラーが発生したとき、システムはユーザーフレンドリーなエラーメッセージを表示しなければならない。

NFR-ERR-004: WHEN 重要な処理（登録・更新・削除）が実行されたとき、システムはログを出力しなければならない。

NFR-ERR-005: WHEN 例外が発生したとき、システムはスタックトレースをログに記録しなければならない。

---

## 8. 制約事項

### 8.1 技術的制約

CONST-TECH-001: システムは、データベーススキーマを変更してはならない。

CONST-TECH-002: システムは、既存のPERSONテーブル構造を維持しなければならない。

CONST-TECH-003: システムは、マイグレーション範囲をアプリケーション層のみとしなければならない。

CONST-TECH-004: システムは、既存のPERSONデータを引き続き使用しなければならない。

CONST-TECH-005: システムは、データ移行を行ってはならない。

### 8.2 スコープ外

OUT-OF-SCOPE-001: 認証・認可機能は本リリースではスコープ外とする（将来的に追加の可能性がある）。

OUT-OF-SCOPE-002: 検索・フィルタリング機能は本リリースではスコープ外とする（全データを一覧表示のみ）。

OUT-OF-SCOPE-003: CSV出力・インポート機能は本リリースではスコープ外とする。

OUT-OF-SCOPE-004: ページネーション機能は本リリースではスコープ外とする（全データを1ページに表示）。

---

## 9. 受入基準

### 9.1 特殊な振る舞い

#### 9.1.1 自動採番（PERSON_ID）

AC-AUTO-001: WHEN 新規追加時、ユーザーはPERSON_IDを入力してはならない。

AC-AUTO-002: WHEN PersonオブジェクトのpersonIdフィールドがnullのとき、システムはEntityManagerのpersist()を実行し、HSQLDBが自動的にIDを生成しなければならない。

AC-AUTO-003: WHEN トランザクションコミット後、システムはPersonオブジェクトに生成されたIDを設定しなければならない。

#### 9.1.2 性別の表示変換

AC-GENDER-001: WHEN データベース値が"male"のとき、システムは画面に"男性"と表示しなければならない。

AC-GENDER-002: WHEN データベース値が"female"のとき、システムは画面に"女性"と表示しなければならない。

#### 9.1.3 削除確認ダイアログ

AC-DELETE-001: WHEN 削除ボタンがクリックされたとき、システムはJavaScriptのconfirm()ダイアログを表示しなければならない。

AC-DELETE-002: WHEN ユーザーが「OK」をクリックしたとき、システムは削除処理を実行しなければならない。

AC-DELETE-003: WHEN ユーザーが「キャンセル」をクリックしたとき、システムは削除処理を実行せず、一覧画面を維持しなければならない。

#### 9.1.4 画面間データ引き継ぎ

AC-DATA-001: WHEN 入力画面から確認画面に遷移するとき、システムは@ViewScopedのBeanがデータを保持しなければならない。

AC-DATA-002: WHEN PersonInputBeanのフィールドが設定されたとき、システムはPersonConfirmBeanに引き継がれなければならない。

AC-DATA-003: WHEN 確認画面から一覧画面にリダイレクトされたとき、システムはデータ引き継ぎを行わず、データベースから再取得しなければならない。

### 9.2 並行アクセス

AC-CONC-001: 現在のバージョンでは楽観的ロックを実装しない。

AC-CONC-002: 現在のバージョンでは悲観的ロックを実装しない。

AC-CONC-003: WHEN 同じPERSONを複数ユーザーが編集した場合、システムは後勝ち（Last Write Wins）動作を許容しなければならない。

AC-CONC-004: WHERE 将来的に楽観的ロックで対応する可能性がある場合、システムは拡張可能な設計としなければならない。

### 9.3 セッション管理

AC-SESSION-001: システムは、PersonListBean、PersonInputBean、PersonConfirmBeanに@ViewScopedを使用しなければならない。

AC-SESSION-002: システムは、PersonServiceに@RequestScopedを使用しなければならない。

AC-SESSION-003: システムは、デフォルトのセッションタイムアウトを30分に設定しなければならない。

AC-SESSION-004: WHEN セッションタイムアウトが発生したとき、システムは@ViewScopedのBeanを破棄しなければならない。

AC-SESSION-005: IF ユーザーがタイムアウト後に操作を続けた場合、THEN システムはViewExpiredExceptionを発生させ、エラーページに遷移しなければならない。

### 9.4 ログ出力

AC-LOG-001: WHEN addPerson()、updatePerson()、deletePerson()が実行されたとき、システムはINFOレベルでログを出力しなければならない。

AC-LOG-002: WHEN 例外が発生したとき、システムはSEVEREレベルでログを出力しなければならない。

AC-LOG-003: WHEN 例外が発生したとき、システムはスタックトレースを出力しなければならない。

### 9.5 パフォーマンス考慮事項

AC-PERF-001: WHEN 一覧表示が実行されたとき、システムは全PERSONをORDER BY PERSON_IDで取得しなければならない。

AC-PERF-002: 現状はページネーションを実装しない（将来的に追加の可能性）。

AC-PERF-003: WHEN 編集モードが実行されたとき、システムはfindById()で1件のみ取得しなければならない。

AC-PERF-004: システムは、トランザクション境界をServiceクラスのメソッドに設定しなければならない。

AC-PERF-005: システムは、短いトランザクション（CRUD操作のみ）を実装しなければならない。

AC-PERF-006: システムは、長時間のトランザクションを避けなければならない。

---

## 10. 参考資料

* [アーキテクチャ設計書](../basic_design/common/architecture_design.md) - システムアーキテクチャの詳細
* [データモデル](../basic_design/common/data_model.md) - データベーススキーマの詳細
* [機能設計書](../basic_design/person_management/functional_design.md) - 画面遷移とコンポーネント設計
