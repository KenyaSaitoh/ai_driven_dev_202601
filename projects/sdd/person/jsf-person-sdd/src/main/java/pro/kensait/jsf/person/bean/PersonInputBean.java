package pro.kensait.jsf.person.bean;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

/**
 * PersonInputBean is the Managed Bean for PERSON入力画面 (SCREEN_002_PersonInput).
 * This bean handles input form data, validation, and screen transitions.
 */
@Named("personInputBean")
@ViewScoped
public class PersonInputBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * PersonService injected by CDI container
     */
    @Inject
    private PersonService personService;

    /**
     * PERSON_ID - set when editing an existing person, null for new registration
     */
    private Integer personId;

    /**
     * Person name - required, 1-30 characters
     */
    @NotNull(message = "名前を入力してください")
    @Size(min = 1, max = 30, message = "名前は1〜30文字で入力してください")
    private String personName;

    /**
     * Age - required, 0-150
     */
    @NotNull(message = "年齢を入力してください")
    @Min(value = 0, message = "年齢は0以上で入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;

    /**
     * Gender - required, "male" or "female"
     */
    @NotNull(message = "性別を選択してください")
    private String gender;

    /**
     * Initialize the bean.
     * If personId is set (edit mode), load the existing person data.
     * This method is automatically called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        if (personId != null) {
            try {
                Person person = personService.getPersonById(personId);
                if (person != null) {
                    this.personName = person.getPersonName();
                    this.age = person.getAge();
                    this.gender = person.getGender();
                } else {
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "指定されたPERSONが見つかりませんでした", null));
                }
            } catch (RuntimeException e) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "データ取得に失敗しました: " + e.getMessage(), null));
            }
        }
    }

    /**
     * Transition to the confirmation screen.
     * This method is called when the user clicks the "確認画面へ" button.
     * Input data is validated by JSF before this method is invoked.
     * 
     * @return Navigation outcome "personConfirm" (Forward transition)
     */
    public String confirm() {
        return "personConfirm";
    }

    /**
     * Return to the person list screen.
     * This method is called when the user clicks the "キャンセル" button.
     * 
     * @return Navigation outcome "personList?faces-redirect=true" (Redirect transition)
     */
    public String cancel() {
        return "personList?faces-redirect=true";
    }

    // Getter and Setter methods

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
