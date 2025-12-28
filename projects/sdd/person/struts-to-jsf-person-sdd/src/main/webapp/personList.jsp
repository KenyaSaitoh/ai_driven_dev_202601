<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ page import="dev.berry.model.Person" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PERSON一覧</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
    <h1>PERSON一覧</h1>
    
    <a href="personInput.do" class="button-link add">新規追加</a>
    
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>名前</th>
                <th>年齢</th>
                <th>性別</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <logic:iterate id="person" name="personList" type="dev.berry.model.Person">
                <tr>
                    <td><bean:write name="person" property="personId"/></td>
                    <td><bean:write name="person" property="personName"/></td>
                    <td><bean:write name="person" property="age"/></td>
                    <td>
                        <logic:equal name="person" property="gender" value="male">男性</logic:equal>
                        <logic:equal name="person" property="gender" value="female">女性</logic:equal>
                    </td>
                    <td>
                        <a href="personInput.do?personId=<bean:write name="person" property="personId"/>" class="button-link">編集</a>
                        <a href="personDelete.do?personId=<bean:write name="person" property="personId"/>" 
                           class="button-link delete" 
                           onclick="return confirm('削除してもよろしいですか？');">削除</a>
                    </td>
                </tr>
            </logic:iterate>
        </tbody>
    </table>
    
</body>
</html>

