package pro.kensait.backoffice.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT ユーティリティクラス
 * 社員認証用のJWTトークン生成・検証を行う
 */
@ApplicationScoped
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Inject
    @ConfigProperty(name = "jwt.secret-key")
    private String secretKeyString;

    @Inject
    @ConfigProperty(name = "jwt.expiration-ms")
    private Long expirationMs;

    @Inject
    @ConfigProperty(name = "jwt.cookie-name")
    private String cookieName;

    /**
     * JWT生成
     * @param employeeId 社員ID
     * @param employeeCode 社員コード
     * @param departmentId 部署ID
     * @return JWT文字列
     */
    public String generateToken(Long employeeId, String employeeCode, Long departmentId) {
        logger.info("[ JwtUtil#generateToken ] employeeId: {}, employeeCode: {}", employeeId, employeeCode);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());

        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeCode", employeeCode)
                .claim("departmentId", departmentId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * JWTからClaimsを取得（検証込み）
     * @param token JWT文字列
     * @return Claims
     */
    public Claims getClaims(String token) {
        logger.debug("[ JwtUtil#getClaims ]");

        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWTから社員IDを取得
     * @param token JWT文字列
     * @return 社員ID
     */
    public Long getEmployeeIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * JWTから社員コードを取得
     * @param token JWT文字列
     * @return 社員コード
     */
    public String getEmployeeCodeFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("employeeCode", String.class);
    }

    /**
     * JWTから部署IDを取得
     * @param token JWT文字列
     * @return 部署ID
     */
    public Long getDepartmentIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object deptId = claims.get("departmentId");
        if (deptId == null) {
            return null;
        }
        if (deptId instanceof Integer) {
            return ((Integer) deptId).longValue();
        }
        return (Long) deptId;
    }

    /**
     * JWT検証
     * @param token JWT文字列
     * @return 有効な場合true
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("[ JwtUtil#validateToken ] Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HttpServletRequestからJWTを抽出
     * @param request HTTPリクエスト
     * @return JWT文字列（見つからない場合null）
     */
    public String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Cookie名を取得
     * @return Cookie名
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * 有効期限（秒）を取得
     * @return 有効期限（秒）
     */
    public int getExpirationSeconds() {
        return (int) (expirationMs / 1000);
    }
}

