package pro.kensait.jsf.person.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.kensait.jsf.person.entity.Person;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonServiceTest {
    
    private EntityManagerFactory emf;
    private EntityManager em;
    private PersonService personService;
    
    @BeforeEach
    void setUp() {
        // EntityManagerFactoryとEntityManagerを作成
        emf = Persistence.createEntityManagerFactory("personPU");
        em = emf.createEntityManager();
        
        // PersonServiceを作成してEntityManagerを注入
        personService = new PersonService();
        injectEntityManager(personService, em);
        
        // テストデータを初期化
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        Person person1 = new Person("Alice", 35, "female");
        Person person2 = new Person("Bob", 20, "male");
        Person person3 = new Person("Carol", 30, "female");
        
        em.persist(person1);
        em.persist(person2);
        em.persist(person3);
        
        tx.commit();
    }
    
    @AfterEach
    void tearDown() {
        // テストデータをクリア
        if (em != null && em.isOpen()) {
            EntityTransaction tx = em.getTransaction();
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @Test
    void testGetPersonList() {
        // Given: 初期データが3件存在
        
        // When: 全PERSON取得
        List<Person> personList = personService.getPersonList();
        
        // Then: 3件取得できることを確認
        assertNotNull(personList);
        assertEquals(3, personList.size());
        
        // IDの昇順で取得されることを確認
        assertEquals("Alice", personList.get(0).getPersonName());
        assertEquals("Bob", personList.get(1).getPersonName());
        assertEquals("Carol", personList.get(2).getPersonName());
    }
    
    @Test
    void testGetPersonById() {
        // Given: 初期データが存在
        List<Person> personList = personService.getPersonList();
        Integer personId = personList.get(0).getPersonId();
        
        // When: ID指定でPERSON取得
        Person person = personService.getPersonById(personId);
        
        // Then: 該当のPERSONが取得できることを確認
        assertNotNull(person);
        assertEquals(personId, person.getPersonId());
        assertEquals("Alice", person.getPersonName());
        assertEquals(35, person.getAge());
        assertEquals("female", person.getGender());
    }
    
    @Test
    void testGetPersonById_NotFound() {
        // Given: 存在しないID
        Integer nonExistentId = 9999;
        
        // When: 存在しないIDでPERSON取得
        Person person = personService.getPersonById(nonExistentId);
        
        // Then: nullが返されることを確認
        assertNull(person);
    }
    
    @Test
    void testAddPerson() {
        // Given: 新しいPERSONデータ
        Person newPerson = new Person("David", 25, "male");
        
        // When: PERSON追加
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        personService.addPerson(newPerson);
        tx.commit();
        
        // Then: データベースに追加されていることを確認
        em.clear(); // キャッシュをクリア
        List<Person> personList = personService.getPersonList();
        assertEquals(4, personList.size());
        
        Person addedPerson = personList.stream()
                .filter(p -> "David".equals(p.getPersonName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(addedPerson);
        assertEquals("David", addedPerson.getPersonName());
        assertEquals(25, addedPerson.getAge());
        assertEquals("male", addedPerson.getGender());
        assertNotNull(addedPerson.getPersonId()); // IDが自動採番されることを確認
    }
    
    @Test
    void testUpdatePerson() {
        // Given: 既存のPERSONデータ
        List<Person> personList = personService.getPersonList();
        Person existingPerson = personList.get(0);
        Integer personId = existingPerson.getPersonId();
        
        // When: PERSON更新
        existingPerson.setPersonName("Alice Updated");
        existingPerson.setAge(40);
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        personService.updatePerson(existingPerson);
        tx.commit();
        
        // Then: データベースが更新されていることを確認
        em.clear(); // キャッシュをクリア
        Person updatedPerson = personService.getPersonById(personId);
        
        assertNotNull(updatedPerson);
        assertEquals(personId, updatedPerson.getPersonId());
        assertEquals("Alice Updated", updatedPerson.getPersonName());
        assertEquals(40, updatedPerson.getAge());
        assertEquals("female", updatedPerson.getGender());
    }
    
    @Test
    void testDeletePerson() {
        // Given: 既存のPERSONデータ
        List<Person> personList = personService.getPersonList();
        Person existingPerson = personList.get(0);
        Integer personId = existingPerson.getPersonId();
        
        // When: PERSON削除
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        personService.deletePerson(personId);
        tx.commit();
        
        // Then: データベースから削除されていることを確認
        em.clear(); // キャッシュをクリア
        List<Person> remainingPersonList = personService.getPersonList();
        assertEquals(2, remainingPersonList.size());
        
        Person deletedPerson = personService.getPersonById(personId);
        assertNull(deletedPerson);
    }
    
    @Test
    void testDeletePerson_NotFound() {
        // Given: 存在しないID
        Integer nonExistentId = 9999;
        
        // When: 存在しないIDでPERSON削除
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        personService.deletePerson(nonExistentId);
        tx.commit();
        
        // Then: エラーが発生せず、データは変更されないことを確認
        List<Person> personList = personService.getPersonList();
        assertEquals(3, personList.size());
    }
    
    /**
     * リフレクションを使用してEntityManagerを注入
     * 実際のCDI環境ではコンテナが注入するが、テストでは手動で注入
     */
    private void injectEntityManager(PersonService service, EntityManager entityManager) {
        try {
            java.lang.reflect.Field field = PersonService.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(service, entityManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject EntityManager", e);
        }
    }
}
