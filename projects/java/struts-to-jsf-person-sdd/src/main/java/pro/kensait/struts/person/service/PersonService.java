package pro.kensait.struts.person.service;

import java.util.List;

import pro.kensait.struts.person.model.Person;

// PERSONサービスのインターフェース
public interface PersonService {
    
    // 全PERSONを取得
    List<Person> getAllPersons();
    
    // IDでPERSONを取得
    Person getPersonById(Integer personId);
    
    // PERSONを追加
    void addPerson(Person person);
    
    // PERSONを更新
    void updatePerson(Person person);
    
    // PERSONを削除
    void deletePerson(Integer personId);
}

