package pro.kensait.jsf.person;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// 人物情報確認・更新画面のバッキングBean
@ViewScoped
@Named("personUpdate")
public class PersonUpdateBean implements Serializable {
    // UIコンポーネントの値を保持するためのプロパティ
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    // インジェクションポイント
    @Inject
    private PersonService personService;

    // ライフサイクルメソッド
    @PostConstruct
    public void postConstruct() {
        System.out.println("[ PersonUpdateBean#postConstruct ]");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        flash = facesContext.getExternalContext().getFlash();
        person = (Person) flash.get("person");
    }

    // フラッシュスコープ
    private Flash flash;

    // アクションメソッド（PERSONを更新・追加する）
    public String updatePerson() {
        if (person.getPersonId() != null) {
            personService.updatePerson(person);
        } else {
            personService.addPerson(person);
        }
        return "PersonTablePage";
    }

    // アクションメソッド（「入力画面」に戻る）
    public String back() {
        flash.put("person", person);
        return "PersonInputPage";
    }
}

