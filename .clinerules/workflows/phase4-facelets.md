# Phase 4: ビュー層のFacelets化

struts-to-jsf-personプロジェクトのビュー層を、JSPからFacelets（XHTML）に移行します。

## 作業概要

このフェーズでは以下を実施します：
1. PersonTablePage.xhtml（一覧画面）の作成
2. PersonInputPage.xhtml（入力画面）の作成
3. PersonConfirmPage.xhtml（確認画面）の作成
4. 旧JSPファイルの削除

## 詳細手順

### 1. PersonTablePage.xhtml（一覧画面）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/webapp/PersonTablePage.xhtml`

**役割：** `personList.jsp`の置き換え

**基本構造：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<h:head>
    <title>人物一覧</title>
    <h:outputStylesheet library="css" name="style.css"/>
</h:head>
<h:body>
    <h1>人物一覧</h1>
    
    <!-- 新規追加リンク -->
    <h:link outcome="PersonInputPage" value="新規追加"/>
    
    <!-- 一覧テーブル -->
    <h:dataTable value="#{personTable.personList}" var="person" 
                 styleClass="table">
        <h:column>
            <f:facet name="header">ID</f:facet>
            <h:outputText value="#{person.personId}"/>
        </h:column>
        
        <h:column>
            <f:facet name="header">名前</f:facet>
            <h:outputText value="#{person.personName}"/>
        </h:column>
        
        <h:column>
            <f:facet name="header">年齢</f:facet>
            <h:outputText value="#{person.age}"/>
        </h:column>
        
        <h:column>
            <f:facet name="header">性別</f:facet>
            <h:outputText value="男性" rendered="#{person.gender == 'male'}"/>
            <h:outputText value="女性" rendered="#{person.gender == 'female'}"/>
        </h:column>
        
        <h:column>
            <f:facet name="header">操作</f:facet>
            <h:form>
                <h:commandButton value="編集" 
                               action="#{personTable.editPerson(person.personId)}"/>
                <h:commandButton value="削除" 
                               action="#{personTable.removePerson(person.personId)}"
                               onclick="return confirm('削除してもよろしいですか？');"/>
            </h:form>
        </h:column>
    </h:dataTable>
</h:body>
</html>
```

**重要な変更点：**
- `<logic:iterate>` → `<h:dataTable>`
- `<bean:write>` → `<h:outputText>`
- リクエストスコープ → EL式でBean直接アクセス（`#{personTable.personList}`）
- 条件表示は`rendered`属性で制御

### 2. PersonInputPage.xhtml（入力画面）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/webapp/PersonInputPage.xhtml`

**役割：** `personInput.jsp`の置き換え

**基本構造：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<h:head>
    <title>人物入力</title>
    <h:outputStylesheet library="css" name="style.css"/>
</h:head>
<h:body>
    <h1>人物入力</h1>
    
    <h:form>
        <!-- 非表示フィールド（編集時のID） -->
        <h:inputHidden value="#{personInput.personId}"/>
        
        <table>
            <tr>
                <th>名前：</th>
                <td>
                    <h:inputText value="#{personInput.personName}" 
                               required="true"
                               requiredMessage="名前を入力してください"/>
                </td>
            </tr>
            <tr>
                <th>年齢：</th>
                <td>
                    <h:inputText value="#{personInput.age}" 
                               required="true"
                               requiredMessage="年齢を入力してください"/>
                </td>
            </tr>
            <tr>
                <th>性別：</th>
                <td>
                    <h:selectOneRadio value="#{personInput.gender}" 
                                    required="true"
                                    requiredMessage="性別を選択してください">
                        <f:selectItem itemLabel="男性" itemValue="male"/>
                        <f:selectItem itemLabel="女性" itemValue="female"/>
                    </h:selectOneRadio>
                </td>
            </tr>
        </table>
        
        <h:commandButton value="確認" action="#{personInput.confirm()}"/>
        <h:commandButton value="キャンセル" 
                       action="#{personInput.cancel()}" 
                       immediate="true"/>
    </h:form>
</h:body>
</html>
```

**重要な変更点：**
- `<html:form>` → `<h:form>`
- `<html:text>` → `<h:inputText>`
- value属性でBeanプロパティとバインド
- required属性で必須検証
- immediate="true"で検証スキップ（キャンセルボタン）
- 型変換は自動（age: String → Integer）

### 3. PersonConfirmPage.xhtml（確認画面）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/webapp/PersonConfirmPage.xhtml`

**役割：** `personConfirm.jsp`の置き換え

**基本構造：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<h:head>
    <title>人物確認</title>
    <h:outputStylesheet library="css" name="style.css"/>
</h:head>
<h:body>
    <h1>人物確認</h1>
    
    <table>
        <tr>
            <th>名前：</th>
            <td><h:outputText value="#{personUpdate.person.personName}"/></td>
        </tr>
        <tr>
            <th>年齢：</th>
            <td><h:outputText value="#{personUpdate.person.age}"/></td>
        </tr>
        <tr>
            <th>性別：</th>
            <td>
                <h:outputText value="男性" 
                            rendered="#{personUpdate.person.gender == 'male'}"/>
                <h:outputText value="女性" 
                            rendered="#{personUpdate.person.gender == 'female'}"/>
            </td>
        </tr>
    </table>
    
    <h:form>
        <h:commandButton value="登録" action="#{personUpdate.register()}"/>
        <h:commandButton value="戻る" 
                       action="#{personUpdate.back()}" 
                       immediate="true"/>
    </h:form>
</h:body>
</html>
```

**重要な変更点：**
- 確認表示はすべて`<h:outputText>`
- EL式で直接Personオブジェクトのプロパティにアクセス
- 性別の条件分岐表示

### 4. 削除するファイル

**削除対象：**
- `projects/java/struts-to-jsf-person/src/main/webapp/index.jsp`
- `projects/java/struts-to-jsf-person/src/main/webapp/personList.jsp`
- `projects/java/struts-to-jsf-person/src/main/webapp/personInput.jsp`
- `projects/java/struts-to-jsf-person/src/main/webapp/personConfirm.jsp`

## 実行手順

1. PersonTablePage.xhtmlを作成
2. PersonInputPage.xhtmlを作成
3. PersonConfirmPage.xhtmlを作成
4. 旧JSPファイル（4つ）を削除

## 注意事項

### XHTMLの要件
- 整形式XML（well-formed XML）
- 全タグを閉じる必要がある
- 属性値は引用符で囲む
- 大文字小文字を区別

### 名前空間
- `xmlns:h="jakarta.faces.html"` - JSF HTMLタグライブラリ
- `xmlns:f="jakarta.faces.core"` - JSF Coreタグライブラリ
- Jakarta EE 10では`jakarta.faces`を使用

### EL式
- `#{}` - 遅延評価（JSF推奨）
- バッキングBeanのプロパティに直接アクセス
- メソッド呼び出しも可能（`#{bean.method()}`）

### バリデーション
- `required`属性 - 必須チェック
- `requiredMessage`属性 - エラーメッセージ
- Bean Validationとの連携も可能

### CSSスタイリング
- `<h:outputStylesheet library="css" name="style.css"/>`でCSS読み込み
- `styleClass`属性でCSSクラス指定
- 既存のstyle.cssを活用可能

### 自動エスケープ
- `<h:outputText>` はXSS対策で自動エスケープ
- HTMLを出力する場合は`escape="false"`（慎重に使用）

### フォーム処理
- `<h:form>`内のボタンはPOST送信
- `immediate="true"`で検証フェーズをスキップ
- アクションメソッドで画面遷移を制御

## 確認項目

- [ ] PersonTablePage.xhtmlが作成されている
- [ ] PersonInputPage.xhtmlが作成されている
- [ ] PersonConfirmPage.xhtmlが作成されている
- [ ] 各XHTMLファイルが整形式XMLになっている
- [ ] 旧JSPファイル4つが削除されている
- [ ] CSSが正しく読み込まれている
