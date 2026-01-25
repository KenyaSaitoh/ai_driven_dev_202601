import org.mindrot.jbcrypt.BCrypt;

public class GenerateBCryptHash {
    public static void main(String[] args) {
        String password = "password";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Verification: " + BCrypt.checkpw(password, hash));
    }
}