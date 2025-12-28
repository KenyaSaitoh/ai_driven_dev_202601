package pro.kensait.berrybooks.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 顧客を表すエンティティクラス
@Entity
@Table(name = "CUSTOMER")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "BIRTHDAY")
    private LocalDate birthday;

    @Column(name = "ADDRESS")
    private String address;

    public Customer() {
    }

    public Customer(String customerName, String email, String password, 
                   LocalDate birthday, String address) {
        this.customerName = customerName;
        this.email = email;
        this.password = password;
        this.birthday = birthday;
        this.address = address;
    }

    // アクセサメソッド
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        return "Customer [customerId=" + customerId + ", customerName=" + customerName
                + ", email=" + email + ", birthday=" + birthday + ", address=" + address + "]";
    }
}

