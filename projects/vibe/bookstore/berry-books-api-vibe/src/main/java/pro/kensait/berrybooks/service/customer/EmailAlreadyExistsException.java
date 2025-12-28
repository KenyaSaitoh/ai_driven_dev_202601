package pro.kensait.berrybooks.service.customer;

// メールアドレス重複時にスローされる例外クラス
public class EmailAlreadyExistsException extends RuntimeException {
    private String email;

    public EmailAlreadyExistsException() {
        super();
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public EmailAlreadyExistsException(String email, String message) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}



