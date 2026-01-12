# SCREEN_003_PersonConfirm PERSON確認画面 - 詳細設計書

* 画面ID: SCREEN_003_PersonConfirm
* 画面名: PERSON確認画面
* バージョン: 1.0.0
* 最終更新: 2026-01-12

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.jsf.person
├── bean
│   ├── PersonConfirmBean.java       # JSF Managed Bean（本画面）
│   └── PersonInputBean.java         # JSF Managed Bean（入力画面、依存）
├── entity
│   └── Person.java                  # JPA Entity
└── service
    └── PersonService.java           # CDI Service
```

---

## 2. クラス設計

### 2.1 PersonConfirmBean（JSF Managed Bean）

* 責務: PERSON確認画面の表示制御とデータ登録・更新処理

* アノテーション:
  * `@Named("personConfirmBean")` - JSF Managed Bean名
  * `@ViewScoped` - ビュースコープ（画面表示中のみ有効）
  * `implements Serializable` - シリアライズ可能

* プロパティ:

| プロパティ名 | 型 | 説明 |
|------------|---|------|
| `personService` | `PersonService` | ビジネスロジック層のサービス（@Inject） |
| `personInputBean` | `PersonInputBean` | 入力画面のBean（@Inject、データ取得用） |
| `personId` | `Integer` | PERSON_ID（編集モードの場合に設定、新規の場合はnull） |
| `personName` | `String` | 名前 |
| `age` | `Integer` | 年齢 |
| `gender` | `String` | 性別（"male" または "female"） |

* アクションメソッド:

#### init() - 画面初期化

* シグネチャ:

```java
@PostConstruct
public void init()
```

* 処理フロー:
  1. PersonInputBeanから入力データを取得
  2. personId、personName、age、genderフィールドに設定
  3. 画面に確認情報を表示

* エラーケース:
  * PersonInputBeanがnullの場合 → 通常は発生しない（CDIによる自動注入）

#### save() - データ登録・更新

* シグネチャ:

```java
public String save()
```

* 処理フロー:
  1. Personエンティティオブジェクトを作成
  2. フィールドの値をPersonオブジェクトに設定（personId、personName、age、gender）
  3. personIdがnullかどうかで処理を分岐:
     * null → personService.addPerson(person)を呼び出し（新規追加）
     * null以外 → personService.updatePerson(person)を呼び出し（更新）
  4. トランザクションがコミットされ、INSERT/UPDATEが実行される
  5. 一覧画面（personList.xhtml）にリダイレクト

* 戻り値: `"personList?faces-redirect=true"` - 一覧画面へリダイレクト遷移

* エラーケース:
  * データベースエラー → RuntimeExceptionをキャッチし、エラーメッセージを表示、確認画面を再表示（return null）
  * 制約違反エラー → RuntimeExceptionをキャッチし、エラーメッセージを表示、確認画面を再表示（return null）

* 実装例:

```java
public String save() {
    try {
        Person person = new Person();
        person.setPersonId(personId);
        person.setPersonName(personName);
        person.setAge(age);
        person.setGender(gender);
        
        if (personId == null) {
            personService.addPerson(person);
        } else {
            personService.updatePerson(person);
        }
        
        return "personList?faces-redirect=true";
    } catch (RuntimeException e) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "登録処理に失敗しました: " + e.getMessage()));
        return null;
    }
}
```

---

### 2.2 PersonService（ビジネスロジック層）

* 責務: PERSONエンティティの永続化処理（追加・更新）

* アノテーション:
  * `@RequestScoped` - リクエストスコープ
  * `@Transactional(TxType.REQUIRED)` - トランザクション管理

* 主要メソッド:

#### addPerson(Person person)

* シグネチャ:

```java
public void addPerson(Person person)
```

* 処理:
  1. EntityManager.persist(person)を呼び出す
  2. Personエンティティを永続化コンテキストに追加
  3. トランザクションコミット時にINSERT文が実行される
  4. データベースがPERSON_IDを自動採番

* JPQL:

```java
em.persist(person);
```

#### updatePerson(Person person)

* シグネチャ:

```java
public void updatePerson(Person person)
```

* 処理:
  1. EntityManager.merge(person)を呼び出す
  2. Personエンティティを更新
  3. トランザクションコミット時にUPDATE文が実行される

* JPQL:

```java
em.merge(person);
```

---

### 2.3 Person（エンティティ）

* テーブル: `PERSON`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `personId` | `Integer` | `PERSON_ID` | `PRIMARY KEY, AUTO INCREMENT` | 人材ID（主キー、自動採番） |
| `personName` | `String` | `PERSON_NAME` | `NOT NULL, VARCHAR(30)` | 名前 |
| `age` | `Integer` | `AGE` | `NOT NULL` | 年齢 |
| `gender` | `String` | `GENDER` | `NOT NULL, VARCHAR(10)` | 性別（"male"/"female"） |

* アノテーション:

```java
@Entity
@Table(name = "PERSON")
```

* リレーション:
  * なし（単独テーブル）

* Bean Validation:
  * `@NotNull` - personName、age、gender
  * `@Size(max=30)` - personName
  * `@Size(max=10)` - gender
  * `@Min(0)` - age（オプション）

---

## 3. 画面設計

### 3.1 Facelets XHTML構造

* ファイル名: `personConfirm.xhtml`
* 配置場所: `src/main/webapp/person/personConfirm.xhtml`

* 主要コンポーネント:
  * `<h:head>` - ページヘッダー
  * `<h:body>` - ページボディ
  * `<h:messages>` - エラーメッセージ表示
  * `<h:outputText>` - 確認情報表示
  * `<h:form>` - フォーム
  * `<h:inputHidden>` - hiddenフィールド
  * `<h:commandButton>` - 登録ボタン
  * `<h:button>` - 戻るボタン

* レイアウト:

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON確認</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>
<h:body>
    <h1>PERSON確認</h1>
    
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <!-- 確認情報表示 -->
    <div class="info-group">
        <label>名前:</label>
        <h:outputText value="#{personConfirmBean.personName}"/>
    </div>
    
    <div class="info-group">
        <label>年齢:</label>
        <h:outputText value="#{personConfirmBean.age}"/>
    </div>
    
    <div class="info-group">
        <label>性別:</label>
        <h:outputText value="男性" rendered="#{personConfirmBean.gender == 'male'}"/>
        <h:outputText value="女性" rendered="#{personConfirmBean.gender == 'female'}"/>
    </div>
    
    <!-- フォーム -->
    <h:form>
        <h:inputHidden value="#{personConfirmBean.personId}"/>
        <h:inputHidden value="#{personConfirmBean.personName}"/>
        <h:inputHidden value="#{personConfirmBean.age}"/>
        <h:inputHidden value="#{personConfirmBean.gender}"/>
        
        <div class="button-group">
            <h:commandButton value="登録" action="#{personConfirmBean.save}" 
                             styleClass="button"/>
            <h:button value="戻る" onclick="history.back(); return false;" 
                      styleClass="button back"/>
        </div>
    </h:form>
</h:body>
</html>
```

---

## 4. 画面遷移

### 4.1 遷移パターン

| 遷移元 | 遷移先 | 条件 | データ受け渡し |
|-------|-------|-----|--------------|
| personInput.xhtml | personConfirm.xhtml | 確認ボタンクリック | PersonInputBean（@ViewScoped） |
| personConfirm.xhtml | personList.xhtml | 登録ボタンクリック | リダイレクト遷移 |
| personConfirm.xhtml | personInput.xhtml | 戻るボタンクリック | JavaScriptのhistory.back() |

### 4.2 データ受け渡し方法

* 入力画面から確認画面へ:
  * PersonInputBeanがPersonConfirmBeanに注入される（@Inject）
  * PersonConfirmBean.init()メソッドでPersonInputBeanからデータを取得

```java
@Inject
private PersonInputBean personInputBean;

@PostConstruct
public void init() {
    this.personId = personInputBean.getPersonId();
    this.personName = personInputBean.getPersonName();
    this.age = personInputBean.getAge();
    this.gender = personInputBean.getGender();
}
```

* 確認画面から一覧画面へ:
  * リダイレクト遷移（`?faces-redirect=true`）
  * データ受け渡しなし（一覧画面が自らデータを取得）

---

## 5. バリデーション

### 5.1 Bean Validation

* 本画面ではバリデーションなし（入力画面で既に実行済み）
* hiddenフィールドの値は入力画面でバリデーション済みのため、再度の検証は不要

---

## 6. エラーハンドリング

### 6.1 エラーシナリオ

| エラーケース | 処理 | メッセージ |
|------------|-----|----------|
| データベース接続エラー | RuntimeExceptionをキャッチ、確認画面を再表示 | "登録処理に失敗しました: [エラー詳細]" |
| 制約違反エラー | RuntimeExceptionをキャッチ、確認画面を再表示 | "登録処理に失敗しました: [制約違反詳細]" |
| トランザクションエラー | 自動ロールバック、エラーメッセージ表示 | "登録処理に失敗しました: [エラー詳細]" |

### 6.2 エラーメッセージ表示

* Faceletsでの表示:

```xhtml
<h:messages globalOnly="false" showSummary="true" showDetail="false" 
            styleClass="error-messages"/>
```

* Managed Beanからのメッセージ追加:

```java
FacesContext context = FacesContext.getCurrentInstance();
context.addMessage(null, 
    new FacesMessage(FacesMessage.SEVERITY_ERROR, "エラー", "登録処理に失敗しました: " + e.getMessage()));
```

---

## 7. トランザクション設計

### 7.1 トランザクション境界

* PersonServiceのaddPerson()、updatePerson()メソッドがトランザクション境界
* `@Transactional(TxType.REQUIRED)` - 既存トランザクションがあれば参加、なければ新規作成
* トランザクション分離レベル: READ_COMMITTED（デフォルト）

### 7.2 トランザクションライフサイクル

* 新規追加の場合:
  1. PersonConfirmBean.save()が呼び出される
  2. PersonService.addPerson(person)が実行される（トランザクション開始）
  3. EntityManager.persist(person)でエンティティを永続化コンテキストに追加
  4. メソッド終了時にトランザクションコミット
  5. INSERT文が実行され、PERSON_IDが自動採番される

* 更新の場合:
  1. PersonConfirmBean.save()が呼び出される
  2. PersonService.updatePerson(person)が実行される（トランザクション開始）
  3. EntityManager.merge(person)でエンティティを更新
  4. メソッド終了時にトランザクションコミット
  5. UPDATE文が実行される

* エラー時:
  * RuntimeExceptionが発生した場合、自動的にロールバック
  * データベースへの変更は破棄される

---

## 8. セキュリティ設計

### 8.1 XSS対策

* `<h:outputText>` は自動的にHTMLエスケープを行う
* personName、age、genderの表示は安全

### 8.2 CSRF対策

* `<h:form>` は自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden fieldによる保護

### 8.3 入力検証

* hiddenフィールドの値は入力画面でバリデーション済み
* サーバーサイドで再度Personエンティティに設定されるため、型安全性が保証される

---

## 9. パフォーマンス設計

### 9.1 データベースアクセス

* 新規追加: 1回のINSERT文
* 更新: 1回のUPDATE文
* トランザクション: 短時間のトランザクション（数ミリ秒）

### 9.2 画面表示

* 確認情報の表示: メモリ上のデータのみ（データベースアクセスなし）
* 初期表示パフォーマンス: 高速（@ViewScopedによるBean注入）

---

## 10. ログ設計

### 10.1 ログ出力タイミング

* save()メソッド実行時（新規追加）:
  * ログレベル: INFO
  * メッセージ: "Person added successfully: personId=[自動採番されたID]"

* save()メソッド実行時（更新）:
  * ログレベル: INFO
  * メッセージ: "Person updated successfully: personId=[ID]"

* エラー発生時:
  * ログレベル: SEVERE
  * メッセージ: "Failed to save person: [エラー詳細]"
  * スタックトレースを出力

### 10.2 ログ実装例

```java
private static final Logger logger = Logger.getLogger(PersonConfirmBean.class.getName());

public String save() {
    try {
        // ... 処理 ...
        
        if (personId == null) {
            personService.addPerson(person);
            logger.log(Level.INFO, "Person added successfully: personId={0}", person.getPersonId());
        } else {
            personService.updatePerson(person);
            logger.log(Level.INFO, "Person updated successfully: personId={0}", personId);
        }
        
        return "personList?faces-redirect=true";
    } catch (RuntimeException e) {
        logger.log(Level.SEVERE, "Failed to save person", e);
        // ... エラー処理 ...
    }
}
```

---

## 11. テスト要件

### 11.1 ユニットテスト

* 対象: `PersonConfirmBean`

* テストケース:
  * init()メソッドのテスト（PersonInputBeanからのデータ取得）
  * save()メソッドのテスト（新規追加モード、personId=null）
  * save()メソッドのテスト（更新モード、personId=2）
  * save()メソッドのテスト（エラーケース、RuntimeException発生）

### 11.2 統合テスト

* 対象: PersonConfirmBean + PersonService + Person Entity

* テストケース:
  * 新規追加の統合テスト（データベースへのINSERT確認）
  * 更新の統合テスト（データベースへのUPDATE確認）
  * トランザクションロールバックのテスト

### 11.3 画面テスト

* 対象: personConfirm.xhtml

* テストケース:
  * 確認画面の初期表示（入力データが正しく表示される）
  * 性別の表示変換（"male"→"男性", "female"→"女性"）
  * 登録ボタンのクリック（一覧画面へ遷移）
  * 戻るボタンのクリック（入力画面へ戻る）
  * エラーメッセージの表示

---

## 12. 実装チェックリスト

### 12.1 PersonConfirmBean実装

* PersonService、PersonInputBeanの@Inject
* personId、personName、age、genderフィールドの定義
* init()メソッドの実装（@PostConstruct）
* save()メソッドの実装（新規追加・更新の分岐）
* エラーハンドリングの実装
* getter/setterの実装
* Serializableの実装

### 12.2 personConfirm.xhtml実装

* ページヘッダー（title、CSS）の実装
* エラーメッセージ表示エリア（<h:messages>）の実装
* 確認情報表示（名前、年齢、性別）の実装
* 性別の表示変換（rendered属性）の実装
* hiddenフィールド（personId、personName、age、gender）の実装
* 登録ボタン（<h:commandButton>）の実装
* 戻るボタン（history.back()）の実装

### 12.3 PersonService実装

* addPerson(Person person)メソッドの実装
* updatePerson(Person person)メソッドの実装
* @RequestScoped、@Transactionalアノテーションの付与
* EntityManagerの@PersistenceContext注入

---

## 13. 参考資料

* [screen_design.md](screen_design.md) - 画面設計書
* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
* [../../system/data_model.md](../../system/data_model.md) - データモデル
* [../../system/functional_design.md](../../system/functional_design.md) - システム機能設計書
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
