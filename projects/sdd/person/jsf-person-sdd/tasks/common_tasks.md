# 共通機能タスク

担当者: 1名（共通機能チーム）
推奨スキル: JPA、CDI、Jakarta EE
想定工数: 4時間
依存タスク: [setup_tasks.md](setup_tasks.md)

## タスク一覧

### Entityクラス

* [ ] T_COMMON_001: Personエンティティクラスの作成
  * 目的: PERSONテーブルとマッピングするJPAエンティティを作成する
  * 対象: pro.kensait.jsf.person.entity.Person
  * 参照SPEC: 
    * [data_model.md](../specs/baseline/system/data_model.md) の「5.1 Personエンティティクラス」
    * [data_model.md](../specs/baseline/system/data_model.md) の「3.1 PERSONテーブル」
  * 注意事項: @Entity、@Table、@Id、@GeneratedValue、@Columnアノテーションを使用。GenerationType.IDENTITYで主キーを自動採番

### Serviceクラス

* [ ] T_COMMON_002: PersonServiceクラスの作成
  * 目的: PERSONのビジネスロジックを実装するServiceクラスを作成する
  * 対象: pro.kensait.jsf.person.service.PersonService
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.2 Business Logic Layer」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.1 依存関係図」
  * 注意事項: @RequestScoped、@Transactionalアノテーションを使用。EntityManagerを@PersistenceContextで注入

* [ ] T_COMMON_003: PersonService.getAllPersons() の実装
  * 目的: 全PERSON情報を取得するメソッドを実装する
  * 対象: PersonService.getAllPersons()
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「SCREEN_001_PersonList」「3.1 PersonService」
    * [data_model.md](../specs/baseline/system/data_model.md) の「6.1 全PERSON取得クエリ」
  * 注意事項: JPQL "SELECT p FROM Person p ORDER BY p.personId" を使用

* [ ] T_COMMON_004: PersonService.getPersonById() の実装
  * 目的: 指定されたIDのPERSONを取得するメソッドを実装する
  * 対象: PersonService.getPersonById(Integer personId)
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「SCREEN_002_PersonInput」「3.1 PersonService」
    * [data_model.md](../specs/baseline/system/data_model.md) の「6.2 IDでPERSON取得」
  * 注意事項: em.find(Person.class, personId)を使用。見つからない場合はnullを返す

* [ ] T_COMMON_005: PersonService.addPerson() の実装
  * 目的: 新しいPERSONを追加するメソッドを実装する
  * 対象: PersonService.addPerson(Person person)
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「SCREEN_003_PersonConfirm」「3.1 PersonService」
    * [data_model.md](../specs/baseline/system/data_model.md) の「6.3 PERSON追加」
  * 注意事項: em.persist(person)を使用。トランザクション境界は@Transactionalで自動管理

* [ ] T_COMMON_006: PersonService.updatePerson() の実装
  * 目的: 既存のPERSONを更新するメソッドを実装する
  * 対象: PersonService.updatePerson(Person person)
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「SCREEN_003_PersonConfirm」「3.1 PersonService」
    * [data_model.md](../specs/baseline/system/data_model.md) の「6.4 PERSON更新」
  * 注意事項: em.merge(person)を使用。トランザクション境界は@Transactionalで自動管理

* [ ] T_COMMON_007: PersonService.deletePerson() の実装
  * 目的: 指定されたPERSONを削除するメソッドを実装する
  * 対象: PersonService.deletePerson(Integer personId)
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「SCREEN_001_PersonList」「3.1 PersonService」
    * [data_model.md](../specs/baseline/system/data_model.md) の「6.5 PERSON削除」
  * 注意事項: em.find()で取得後、em.remove()で削除。見つからない場合はRuntimeExceptionをスロー

### 設定ファイル

* [ ] T_COMMON_008: persistence.xml の作成
  * 目的: JPA設定ファイルを作成し、Persistence Unitを定義する
  * 対象: src/main/resources/META-INF/persistence.xml
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.1 JPA設定（persistence.xml）」
    * [data_model.md](../specs/baseline/system/data_model.md) の「7.1 Persistence Unit定義」
  * 注意事項: persistence-unit名は"personPU"、transaction-type="JTA"、jta-data-source="java:app/jdbc/testdb"

### 単体テスト

* [ ] [P] T_COMMON_009: PersonServiceTest の作成
  * 目的: PersonServiceの単体テストを作成する
  * 対象: pro.kensait.jsf.person.service.PersonServiceTest
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.2 レイヤーの責務」
  * 注意事項: Arquillianを使用した統合テスト、またはMockitoを使用したモックテスト

## 並行実行の注意

* T_COMMON_001〜T_COMMON_008は順次実行
* T_COMMON_009（単体テスト）はT_COMMON_007完了後に並行実行可能

## 完了基準

* Personエンティティクラスが作成され、PERSONテーブルとマッピングされている
* PersonServiceクラスが作成され、すべてのCRUDメソッドが実装されている
* persistence.xmlが作成され、JPA設定が正しく定義されている
* PersonServiceの単体テストが作成され、すべてのメソッドがテストされている

## 参考資料

* [アーキテクチャ設計書](../specs/baseline/system/architecture_design.md)
* [機能設計書](../specs/baseline/system/functional_design.md)
* [データモデル](../specs/baseline/system/data_model.md)
* [Jakarta EE開発憲章](../../../agent_skills/jakarta-ee-standard/principles/constitution.md)
