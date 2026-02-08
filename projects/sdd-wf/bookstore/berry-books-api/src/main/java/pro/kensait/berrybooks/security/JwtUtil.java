package pro.kensait.berrybooks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    @ConfigProperty(name = "jwt.secret-key", defaultValue = "default-secret-key-for-development-only-at-least-32-characters")
    private String secretKey;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;
    
    @Inject
    @ConfigProperty(name = "jwt.cookie-name", defaultValue = "berry-books-jwt")
    private String cookieName;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.warn("jwt.secret-key is not configured, using default value.");
            secretKey = "default-secret-key-for-development-only-at-least-32-characters";
        }
        if (expirationMs == null) {
            logger.warn("jwt.expiration-ms is not configured, using default value.");
            expirationMs = 86400000L; // 24 hours
        }
        
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT secret key initialized, expiration: {} ms", expirationMs);
    }
    
    /**
     * JWTトークンを生成する
     * 
     * @param customerId 顧客ID
     * @param customerName 顧客名
     * @return JWT文字列
     */
    public String generateToken(Integer customerId, String customerName) {
        logger.info("[ JwtUtil#generateToken ] customerId={}, customerName={}", customerId, customerName);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .subject(String.valueOf(customerId))
                .claim("customerId", customerId)
                .claim("customerName", customerName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * JWTトークンを検証する
     * 
     * @param token JWT文字列
     * @return クレーム
     * @throws JwtException トークンが無効な場合
     */
    public Claims validateToken(String token) throws JwtException {
        logger.debug("[ JwtUtil#validateToken ] Validating token");
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            logger.debug("[ JwtUtil#validateToken ] Token validated successfully");
            return claims;
        } catch (JwtException e) {
            logger.warn("[ JwtUtil#validateToken ] Token validation failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * トークンからcustomerIdを抽出する
     * 
     * @param token JWT文字列
     * @return 顧客ID
     */
    public Integer extractCustomerId(String token) {
        Claims claims = validateToken(token);
        return claims.get("customerId", Integer.class);
    }
    
    /**
     * トークンからcustomerNameを抽出する
     * 
     * @param token JWT文字列
     * @return 顧客名
     */
    public String extractCustomerName(String token) {
        Claims claims = validateToken(token);
        return claims.get("customerName", String.class);
    }
    
    /**
     * HttpServletRequestからJWTトークンを抽出する
     * 
     * @param request HTTPリクエスト
     * @return JWT文字列（存在しない場合はnull）
     */
    public String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    logger.debug("[ JwtUtil#extractJwtFromRequest ] JWT found in cookie");
                    return cookie.getValue();
                }
            }
        }
        logger.debug("[ JwtUtil#extractJwtFromRequest ] JWT not found in cookie");
        return null;
    }
    
    public String getCookieName() {
        return cookieName;
    }
}