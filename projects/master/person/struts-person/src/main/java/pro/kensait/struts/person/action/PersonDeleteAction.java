package pro.kensait.struts.person.action;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pro.kensait.struts.person.service.PersonService;

// PERSONを削除するAction
public class PersonDeleteAction extends Action {
    
    // JNDIルックアップでPersonServiceを取得
    private PersonService getPersonService() throws Exception {
        InitialContext ctx = new InitialContext();
        return (PersonService) ctx.lookup("java:global/struts-person/PersonServiceBean!pro.kensait.struts.person.service.PersonService");
    }
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // PersonServiceを取得
        PersonService personService = getPersonService();
        
        // パラメータからIDを取得
        String personIdStr = request.getParameter("personId");
        if (personIdStr != null && !personIdStr.isEmpty()) {
            Integer personId = Integer.valueOf(personIdStr);
            
            // 削除実行
            personService.deletePerson(personId);
        }
        
        // 一覧画面にリダイレクト
        return mapping.findForward("success");
    }
}

