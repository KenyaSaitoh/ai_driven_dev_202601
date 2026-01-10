package pro.kensait.berrybooks.service.exception;

/**
 * メールアドレス重複例外
 * 
 * 新規登録時にメールアドレスが既に存在する場合にスローされる。
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("指定されたメールアドレスは既に登録されています: " + email);
        this.email = email;
    }

    public EmailAlreadyExistsException(String email, String message) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
