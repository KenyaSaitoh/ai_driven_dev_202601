package pro.kensait.struts.person.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pro.kensait.struts.person.form.PersonForm;

// PERSON確認画面を表示するAction
public class PersonConfirmAction extends Action {
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        PersonForm personForm = (PersonForm) form;
        
        // フォームデータをリクエストスコープに設定
        request.setAttribute("personForm", personForm);
        
        // 確認画面に遷移
        return mapping.findForward("success");
    }
}

