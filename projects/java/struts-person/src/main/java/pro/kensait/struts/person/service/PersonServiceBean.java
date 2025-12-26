package pro.kensait.struts.person.service;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Stateless;

import pro.kensait.struts.person.dao.PersonDao;
import pro.kensait.struts.person.model.Person;

// PERSONサービスの実装（ステートレスセッションBean）
@Stateless
public class PersonServiceBean implements PersonService {
    
    @Override
    public List<Person> getAllPersons() {
        try {
            PersonDao dao = new PersonDao();
            return dao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all persons", e);
        }
    }
    
    @Override
    public Person getPersonById(Integer personId) {
        try {
            PersonDao dao = new PersonDao();
            return dao.findById(personId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get person by id: " + personId, e);
        }
    }
    
    @Override
    public void addPerson(Person person) {
        try {
            PersonDao dao = new PersonDao();
            dao.insert(person);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add person", e);
        }
    }
    
    @Override
    public void updatePerson(Person person) {
        try {
            PersonDao dao = new PersonDao();
            dao.update(person);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update person", e);
        }
    }
    
    @Override
    public void deletePerson(Integer personId) {
        try {
            PersonDao dao = new PersonDao();
            dao.delete(personId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete person: " + personId, e);
        }
    }
}

