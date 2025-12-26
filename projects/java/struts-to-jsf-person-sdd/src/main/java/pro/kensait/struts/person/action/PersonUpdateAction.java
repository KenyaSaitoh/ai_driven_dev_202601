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

// PERSONを更新または追加するAction
public class PersonUpdateAction extends Action {
    
    // JNDIルックアップでPersonServiceを取得
    private PersonService getPersonService() throws Exception {
        InitialContext ctx = new InitialContext();
        return (PersonService) ctx.lookup("java:global/struts_to_jsf_person/PersonServiceBean!pro.kensait.struts.person.service.PersonService");
    }
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        PersonForm personForm = (PersonForm) form;
        
        // PersonServiceを取得
        PersonService personService = getPersonService();
        
        // Formデータをモデルに変換
        Person person = new Person();
        if (personForm.getPersonId() != null && !personForm.getPersonId().isEmpty()) {
            person.setPersonId(Integer.valueOf(personForm.getPersonId()));
        }
        person.setPersonName(personForm.getPersonName());
        person.setAge(Integer.valueOf(personForm.getAge()));
        person.setGender(personForm.getGender());
        
        // 更新または追加
        if (person.getPersonId() != null) {
            personService.updatePerson(person);
        } else {
            personService.addPerson(person);
        }
        
        // 一覧画面にリダイレクト
        return mapping.findForward("success");
    }
}

