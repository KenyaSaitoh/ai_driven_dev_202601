package pro.kensait.backoffice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
 * HMAC-SHA256アルゴリズムによるJWTの生成と検証を提供する
 */
@ApplicationScoped
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Inject
    @ConfigProperty(name = "jwt.secret-key", defaultValue = "BackOfficeSecretKeyForJWT2024MustBe32CharactersOrMore")
    private String secretKey;

    @Inject
    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;

    @Inject
    @ConfigProperty(name = "jwt.cookie-name", defaultValue = "back-office-jwt")
    private String cookieName;

    /**
     * JWTトークンを生成する
     * 
     * @param employeeId 社員ID
     * @param employeeCode 社員コード
     * @param departmentId 部署ID
     * @return JWT文字列
     */
    public String generateToken(Long employeeId, String employeeCode, Long departmentId) {
        logger.debug("Generating JWT for employeeId: {}, employeeCode: {}", employeeId, employeeCode);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeCode", employeeCode)
                .claim("departmentId", departmentId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        logger.debug("JWT generated successfully");
        return token;
    }

    /**
     * JWTトークンを検証する
     * 
     * @param token JWT文字列
     * @return 検証結果（true: 有効、false: 無効）
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            logger.debug("JWT validation successful");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWTトークンからクレームを取得する
     * 
     * @param token JWT文字列
     * @return クレーム情報
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Failed to get claims from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * JWTトークンから社員IDを取得する
     * 
     * @param token JWT文字列
     * @return 社員ID
     */
    public Long getEmployeeIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * JWTトークンから社員コードを取得する
     * 
     * @param token JWT文字列
     * @return 社員コード
     */
    public String getEmployeeCodeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("employeeCode", String.class);
    }

    /**
     * JWTトークンから部署IDを取得する
     * 
     * @param token JWT文字列
     * @return 部署ID
     */
    public Long getDepartmentIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object deptId = claims.get("departmentId");
        if (deptId instanceof Integer) {
            return ((Integer) deptId).longValue();
        } else if (deptId instanceof Long) {
            return (Long) deptId;
        }
        return null;
    }

    /**
     * Cookie名を取得する
     * 
     * @return Cookie名
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * JWT有効期限（秒）を取得する
     * 
     * @return 有効期限（秒）
     */
    public int getExpirationSeconds() {
        return (int) (expirationMs / 1000);
    }
}
