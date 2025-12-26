package pro.kensait.jsf.person;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

// 人物情報の取得・登録・更新・削除を行うサービスクラス
@RequestScoped
@Transactional(TxType.REQUIRED)
public class PersonService {
    @PersistenceContext(unitName = "MyPersistenceUnit")
    private EntityManager entitiManager;

    public Person getPerson(Integer personId) {
        return entitiManager.find(Person.class, personId);
    }

    @SuppressWarnings("unchecked")
    public List<Person> getPersonList() {
        Query query = entitiManager.createQuery(
                "SELECT p FROM Person AS p");
        return query.getResultList();
    }

    public void addPerson(Person person) {
        entitiManager.persist(person);
    }

    public void removePerson(Integer personId) {
        Person person = entitiManager.find(Person.class, personId);
        entitiManager.remove(person);
    }

    public void updatePerson(Person person) {
        entitiManager.merge(person);
    }
}

