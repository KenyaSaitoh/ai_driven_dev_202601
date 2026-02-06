package pro.kensait.berrybooks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT生成・検証ユーティリティ
 * 
 * JWTトークンの生成、検証、Claims抽出を担当する。
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    private static final long DEFAULT_EXPIRATION_MS = 86400000L; // 24時間
    private static final String DEFAULT_SECRET_KEY = "your-secret-key-at-least-256-bits-long-for-hs256-algorithm";
    
    @Inject
    @ConfigProperty(name = "jwt.secret")
    private String secretKey;
    
    private SecretKey key;
    private long expirationMs;
    
    @PostConstruct
    public void init() {
        // フォールバック設定
        if (secretKey == null || secretKey.isEmpty()) {
            logger.warn("[ JwtUtil#init ] jwt.secret is not configured, using default value.");
            secretKey = DEFAULT_SECRET_KEY;
        }
        
        expirationMs = DEFAULT_EXPIRATION_MS;
        
        // HMAC-SHA256用の秘密鍵を生成
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        
        logger.info("[ JwtUtil#init ] Initialized with expirationMs={}", expirationMs);
    }
    
    /**
     * 顧客ID、メールアドレスからJWTトークンを生成する
     * 
     * @param customerId 顧客ID
     * @param email メールアドレス
     * @return JWTトークン文字列
     */
    public String generateToken(Integer customerId, String email) {
        logger.info("[ JwtUtil#generateToken ] customerId={}, email={}", customerId, email);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        String token = Jwts.builder()
                .subject(customerId.toString())
                .claim("customerId", customerId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        
        logger.info("[ JwtUtil#generateToken ] Token generated successfully");
        return token;
    }
    
    /**
     * JWTトークンの有効性を検証する
     * 
     * @param token JWTトークン
     * @return 有効な場合はtrue、無効な場合はfalse
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            logger.debug("[ JwtUtil#validateToken ] Token is valid");
            return true;
        } catch (Exception e) {
            logger.warn("[ JwtUtil#validateToken ] Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWTトークンから顧客IDを抽出する
     * 
     * @param token JWTトークン
     * @return 顧客ID
     */
    public Integer getCustomerIdFromToken(String token) {
        Claims claims = getClaims(token);
        Integer customerId = claims.get("customerId", Integer.class);
        logger.debug("[ JwtUtil#getCustomerIdFromToken ] customerId={}", customerId);
        return customerId;
    }
    
    /**
     * JWTトークンからメールアドレスを抽出する
     * 
     * @param token JWTトークン
     * @return メールアドレス
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        String email = claims.get("email", String.class);
        logger.debug("[ JwtUtil#getEmailFromToken ] email={}", email);
        return email;
    }
    
    /**
     * JWTトークンからClaimsを取得する
     * 
     * @param token JWTトークン
     * @return Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
