package pro.kensait.jsf.person.bean;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

/**
 * PersonConfirmBean is the Managed Bean for PERSON確認画面 (SCREEN_003_PersonConfirm).
 * This bean handles confirmation display and registration/update of Person data.
 */
@Named("personConfirmBean")
@ViewScoped
public class PersonConfirmBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PersonConfirmBean.class.getName());

    /**
     * PersonService injected by CDI container
     */
    @Inject
    private PersonService personService;

    /**
     * PersonInputBean injected by CDI container to retrieve input data
     */
    @Inject
    private PersonInputBean personInputBean;

    /**
     * PERSON_ID - set when editing an existing person, null for new registration
     */
    private Integer personId;

    /**
     * Person name
     */
    private String personName;

    /**
     * Age
     */
    private Integer age;

    /**
     * Gender - "male" or "female"
     */
    private String gender;

    /**
     * Initialize the bean.
     * Retrieves input data from PersonInputBean and sets them to local fields.
     * This method is automatically called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        this.personId = personInputBean.getPersonId();
        this.personName = personInputBean.getPersonName();
        this.age = personInputBean.getAge();
        this.gender = personInputBean.getGender();
    }

    /**
     * Save the Person data to database.
     * If personId is null, registers as new data (INSERT).
     * If personId is not null, updates existing data (UPDATE).
     * 
     * @return Navigation outcome "personList?faces-redirect=true" on success, null on error
     */
    public String save() {
        try {
            Person person = new Person();
            person.setPersonId(personId);
            person.setPersonName(personName);
            person.setAge(age);
            person.setGender(gender);
            
            if (personId == null) {
                personService.addPerson(person);
                logger.log(Level.INFO, "Person added successfully: personId={0}", person.getPersonId());
            } else {
                personService.updatePerson(person);
                logger.log(Level.INFO, "Person updated successfully: personId={0}", personId);
            }
            
            return "personList?faces-redirect=true";
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to save person", e);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "登録処理に失敗しました: " + e.getMessage()));
            return null;
        }
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
