package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 顧客エンティティ
 * 
 * テーブル: CUSTOMER
 * 注意: このテーブルは外部API（berry-books-rest）経由でアクセスされる
 */
@Entity
@Table(name = "CUSTOMER")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Integer customerId;
    
    @Column(name = "CUSTOMER_NAME", length = 30, nullable = false)
    private String customerName;
    
    @Column(name = "PASSWORD", length = 60, nullable = false)
    private String password;
    
    @Column(name = "EMAIL", length = 30, nullable = false, unique = true)
    private String email;
    
    @Column(name = "BIRTHDAY")
    private LocalDate birthday;
    
    @Column(name = "ADDRESS", length = 120)
    private String address;
    
    // コンストラクタ
    public Customer() {
    }
    
    public Customer(Integer customerId, String customerName, String password, 
                   String email, LocalDate birthday, String address) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.password = password;
        this.email = email;
        this.birthday = birthday;
        this.address = address;
    }
    
    // Getter/Setter
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", email='" + email + '\'' +
                ", birthday=" + birthday +
                ", address='" + address + '\'' +
                '}';
    }
}

