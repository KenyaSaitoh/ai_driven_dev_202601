<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PERSON確認</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
    <h1>PERSON確認</h1>
    
    <div class="info-group">
        <label>名前:</label>
        <bean:write name="personForm" property="personName"/>
    </div>
    
    <div class="info-group">
        <label>年齢:</label>
        <bean:write name="personForm" property="age"/>
    </div>
    
    <div class="info-group">
        <label>性別:</label>
        <logic:equal name="personForm" property="gender" value="male">男性</logic:equal>
        <logic:equal name="personForm" property="gender" value="female">女性</logic:equal>
    </div>
    
    <html:form action="/personUpdate" method="post">
        <html:hidden property="personId"/>
        <html:hidden property="personName"/>
        <html:hidden property="age"/>
        <html:hidden property="gender"/>
        
        <div class="info-group">
            <html:submit styleClass="button">登録</html:submit>
            <html:button property="back" onclick="history.back();" styleClass="button back">
                戻る
            </html:button>
        </div>
    </html:form>
    
</body>
</html>

