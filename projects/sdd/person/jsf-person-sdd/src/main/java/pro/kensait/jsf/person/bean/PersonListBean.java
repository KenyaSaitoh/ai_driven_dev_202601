package pro.kensait.jsf.person.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

/**
 * PersonListBean is the Managed Bean for PERSON一覧画面 (SCREEN_001_PersonList).
 * This bean handles the display of all Person records and provides delete functionality.
 */
@Named("personListBean")
@ViewScoped
public class PersonListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * PersonService injected by CDI container
     */
    @Inject
    private PersonService personService;

    /**
     * List of all Person records to be displayed on the screen
     */
    private List<Person> personList;

    /**
     * Initialize the bean by loading all Person records from the database.
     * This method is automatically called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        try {
            personList = personService.getAllPersons();
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "データ取得に失敗しました: " + e.getMessage()));
            personList = new ArrayList<>();
        }
    }

    /**
     * Get the list of all Person records.
     * 
     * @return List of Person entities
     */
    public List<Person> getPersonList() {
        return personList;
    }

    /**
     * Delete a Person record by personId.
     * After successful deletion, the list is reloaded and a success message is displayed.
     * 
     * @param personId The ID of the person to delete
     * @return null (to reload the same page)
     */
    public String deletePerson(Integer personId) {
        try {
            personService.deletePerson(personId);
            init(); // Reload the list
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "成功", "PERSONを削除しました"));
            return null; // Reload the same page
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "削除処理に失敗しました: " + e.getMessage()));
            return null;
        }
    }
}
