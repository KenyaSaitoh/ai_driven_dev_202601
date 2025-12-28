package pro.kensait.struts.person.form;

import org.apache.struts.action.ActionForm;

// PERSON情報のActionForm
public class PersonForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    
    private String personId;
    private String personName;
    private String age;
    private String gender;
    
    // コンストラクタ
    public PersonForm() {}
    
    // ゲッター・セッター
    public String getPersonId() {
        return personId;
    }
    
    public void setPersonId(String personId) {
        this.personId = personId;
    }
    
    public String getPersonName() {
        return personName;
    }
    
    public void setPersonName(String personName) {
        this.personName = personName;
    }
    
    public String getAge() {
        return age;
    }
    
    public void setAge(String age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    // フォームをリセット
    public void reset() {
        this.personId = null;
        this.personName = null;
        this.age = null;
        this.gender = null;
    }
}

