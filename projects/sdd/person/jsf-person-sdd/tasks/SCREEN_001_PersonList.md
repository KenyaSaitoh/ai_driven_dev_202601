# SCREEN_001_PersonList タスク

担当者: 1名（担当者A）
推奨スキル: JSF、Facelets XHTML、Managed Bean
想定工数: 3時間
依存タスク: [common_tasks.md](common_tasks.md)

## タスク一覧

### Managed Bean

* [ ] T_SCREEN001_001: PersonListBeanの作成
  * 目的: PERSON一覧画面のManaged Beanを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonListBean
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「2.1 PersonListBean」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.1 SCREEN_001_PersonList」
  * 注意事項: @Named("personListBean")、@ViewScoped、implements Serializableを使用。PersonServiceを@Injectで注入

* [ ] T_SCREEN001_002: PersonListBean.init() の実装
  * 目的: 画面初期表示時にPERSONリストを取得するメソッドを実装する
  * 対象: PersonListBean.init()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: @PostConstructアノテーションを使用。personService.getAllPersons()を呼び出してpersonListフィールドに設定

* [ ] T_SCREEN001_003: PersonListBean.deletePerson() の実装
  * 目的: 指定されたPERSONを削除するメソッドを実装する
  * 対象: PersonListBean.deletePerson(Integer personId)
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personService.deletePerson()を呼び出し、削除後にinit()を再実行してリストを更新。nullを返して同じページをリロード

### Facelets XHTML

* [ ] T_SCREEN001_004: personList.xhtmlの作成
  * 目的: PERSON一覧画面のXHTMLファイルを作成する
  * 対象: src/main/webapp/personList.xhtml
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「2. 画面レイアウト」
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: <h:head>、<h:body>、<h:form>、<h:dataTable>を使用。ボタンはJSFリソースハンドリングでCSSを読み込み

* [ ] T_SCREEN001_005: personList.xhtml - メッセージ表示の実装
  * 目的: エラーメッセージと成功メッセージを表示する
  * 対象: personList.xhtml - <h:messages>コンポーネント
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: globalOnly="false"、showSummary="true"、showDetail="false"を設定

* [ ] T_SCREEN001_006: personList.xhtml - PERSONリストテーブルの実装
  * 目的: PERSONリストを表形式で表示する
  * 対象: personList.xhtml - <h:dataTable>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「3. 表示項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personListBean.personList}"、var="person"を設定。カラムはID、名前、年齢、性別、操作

* [ ] T_SCREEN001_007: personList.xhtml - 新規追加ボタンの実装
  * 目的: PERSON入力画面に遷移する新規追加ボタンを実装する
  * 対象: personList.xhtml - <h:link>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「6.1 遷移先」
  * 注意事項: outcome="personInput"を設定。personIdパラメータは不要（新規追加モード）

* [ ] T_SCREEN001_008: personList.xhtml - 編集ボタンの実装
  * 目的: PERSON入力画面に遷移する編集ボタンを実装する
  * 対象: personList.xhtml - <h:link>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「6.1 遷移先」
  * 注意事項: outcome="personInput"、<f:param name="personId" value="#{person.personId}"/>でpersonIdをパラメータで渡す

* [ ] T_SCREEN001_009: personList.xhtml - 削除ボタンの実装
  * 目的: PERSONを削除する削除ボタンを実装する
  * 対象: personList.xhtml - <h:commandButton>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: action="#{personListBean.deletePerson(person.personId)}"、onclick="return confirm('削除してもよろしいですか？');"でJavaScript確認ダイアログ

* [ ] T_SCREEN001_010: personList.xhtml - 性別表示の実装
  * 目的: 性別（male/female）を日本語（男性/女性）で表示する
  * 対象: personList.xhtml - <h:outputText>コンポーネント
  * 参照SPEC: [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md) の「3. 表示項目」
  * 注意事項: rendered="#{person.gender == 'male'}"とrendered="#{person.gender == 'female'}"で条件分岐

### 単体テスト

* [ ] [P] T_SCREEN001_011: PersonListBeanTestの作成
  * 目的: PersonListBeanの単体テストを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonListBeanTest
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md)
  * 注意事項: MockitoでPersonServiceをモック化してテスト

* [ ] [P] T_SCREEN001_012: PersonListBeanTest - init()のテスト
  * 目的: init()メソッドが正しくPERSONリストを取得することをテストする
  * 対象: PersonListBeanTest.testInit()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personService.getAllPersons()がモックから返されることを確認

* [ ] [P] T_SCREEN001_013: PersonListBeanTest - deletePerson()のテスト
  * 目的: deletePerson()メソッドが正しくPERSONを削除することをテストする
  * 対象: PersonListBeanTest.testDeletePerson()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personService.deletePerson()が呼び出され、init()が再実行されることを確認

## 並行実行の注意

* T_SCREEN001_001〜T_SCREEN001_003は順次実行
* T_SCREEN001_004〜T_SCREEN001_010は順次実行
* T_SCREEN001_011〜T_SCREEN001_013（単体テスト）はT_SCREEN001_003完了後に並行実行可能

## 完了基準

* PersonListBeanが作成され、init()とdeletePerson()メソッドが実装されている
* personList.xhtmlが作成され、PERSONリストが表形式で表示される
* 新規追加ボタン、編集ボタン、削除ボタンが正しく動作する
* PersonListBeanの単体テストが作成され、すべてのメソッドがテストされている

## 参考資料

* [SCREEN_001_PersonList画面設計](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md)
* [SCREEN_001_PersonList機能設計](../specs/baseline/screen/SCREEN_001_PersonList/functional_design.md)
* [SCREEN_001_PersonList振る舞い仕様](../specs/baseline/screen/SCREEN_001_PersonList/behaviors.md)
* [システム機能設計書](../specs/baseline/system/functional_design.md)
