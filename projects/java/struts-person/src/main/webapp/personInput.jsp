<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PERSON入力</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
    <h1>PERSON入力</h1>
    
    <html:form action="/personConfirm" method="post">
        <html:hidden property="personId"/>
        
        <div class="form-group">
            <label>名前:</label>
            <html:text property="personName" size="30"/>
        </div>
        
        <div class="form-group">
            <label>年齢:</label>
            <html:text property="age" size="10"/>
        </div>
        
        <div class="form-group">
            <label>性別:</label>
            <html:radio property="gender" value="male"/>男性
            <html:radio property="gender" value="female" styleClass="gender-radio"/>女性
        </div>
        
        <div class="form-group">
            <html:submit styleClass="button">確認画面へ</html:submit>
            <html:button property="cancel" onclick="location.href='personList.do'" styleClass="button cancel">
                キャンセル
            </html:button>
        </div>
    </html:form>
    
</body>
</html>

