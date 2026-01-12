# SCREEN_003_PersonConfirm タスク

担当者: 1名（担当者C）
推奨スキル: JSF、Managed Bean、CDI
想定工数: 3時間
依存タスク: [common_tasks.md](common_tasks.md)

## タスク一覧

### Managed Bean

* [X] T_SCREEN003_001: PersonConfirmBeanの作成
  * 目的: PERSON確認画面のManaged Beanを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonConfirmBean
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1 PersonConfirmBean」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.3 SCREEN_003_PersonConfirm」
  * 注意事項: @Named("personConfirmBean")、@ViewScoped、implements Serializableを使用。PersonServiceとPersonInputBeanを@Injectで注入

* [X] T_SCREEN003_002: PersonConfirmBean - フィールドの定義
  * 目的: PERSON確認項目のフィールドを定義する
  * 対象: PersonConfirmBean - personId、personName、age、gender
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.1 フィールド」
  * 注意事項: PersonInputBeanから取得したデータを保持するフィールド

* [X] T_SCREEN003_003: PersonConfirmBean.init() の実装
  * 目的: 画面初期表示時にPersonInputBeanから入力データを取得する
  * 対象: PersonConfirmBean.init()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: @PostConstructアノテーションを使用。personInputBeanから各フィールドの値を取得

* [X] T_SCREEN003_004: PersonConfirmBean.save() の実装
  * 目的: PERSON情報をデータベースに登録または更新するメソッドを実装する
  * 対象: PersonConfirmBean.save()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personIdがnullの場合はpersonService.addPerson()、null以外の場合はpersonService.updatePerson()を呼び出す。成功後は"personList?faces-redirect=true"を返す

### Facelets XHTML

* [X] T_SCREEN003_005: personConfirm.xhtmlの作成
  * 目的: PERSON確認画面のXHTMLファイルを作成する
  * 対象: src/main/webapp/personConfirm.xhtml
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「2. 画面レイアウト」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: <h:head>、<h:body>、<h:form>を使用

* [X] T_SCREEN003_006: personConfirm.xhtml - メッセージ表示の実装
  * 目的: エラーメッセージを表示する
  * 対象: personConfirm.xhtml - <h:messages>コンポーネント
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: globalOnly="false"、showSummary="true"、showDetail="false"を設定

* [X] T_SCREEN003_007: personConfirm.xhtml - 名前表示の実装
  * 目的: 名前を確認表示する
  * 対象: personConfirm.xhtml - <h:outputText>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「3. 表示項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personConfirmBean.personName}"を設定

* [X] T_SCREEN003_008: personConfirm.xhtml - 年齢表示の実装
  * 目的: 年齢を確認表示する
  * 対象: personConfirm.xhtml - <h:outputText>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「3. 表示項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personConfirmBean.age}"を設定

* [X] T_SCREEN003_009: personConfirm.xhtml - 性別表示の実装
  * 目的: 性別（male/female）を日本語（男性/女性）で表示する
  * 対象: personConfirm.xhtml - <h:outputText>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「3. 表示項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: rendered="#{personConfirmBean.gender == 'male'}"とrendered="#{personConfirmBean.gender == 'female'}"で条件分岐

* [X] T_SCREEN003_010: personConfirm.xhtml - hiddenフィールドの実装
  * 目的: personId、personName、age、genderをhiddenフィールドで保持する
  * 対象: personConfirm.xhtml - <h:inputHidden>コンポーネント
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: 登録ボタンクリック時にフォームデータとして送信される

* [X] T_SCREEN003_011: personConfirm.xhtml - 登録ボタンの実装
  * 目的: PERSON情報を登録・更新する登録ボタンを実装する
  * 対象: personConfirm.xhtml - <h:commandButton>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「6.1 遷移先」
  * 注意事項: action="#{personConfirmBean.save}"を設定。登録後は一覧画面にリダイレクト

* [X] T_SCREEN003_012: personConfirm.xhtml - 戻るボタンの実装
  * 目的: 入力画面に戻る戻るボタンを実装する
  * 対象: personConfirm.xhtml - <h:button>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「6.2 遷移方式」
  * 注意事項: onclick="history.back(); return false;"でJavaScriptのhistory.back()を使用

### 単体テスト

* [ ] [P] T_SCREEN003_013: PersonConfirmBeanTestの作成
  * 目的: PersonConfirmBeanの単体テストを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonConfirmBeanTest
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md)
  * 注意事項: MockitoでPersonServiceとPersonInputBeanをモック化してテスト

* [ ] [P] T_SCREEN003_014: PersonConfirmBeanTest - init()のテスト
  * 目的: init()メソッドが正しくPersonInputBeanからデータを取得することをテストする
  * 対象: PersonConfirmBeanTest.testInit()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personInputBeanのgetterメソッドが呼び出され、フィールドに値が設定されることを確認

* [ ] [P] T_SCREEN003_015: PersonConfirmBeanTest - save()のテスト（新規追加）
  * 目的: save()メソッドが新規追加モードで正しく動作することをテストする
  * 対象: PersonConfirmBeanTest.testSaveNewMode()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personIdがnullの場合、personService.addPerson()が呼び出されることを確認

* [ ] [P] T_SCREEN003_016: PersonConfirmBeanTest - save()のテスト（更新）
  * 目的: save()メソッドが更新モードで正しく動作することをテストする
  * 対象: PersonConfirmBeanTest.testSaveUpdateMode()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personIdがnull以外の場合、personService.updatePerson()が呼び出されることを確認

* [ ] [P] T_SCREEN003_017: PersonConfirmBeanTest - save()のエラーハンドリングテスト
  * 目的: save()メソッドで例外が発生した場合のエラーハンドリングをテストする
  * 対象: PersonConfirmBeanTest.testSaveError()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: RuntimeExceptionが発生した場合、nullが返されることを確認

## 並行実行の注意

* T_SCREEN003_001〜T_SCREEN003_004は順次実行
* T_SCREEN003_005〜T_SCREEN003_012は順次実行
* T_SCREEN003_013〜T_SCREEN003_017（単体テスト）はT_SCREEN003_004完了後に並行実行可能

## 完了基準

* PersonConfirmBeanが作成され、init()とsave()メソッドが実装されている
* personConfirm.xhtmlが作成され、名前、年齢、性別が確認表示される
* 登録ボタンと戻るボタンが正しく動作する
* PersonConfirmBeanの単体テストが作成され、すべてのメソッドがテストされている

## 参考資料

* [SCREEN_003_PersonConfirm画面設計](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md)
* [SCREEN_003_PersonConfirm機能設計](../specs/baseline/screen/SCREEN_003_PersonConfirm/functional_design.md)
* [SCREEN_003_PersonConfirm振る舞い仕様](../specs/baseline/screen/SCREEN_003_PersonConfirm/behaviors.md)
* [システム機能設計書](../specs/baseline/system/functional_design.md)
