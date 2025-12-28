package pro.kensait.jsf.person;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// 人物情報一覧画面のバッキングBean
@ViewScoped
@Named("personTable")
public class PersonTableBean implements Serializable {
    // UIコンポーネントの値を保持するためのプロパティ
    private List<Person> personList;

    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    // フラッシュスコープ
    private Flash flash;

    // ライフサイクルメソッド
    @PostConstruct
    public void postConstruct() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        flash = facesContext.getExternalContext().getFlash();
        personList = personService.getPersonList();
    }

    // インジェクションポイント
    @Inject
    private PersonService personService;

    // アクションメソッド（PERSONを削除する）
    public String removePerson(Integer personId) {
        personService.removePerson(personId);
        return "PersonTablePage";
    }

    // アクションメソッド（PERSONを編集する）
    public String editPerson(Integer personId) {
        Person person = personService.getPerson(personId);
        flash.put("person", person);
        return "PersonInputPage";
    }
}

