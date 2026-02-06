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
 */
@ApplicationScoped
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Inject
    @ConfigProperty(name = "jwt.secret", defaultValue = "your-secret-key-at-least-256-bits-long-for-hs256-algorithm-development-only")
    private String secretKey;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty() || secretKey.length() < 32) {
            logger.warn("[ JwtUtil#init ] jwt.secret is not properly configured, using default value (development only)");
            secretKey = "your-secret-key-at-least-256-bits-long-for-hs256-algorithm-development-only";
        }
        
        if (expirationMs == null) {
            logger.warn("[ JwtUtil#init ] jwt.expiration-ms is not configured, using default value: 86400000ms (24 hours)");
            expirationMs = 86400000L;
        }
        
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        logger.info("[ JwtUtil#init ] JWT secret key initialized, expiration: {} ms", expirationMs);
    }
    
    /**
     * JWTトークンを生成
     */
    public String generateToken(Integer customerId, String email) {
        logger.info("[ JwtUtil#generateToken ] Generating JWT token: customerId={}, email={}", customerId, email);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        String token = Jwts.builder()
                .claim("customerId", customerId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        
        logger.info("[ JwtUtil#generateToken ] JWT token generated successfully");
        return token;
    }
    
    /**
     * JWTトークンを検証
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("[ JwtUtil#validateToken ] Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWTトークンから顧客IDを抽出
     */
    public Integer getCustomerIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("customerId", Integer.class);
    }
    
    /**
     * JWTトークンからメールアドレスを抽出
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("email", String.class);
    }
}
