package pro.kensait.jsf.person.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PersonConfirmBeanの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class PersonConfirmBeanTest {
    
    @Mock
    private PersonService personService;
    
    @Mock
    private PersonInputBean personInputBean;
    
    @InjectMocks
    private PersonConfirmBean personConfirmBean;
    
    /**
     * init()メソッドのテスト
     * PersonInputBeanからデータがコピーされることを確認
     */
    @Test
    void testInit() {
        // モックの設定：PersonInputBeanからデータを返す
        when(personInputBean.getPersonId()).thenReturn(1);
        when(personInputBean.getPersonName()).thenReturn("Alice");
        when(personInputBean.getAge()).thenReturn(35);
        when(personInputBean.getGender()).thenReturn("female");
        
        // init()を実行
        personConfirmBean.init();
        
        // 検証：PersonInputBeanからデータがコピーされる
        assertEquals(1, personConfirmBean.getPersonId(), "personIdが1であること");
        assertEquals("Alice", personConfirmBean.getPersonName(), "personNameがAliceであること");
        assertEquals(35, personConfirmBean.getAge(), "ageが35であること");
        assertEquals("female", personConfirmBean.getGender(), "genderがfemaleであること");
        
        // personInputBeanのゲッターが1回ずつ呼ばれたことを確認
        verify(personInputBean, times(1)).getPersonId();
        verify(personInputBean, times(1)).getPersonName();
        verify(personInputBean, times(1)).getAge();
        verify(personInputBean, times(1)).getGender();
    }
    
    /**
     * init()メソッドのテスト（新規追加モード）
     * personIdがnullの場合
     */
    @Test
    void testInitForNew() {
        // モックの設定：PersonInputBeanからデータを返す（personIdはnull）
        when(personInputBean.getPersonId()).thenReturn(null);
        when(personInputBean.getPersonName()).thenReturn("Alice");
        when(personInputBean.getAge()).thenReturn(35);
        when(personInputBean.getGender()).thenReturn("female");
        
        // init()を実行
        personConfirmBean.init();
        
        // 検証：PersonInputBeanからデータがコピーされる（personIdはnull）
        assertNull(personConfirmBean.getPersonId(), "personIdがnullであること（新規追加モード）");
        assertEquals("Alice", personConfirmBean.getPersonName(), "personNameがAliceであること");
        assertEquals(35, personConfirmBean.getAge(), "ageが35であること");
        assertEquals("female", personConfirmBean.getGender(), "genderがfemaleであること");
    }
    
    /**
     * register()メソッドのテスト（新規追加）
     * personId == null の場合、addPerson()が呼ばれることを確認
     */
    @Test
    void testRegisterAdd() {
        // 事前準備：PersonConfirmBeanにデータを設定（新規追加モード）
        personConfirmBean.setPersonId(null);
        personConfirmBean.setPersonName("Alice");
        personConfirmBean.setAge(35);
        personConfirmBean.setGender("female");
        
        // register()を実行
        String outcome = personConfirmBean.register();
        
        // 検証：personService.addPerson()が1回呼ばれたことを確認
        verify(personService, times(1)).addPerson(any(Person.class));
        
        // 検証：personService.updatePerson()は呼ばれないことを確認
        verify(personService, never()).updatePerson(any(Person.class));
        
        // 検証：戻り値が"SCREEN_001_PersonList?faces-redirect=true"であること
        assertEquals("SCREEN_001_PersonList?faces-redirect=true", outcome, 
                "戻り値がSCREEN_001_PersonList?faces-redirect=trueであること");
    }
    
    /**
     * register()メソッドのテスト（更新）
     * personId != null の場合、updatePerson()が呼ばれることを確認
     */
    @Test
    void testRegisterUpdate() {
        // 事前準備：PersonConfirmBeanにデータを設定（編集モード）
        personConfirmBean.setPersonId(1);
        personConfirmBean.setPersonName("Alice Updated");
        personConfirmBean.setAge(40);
        personConfirmBean.setGender("female");
        
        // register()を実行
        String outcome = personConfirmBean.register();
        
        // 検証：personService.updatePerson()が1回呼ばれたことを確認
        verify(personService, times(1)).updatePerson(any(Person.class));
        
        // 検証：personService.addPerson()は呼ばれないことを確認
        verify(personService, never()).addPerson(any(Person.class));
        
        // 検証：戻り値が"SCREEN_001_PersonList?faces-redirect=true"であること
        assertEquals("SCREEN_001_PersonList?faces-redirect=true", outcome, 
                "戻り値がSCREEN_001_PersonList?faces-redirect=trueであること");
    }
    
    /**
     * register()メソッドのテスト（リダイレクト）
     * 戻り値がfaces-redirectを含むことを確認
     */
    @Test
    void testRegisterRedirect() {
        // 事前準備：PersonConfirmBeanにデータを設定
        personConfirmBean.setPersonId(null);
        personConfirmBean.setPersonName("Alice");
        personConfirmBean.setAge(35);
        personConfirmBean.setGender("female");
        
        // register()を実行
        String outcome = personConfirmBean.register();
        
        // 検証：戻り値に"faces-redirect=true"が含まれることを確認
        assertTrue(outcome.contains("faces-redirect=true"), 
                "戻り値にfaces-redirect=trueが含まれること（Post-Redirect-Getパターン）");
        
        // 検証：戻り値に"SCREEN_001_PersonList"が含まれることを確認
        assertTrue(outcome.contains("SCREEN_001_PersonList"), 
                "戻り値にSCREEN_001_PersonListが含まれること");
    }
    
    /**
     * register()メソッドのテスト（Personエンティティの生成）
     * 正しいPersonエンティティが生成されることを確認
     */
    @Test
    void testRegisterPersonEntity() {
        // 事前準備：PersonConfirmBeanにデータを設定
        personConfirmBean.setPersonId(1);
        personConfirmBean.setPersonName("Alice");
        personConfirmBean.setAge(35);
        personConfirmBean.setGender("female");
        
        // register()を実行
        personConfirmBean.register();
        
        // 検証：personService.updatePerson()が呼ばれた際の引数を確認
        verify(personService, times(1)).updatePerson(argThat(person -> 
            person.getPersonId().equals(1) &&
            person.getPersonName().equals("Alice") &&
            person.getAge().equals(35) &&
            person.getGender().equals("female")
        ));
    }
    
    /**
     * ゲッター・セッターのテスト
     */
    @Test
    void testGettersAndSetters() {
        // セッターでプロパティを設定
        personConfirmBean.setPersonId(1);
        personConfirmBean.setPersonName("Alice");
        personConfirmBean.setAge(35);
        personConfirmBean.setGender("female");
        
        // ゲッターで値を取得して検証
        assertEquals(1, personConfirmBean.getPersonId(), "personIdが1であること");
        assertEquals("Alice", personConfirmBean.getPersonName(), "personNameがAliceであること");
        assertEquals(35, personConfirmBean.getAge(), "ageが35であること");
        assertEquals("female", personConfirmBean.getGender(), "genderがfemaleであること");
    }
    
    /**
     * register()メソッドのテスト（新規追加モード：personId == null）
     * addPerson()の呼び出し時にpersonIdがnullであることを確認
     */
    @Test
    void testRegisterAddWithNullPersonId() {
        // 事前準備：PersonConfirmBeanにデータを設定（personIdはnull）
        personConfirmBean.setPersonId(null);
        personConfirmBean.setPersonName("Bob");
        personConfirmBean.setAge(20);
        personConfirmBean.setGender("male");
        
        // register()を実行
        personConfirmBean.register();
        
        // 検証：personService.addPerson()が呼ばれた際の引数を確認
        verify(personService, times(1)).addPerson(argThat(person -> 
            person.getPersonId() == null &&
            person.getPersonName().equals("Bob") &&
            person.getAge().equals(20) &&
            person.getGender().equals("male")
        ));
    }
    
    /**
     * init()とregister()の一連の流れをテスト
     */
    @Test
    void testInitAndRegisterFlow() {
        // ステップ1: init()でPersonInputBeanからデータを受け取る
        when(personInputBean.getPersonId()).thenReturn(null);
        when(personInputBean.getPersonName()).thenReturn("Carol");
        when(personInputBean.getAge()).thenReturn(30);
        when(personInputBean.getGender()).thenReturn("female");
        
        personConfirmBean.init();
        
        // 検証：データがコピーされている
        assertNull(personConfirmBean.getPersonId());
        assertEquals("Carol", personConfirmBean.getPersonName());
        assertEquals(30, personConfirmBean.getAge());
        assertEquals("female", personConfirmBean.getGender());
        
        // ステップ2: register()でデータを登録
        String outcome = personConfirmBean.register();
        
        // 検証：addPerson()が呼ばれた
        verify(personService, times(1)).addPerson(any(Person.class));
        
        // 検証：リダイレクト先が正しい
        assertEquals("SCREEN_001_PersonList?faces-redirect=true", outcome);
    }
}
