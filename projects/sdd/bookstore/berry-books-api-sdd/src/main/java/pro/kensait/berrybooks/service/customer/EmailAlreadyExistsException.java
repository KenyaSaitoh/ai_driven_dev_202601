package pro.kensait.berrybooks.service.customer;

/**
 * メールアドレス重複例外
 * 
 * 新規登録時に指定されたメールアドレスが既に登録されている場合にスローされる
 */
public class EmailAlreadyExistsException extends RuntimeException {
    
    private final String email;
    
    public EmailAlreadyExistsException(String email) {
        super("指定されたメールアドレスは既に登録されています: " + email);
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}

