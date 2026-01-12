package pro.kensait.jsf.person.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PersonTableBeanの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class PersonTableBeanTest {
    
    @Mock
    private PersonService personService;
    
    @InjectMocks
    private PersonTableBean personTableBean;
    
    private List<Person> testPersonList;
    
    @BeforeEach
    void setUp() {
        // テストデータの準備
        testPersonList = Arrays.asList(
            new Person(1, "Alice", 35, "female"),
            new Person(2, "Bob", 20, "male"),
            new Person(3, "Carol", 30, "female")
        );
    }
    
    /**
     * init()メソッドのテスト
     * 全PERSONが取得されることを確認
     */
    @Test
    void testInit() {
        // モックの設定
        when(personService.getPersonList()).thenReturn(testPersonList);
        
        // init()メソッドを実行（@PostConstructメソッドは手動で呼び出し）
        personTableBean.init();
        
        // 検証
        List<Person> result = personTableBean.getPersonList();
        assertNotNull(result, "personListがnullではないこと");
        assertEquals(3, result.size(), "personListのサイズが3であること");
        assertEquals("Alice", result.get(0).getPersonName(), "1件目の名前がAliceであること");
        assertEquals("Bob", result.get(1).getPersonName(), "2件目の名前がBobであること");
        assertEquals("Carol", result.get(2).getPersonName(), "3件目の名前がCarolであること");
        
        // personService.getPersonList()が1回呼ばれたことを確認
        verify(personService, times(1)).getPersonList();
    }
    
    /**
     * delete()メソッドのテスト
     * 削除後、personListが再取得されることを確認
     */
    @Test
    void testDelete() {
        // モックの設定
        // 削除前の一覧取得（init()で使用）
        when(personService.getPersonList()).thenReturn(testPersonList);
        
        // 削除後の一覧取得（delete()で使用）
        List<Person> afterDeleteList = Arrays.asList(
            new Person(1, "Alice", 35, "female"),
            new Person(3, "Carol", 30, "female")
        );
        
        // init()を実行
        personTableBean.init();
        
        // モックの設定を更新（削除後のリストを返す）
        when(personService.getPersonList()).thenReturn(afterDeleteList);
        
        // delete()メソッドを実行（ID=2のPERSONを削除）
        personTableBean.delete(2);
        
        // 検証
        List<Person> result = personTableBean.getPersonList();
        assertNotNull(result, "personListがnullではないこと");
        assertEquals(2, result.size(), "削除後のpersonListのサイズが2であること");
        assertEquals("Alice", result.get(0).getPersonName(), "1件目の名前がAliceであること");
        assertEquals("Carol", result.get(1).getPersonName(), "2件目の名前がCarolであること");
        
        // personService.deletePerson(2)が1回呼ばれたことを確認
        verify(personService, times(1)).deletePerson(2);
        
        // personService.getPersonList()が2回呼ばれたことを確認（init()で1回、delete()で1回）
        verify(personService, times(2)).getPersonList();
    }
    
    /**
     * delete()メソッドのテスト（削除対象が存在しない場合）
     * エラーが発生せず、一覧が再取得されることを確認
     */
    @Test
    void testDeleteNonExistentPerson() {
        // モックの設定
        when(personService.getPersonList()).thenReturn(testPersonList);
        
        // init()を実行
        personTableBean.init();
        
        // delete()メソッドを実行（存在しないID=999を削除）
        // personService.deletePerson()は何もしない（nullチェックで処理スキップ）
        doNothing().when(personService).deletePerson(999);
        
        // delete()を実行
        personTableBean.delete(999);
        
        // 検証
        // エラーが発生しないことを確認（例外がスローされない）
        List<Person> result = personTableBean.getPersonList();
        assertNotNull(result, "personListがnullではないこと");
        
        // personService.deletePerson(999)が1回呼ばれたことを確認
        verify(personService, times(1)).deletePerson(999);
        
        // personService.getPersonList()が2回呼ばれたことを確認
        verify(personService, times(2)).getPersonList();
    }
    
    /**
     * getPersonList()メソッドのテスト
     */
    @Test
    void testGetPersonList() {
        // モックの設定
        when(personService.getPersonList()).thenReturn(testPersonList);
        
        // init()を実行
        personTableBean.init();
        
        // getPersonList()を実行
        List<Person> result = personTableBean.getPersonList();
        
        // 検証
        assertNotNull(result, "personListがnullではないこと");
        assertEquals(3, result.size(), "personListのサイズが3であること");
        assertSame(testPersonList, result, "返されるリストがtestPersonListと同じインスタンスであること");
    }
    
    /**
     * init()メソッドのテスト（データが存在しない場合）
     * 空のリストが返されることを確認
     */
    @Test
    void testInitWithEmptyList() {
        // モックの設定（空のリスト）
        when(personService.getPersonList()).thenReturn(Arrays.asList());
        
        // init()を実行
        personTableBean.init();
        
        // 検証
        List<Person> result = personTableBean.getPersonList();
        assertNotNull(result, "personListがnullではないこと");
        assertEquals(0, result.size(), "personListのサイズが0であること");
        
        // personService.getPersonList()が1回呼ばれたことを確認
        verify(personService, times(1)).getPersonList();
    }
}
