package pro.kensait.jsf.person;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

// 人物情報入力画面のバッキングBean
@ViewScoped
@Named("personInput")
public class PersonInputBean implements Serializable {
    // UIコンポーネントの値を保持するためのプロパティ
    private Person person;

    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    // フラッシュスコープ
    private Flash flash;

    // ライフサイクルメソッド
    @PostConstruct
    public void postConstruct() {
        System.out.println("[ PersonInput#postConstruct ]");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        flash = facesContext.getExternalContext().getFlash();
        person = (Person)flash.get("person");
        if (person == null) {
            person = new Person();
        }
    }

    // アクションメソッド（「確認画面」に遷移する）
    public String confirm() {
        flash.put("person", person);
        return "PersonUpdatePage";
    }
}

