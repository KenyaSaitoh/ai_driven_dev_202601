package pro.kensait.berrybooks.model;

// 顧客更新用の転送オブジェクト
public class CustomerTO {
    private String customerName;
    private String email;
    private String birthday; // yyyy-MM-dd形式（REST APIの仕様に合わせる）
    private String address;

    // デフォルトコンストラクタ
    public CustomerTO() {}

    // コンストラクタ
    public CustomerTO(String customerName, String email, String birthday, String address) {
        this.customerName = customerName;
        this.email = email;
        this.birthday = birthday;
        this.address = address;
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
