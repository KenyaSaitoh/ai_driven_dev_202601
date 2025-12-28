package pro.kensait.struts.person.action;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pro.kensait.struts.person.model.Person;
import pro.kensait.struts.person.service.PersonService;
import pro.kensait.struts.person.form.PersonForm;

// PERSON入力画面を表示するAction
public class PersonInputAction extends Action {
    
    // JNDIルックアップでPersonServiceを取得
    private PersonService getPersonService() throws Exception {
        InitialContext ctx = new InitialContext();
        return (PersonService) ctx.lookup("java:global/struts-person/PersonServiceBean!pro.kensait.struts.person.service.PersonService");
    }
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        PersonForm personForm = (PersonForm) form;
        
        // PersonServiceを取得
        PersonService personService = getPersonService();
        
        // 編集モードの場合は、IDからデータを取得
        String personIdStr = request.getParameter("personId");
        if (personIdStr != null && !personIdStr.isEmpty()) {
            Integer personId = Integer.valueOf(personIdStr);
            Person person = personService.getPersonById(personId);
            
            if (person != null) {
                // フォームに設定
                personForm.setPersonId(person.getPersonId().toString());
                personForm.setPersonName(person.getPersonName());
                personForm.setAge(person.getAge().toString());
                personForm.setGender(person.getGender());
            }
        } else {
            // 新規追加モードの場合はフォームをリセット
            personForm.reset();
        }
        
        // 入力画面に遷移
        return mapping.findForward("success");
    }
}

