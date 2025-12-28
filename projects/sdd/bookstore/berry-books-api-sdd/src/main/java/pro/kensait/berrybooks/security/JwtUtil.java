package pro.kensait.berrybooks.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * JWT生成・検証ユーティリティ
 */
@ApplicationScoped
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @ConfigProperty(name = "jwt.secret-key", defaultValue = "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore")
    private String secretKey;
    
    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        // 秘密鍵を生成（最低32文字必要）
        String keyToUse = (secretKey != null && !secretKey.isBlank()) 
            ? secretKey 
            : "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore";
        this.key = Keys.hmacShaKeyFor(keyToUse.getBytes(StandardCharsets.UTF_8));
        
        // 有効期限のデフォルト値設定
        if (expirationMs == null) {
            expirationMs = 86400000L; // 24 hours
        }
        
        logger.info("[ JwtUtil#init ] JWT secret key initialized, expiration: {} ms", expirationMs);
    }
    
    /**
     * JWTトークンを生成
     * 
     * @param customerId 顧客ID
     * @param email メールアドレス
     * @return JWT token
     */
    public String generateToken(Integer customerId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        
        String token = Jwts.builder()
                .subject(email)
                .claim("customerId", customerId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        logger.debug("[ JwtUtil#generateToken ] Token generated for customerId={}", customerId);
        
        return token;
    }
    
    /**
     * JWTトークンを検証
     * 
     * @param token JWT token
     * @return 有効な場合true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("[ JwtUtil#validateToken ] Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("[ JwtUtil#validateToken ] Malformed token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.warn("[ JwtUtil#validateToken ] Invalid signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("[ JwtUtil#validateToken ] Token validation failed", e);
            return false;
        }
    }
    
    /**
     * トークンから顧客IDを取得
     * 
     * @param token JWT token
     * @return 顧客ID
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
     * トークンからメールアドレスを取得
     * 
     * @param token JWT token
     * @return メールアドレス
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
}
