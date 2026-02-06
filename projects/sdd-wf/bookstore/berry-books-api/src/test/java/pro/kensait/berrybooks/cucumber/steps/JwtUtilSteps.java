package pro.kensait.berrybooks.cucumber.steps;

import io.cucumber.java.ja.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pro.kensait.berrybooks.security.JwtUtil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil Cucumber ステップ定義
 */
public class JwtUtilSteps {

    private JwtUtil jwtUtil;
    private String token;
    private Boolean validationResult;
    private Integer customerId;
    private SecretKey secretKey;

    @前提("JwtUtilが初期化されている")
    public void jwtUtilが初期化されている() {
        jwtUtil = new JwtUtil();
        jwtUtil.init();
    }

    @かつ("秘密鍵が設定されている")
    public void 秘密鍵が設定されている() {
        String secret = "your-secret-key-at-least-256-bits-long-for-hs256-algorithm";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        assertNotNull(secretKey);
    }

    @かつ("customerId={int}, email={string}")
    public void customerIdEmail(int id, String email) {
        this.customerId = id;
    }

    @もし("JwtUtil.generateToken\\({int}, {string}\\)を呼び出す")
    public void jwtUtilGenerateTokenを呼び出す(int id, String email) {
        token = jwtUtil.generateToken(id, email);
    }

    @ならば("JWTトークン文字列が返される")
    public void jwtトークン文字列が返される() {
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @かつ("トークンが{int}つのパート（ヘッダー、ペイロード、署名）で構成されている")
    public void トークンがつのパートで構成されている(int parts) {
        String[] tokenParts = token.split("\\.");
        assertEquals(parts, tokenParts.length);
    }

    @かつ("ペイロードにcustomerId={int}が含まれている")
    public void ペイロードにcustomerIdが含まれている(int expectedId) {
        Integer actualId = jwtUtil.getCustomerIdFromToken(token);
        assertEquals(expectedId, actualId);
    }

    @かつ("ペイロードにemail={string}が含まれている")
    public void ペイロードにemailが含まれている(String expectedEmail) {
        String actualEmail = jwtUtil.getEmailFromToken(token);
        assertEquals(expectedEmail, actualEmail);
    }

    @かつ("有効なJWTトークンが存在する")
    public void 有効なJWTトークンが存在する() {
        token = jwtUtil.generateToken(1, "test@example.com");
    }

    @もし("JwtUtil.validateToken\\(validToken\\)を呼び出す")
    public void jwtUtilValidateTokenValidTokenを呼び出す() {
        validationResult = jwtUtil.validateToken(token);
    }

    @ならば("trueが返される")
    public void trueが返される() {
        assertTrue(validationResult);
    }

    @かつ("期限切れのJWTトークンが存在する")
    public void 期限切れのJWTトークンが存在する() {
        // Generate expired token
        String secret = "your-secret-key-at-least-256-bits-long-for-hs256-algorithm";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() - 1000); // 1秒前に期限切れ
        
        token = Jwts.builder()
                .claim("customerId", 1)
                .claim("email", "test@example.com")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    @もし("JwtUtil.validateToken\\(expiredToken\\)を呼び出す")
    public void jwtUtilValidateTokenExpiredTokenを呼び出す() {
        validationResult = jwtUtil.validateToken(token);
    }

    @ならば("falseが返される")
    public void falseが返される() {
        assertFalse(validationResult);
    }

    @かつ("不正な署名のJWTトークンが存在する")
    public void 不正な署名のJWTトークンが存在する() {
        // Generate token with different key
        String wrongSecret = "wrong-secret-key-at-least-256-bits-long-for-hs256-algorithm";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 86400000);
        
        token = Jwts.builder()
                .claim("customerId", 1)
                .claim("email", "test@example.com")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(wrongKey)
                .compact();
    }

    @もし("JwtUtil.validateToken\\(invalidToken\\)を呼び出す")
    public void jwtUtilValidateTokenInvalidTokenを呼び出す() {
        validationResult = jwtUtil.validateToken(token);
    }

    @かつ("有効なJWTトークン（customerId={int}含む）が存在する")
    public void 有効なJWTトークンCustomerIdを含むが存在する(int id) {
        token = jwtUtil.generateToken(id, "test@example.com");
    }

    @もし("JwtUtil.getCustomerIdFromToken\\(token\\)を呼び出す")
    public void jwtUtilGetCustomerIdFromTokenTokenを呼び出す() {
        customerId = jwtUtil.getCustomerIdFromToken(token);
    }

    @ならば("{int}が返される")
    public void が返される(int expected) {
        assertEquals(expected, customerId);
    }
}
