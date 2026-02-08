package pro.kensait.berrybooks.external.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 顧客情報の転送オブジェクト
 */
public class CustomerTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer customerId;
    private String customerName;
    private String password;
    private String email;
    private LocalDate birthday;
    private String address;
    
    public CustomerTO() {
    }
    
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
}