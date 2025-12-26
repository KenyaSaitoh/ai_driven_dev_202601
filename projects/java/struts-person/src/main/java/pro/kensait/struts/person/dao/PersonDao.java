package pro.kensait.struts.person.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import pro.kensait.struts.person.model.Person;

// PERSON情報のDAOクラス（旧来型のデータソースを使用）
public class PersonDao {
    
    private DataSource dataSource;
    
    // コンストラクタ - JNDIルックアップでデータソースを取得
    public PersonDao() {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/HsqldbDS");
        } catch (NamingException e) {
            throw new RuntimeException("DataSource lookup failed", e);
        }
    }
    
    // 全PERSONを取得
    public List<Person> findAll() throws SQLException {
        List<Person> personList = new ArrayList<>();
        String sql = "SELECT PERSON_ID, PERSON_NAME, AGE, GENDER FROM PERSON ORDER BY PERSON_ID";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Person person = new Person();
                person.setPersonId(rs.getInt("PERSON_ID"));
                person.setPersonName(rs.getString("PERSON_NAME"));
                person.setAge(rs.getInt("AGE"));
                person.setGender(rs.getString("GENDER"));
                personList.add(person);
            }
        }
        
        return personList;
    }
    
    // IDでPERSONを取得
    public Person findById(Integer personId) throws SQLException {
        String sql = "SELECT PERSON_ID, PERSON_NAME, AGE, GENDER FROM PERSON WHERE PERSON_ID = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, personId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();
                    person.setPersonId(rs.getInt("PERSON_ID"));
                    person.setPersonName(rs.getString("PERSON_NAME"));
                    person.setAge(rs.getInt("AGE"));
                    person.setGender(rs.getString("GENDER"));
                    return person;
                }
            }
        }
        
        return null;
    }
    
    // PERSONを追加
    public void insert(Person person) throws SQLException {
        String sql = "INSERT INTO PERSON (PERSON_NAME, AGE, GENDER) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, person.getPersonName());
            pstmt.setInt(2, person.getAge());
            pstmt.setString(3, person.getGender());
            pstmt.executeUpdate();
        }
    }
    
    // PERSONを更新
    public void update(Person person) throws SQLException {
        String sql = "UPDATE PERSON SET PERSON_NAME = ?, AGE = ?, GENDER = ? WHERE PERSON_ID = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, person.getPersonName());
            pstmt.setInt(2, person.getAge());
            pstmt.setString(3, person.getGender());
            pstmt.setInt(4, person.getPersonId());
            pstmt.executeUpdate();
        }
    }
    
    // PERSONを削除
    public void delete(Integer personId) throws SQLException {
        String sql = "DELETE FROM PERSON WHERE PERSON_ID = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, personId);
            pstmt.executeUpdate();
        }
    }
}

