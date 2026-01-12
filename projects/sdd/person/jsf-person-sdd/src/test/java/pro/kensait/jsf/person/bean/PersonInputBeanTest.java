package pro.kensait.jsf.person.bean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PersonInputBeanの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class PersonInputBeanTest {
    
    @Mock
    private PersonService personService;
    
    @InjectMocks
    private PersonInputBean personInputBean;
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        // Bean Validationのバリデータを準備
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    /**
     * initForNew()メソッドのテスト（新規追加モード）
     * 全プロパティがnullであることを確認
     */
    @Test
    void testInitForNew() {
        // initForNew()を実行
        personInputBean.initForNew();
        
        // 検証：全プロパティがnull
        assertNull(personInputBean.getPersonId(), "personIdがnullであること");
        assertNull(personInputBean.getPersonName(), "personNameがnullであること");
        assertNull(personInputBean.getAge(), "ageがnullであること");
        assertNull(personInputBean.getGender(), "genderがnullであること");
    }
    
    /**
     * edit()メソッドのテスト（編集モード）
     * 既存データが正しく設定されることを確認
     */
    @Test
    void testEdit() {
        // モックの設定：既存データを返す
        Person existingPerson = new Person(1, "Alice", 35, "female");
        when(personService.getPersonById(1)).thenReturn(existingPerson);
        
        // edit()を実行
        personInputBean.edit(1);
        
        // 検証：既存データがBeanに設定される
        assertEquals(1, personInputBean.getPersonId(), "personIdが1であること");
        assertEquals("Alice", personInputBean.getPersonName(), "personNameがAliceであること");
        assertEquals(35, personInputBean.getAge(), "ageが35であること");
        assertEquals("female", personInputBean.getGender(), "genderがfemaleであること");
        
        // personService.getPersonById(1)が1回呼ばれたことを確認
        verify(personService, times(1)).getPersonById(1);
    }
    
    /**
     * edit()メソッドのテスト（編集対象が存在しない場合）
     * nullが返された場合、プロパティが変更されないことを確認
     */
    @Test
    void testEditNotFound() {
        // モックの設定：nullを返す（存在しないPERSON）
        when(personService.getPersonById(999)).thenReturn(null);
        
        // 事前にプロパティを設定
        personInputBean.initForNew();
        
        // edit()を実行（nullが返される）
        personInputBean.edit(999);
        
        // 検証：プロパティがnullのまま（変更されない）
        assertNull(personInputBean.getPersonId(), "personIdがnullであること");
        assertNull(personInputBean.getPersonName(), "personNameがnullであること");
        assertNull(personInputBean.getAge(), "ageがnullであること");
        assertNull(personInputBean.getGender(), "genderがnullであること");
        
        // personService.getPersonById(999)が1回呼ばれたことを確認
        verify(personService, times(1)).getPersonById(999);
    }
    
    /**
     * confirm()メソッドのテスト
     * 確認画面への遷移が正しく行われることを確認
     */
    @Test
    void testConfirm() {
        // confirm()を実行
        String outcome = personInputBean.confirm();
        
        // 検証：遷移先が"SCREEN_003_PersonConfirm"であること
        assertEquals("SCREEN_003_PersonConfirm", outcome, "遷移先がSCREEN_003_PersonConfirmであること");
    }
    
    /**
     * Bean Validationのテスト（正常系）
     * 全ての入力が正しい場合、バリデーションエラーが発生しないことを確認
     */
    @Test
    void testValidationSuccess() {
        // 正しいデータを設定
        personInputBean.setPersonName("Alice");
        personInputBean.setAge(35);
        personInputBean.setGender("female");
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが0件であること
        assertEquals(0, violations.size(), "バリデーションエラーが0件であること");
    }
    
    /**
     * Bean Validationのテスト（必須項目未入力）
     * 必須項目が未入力の場合、バリデーションエラーが発生することを確認
     */
    @Test
    void testValidationNotNull() {
        // 全プロパティをnullに設定
        personInputBean.setPersonName(null);
        personInputBean.setAge(null);
        personInputBean.setGender(null);
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが3件であること（personName, age, gender）
        assertEquals(3, violations.size(), "バリデーションエラーが3件であること");
        
        // エラーメッセージの確認
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("名前は必須です")),
                "名前の必須エラーメッセージが含まれること");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("年齢は必須です")),
                "年齢の必須エラーメッセージが含まれること");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("性別は必須です")),
                "性別の必須エラーメッセージが含まれること");
    }
    
    /**
     * Bean Validationのテスト（名前の長さ超過）
     * 名前が30文字を超える場合、バリデーションエラーが発生することを確認
     */
    @Test
    void testValidationPersonNameTooLong() {
        // 31文字の名前を設定
        personInputBean.setPersonName("1234567890123456789012345678901"); // 31文字
        personInputBean.setAge(35);
        personInputBean.setGender("female");
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが1件であること
        assertEquals(1, violations.size(), "バリデーションエラーが1件であること");
        
        // エラーメッセージの確認
        ConstraintViolation<PersonInputBean> violation = violations.iterator().next();
        assertEquals("personName", violation.getPropertyPath().toString(), "エラー対象がpersonNameであること");
        assertTrue(violation.getMessage().contains("名前は1文字以上30文字以内で入力してください"),
                "名前の長さエラーメッセージが含まれること");
    }
    
    /**
     * Bean Validationのテスト（名前が空文字）
     * 名前が空文字の場合、バリデーションエラーが発生することを確認
     */
    @Test
    void testValidationPersonNameEmpty() {
        // 空文字の名前を設定
        personInputBean.setPersonName("");
        personInputBean.setAge(35);
        personInputBean.setGender("female");
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが1件であること
        assertEquals(1, violations.size(), "バリデーションエラーが1件であること");
        
        // エラーメッセージの確認
        ConstraintViolation<PersonInputBean> violation = violations.iterator().next();
        assertEquals("personName", violation.getPropertyPath().toString(), "エラー対象がpersonNameであること");
        assertTrue(violation.getMessage().contains("名前は1文字以上30文字以内で入力してください"),
                "名前の長さエラーメッセージが含まれること");
    }
    
    /**
     * Bean Validationのテスト（年齢が負の値）
     * 年齢が負の値の場合、バリデーションエラーが発生することを確認
     */
    @Test
    void testValidationAgeNegative() {
        // 負の値の年齢を設定
        personInputBean.setPersonName("Alice");
        personInputBean.setAge(-1);
        personInputBean.setGender("female");
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが1件であること
        assertEquals(1, violations.size(), "バリデーションエラーが1件であること");
        
        // エラーメッセージの確認
        ConstraintViolation<PersonInputBean> violation = violations.iterator().next();
        assertEquals("age", violation.getPropertyPath().toString(), "エラー対象がageであること");
        assertTrue(violation.getMessage().contains("年齢は0以上で入力してください"),
                "年齢のエラーメッセージが含まれること");
    }
    
    /**
     * Bean Validationのテスト（年齢が0）
     * 年齢が0の場合、バリデーションエラーが発生しないことを確認
     */
    @Test
    void testValidationAgeZero() {
        // 年齢に0を設定
        personInputBean.setPersonName("Alice");
        personInputBean.setAge(0);
        personInputBean.setGender("female");
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが0件であること
        assertEquals(0, violations.size(), "バリデーションエラーが0件であること");
    }
    
    /**
     * ゲッター・セッターのテスト
     */
    @Test
    void testGettersAndSetters() {
        // セッターでプロパティを設定
        personInputBean.setPersonId(1);
        personInputBean.setPersonName("Alice");
        personInputBean.setAge(35);
        personInputBean.setGender("female");
        
        // ゲッターで値を取得して検証
        assertEquals(1, personInputBean.getPersonId(), "personIdが1であること");
        assertEquals("Alice", personInputBean.getPersonName(), "personNameがAliceであること");
        assertEquals(35, personInputBean.getAge(), "ageが35であること");
        assertEquals("female", personInputBean.getGender(), "genderがfemaleであること");
    }
    
    /**
     * 複数のバリデーションエラーのテスト
     * 複数の項目でバリデーションエラーが発生することを確認
     */
    @Test
    void testMultipleValidationErrors() {
        // 複数のバリデーションエラーが発生するデータを設定
        personInputBean.setPersonName(""); // 空文字（エラー）
        personInputBean.setAge(-1); // 負の値（エラー）
        personInputBean.setGender(null); // null（エラー）
        
        // バリデーションを実行
        Set<ConstraintViolation<PersonInputBean>> violations = validator.validate(personInputBean);
        
        // 検証：バリデーションエラーが3件であること
        assertEquals(3, violations.size(), "バリデーションエラーが3件であること");
    }
}
