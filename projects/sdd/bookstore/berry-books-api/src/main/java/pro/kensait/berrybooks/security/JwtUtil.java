package pro.kensait.berrybooks.security;

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

    // JWT生成
    public String generateToken(Integer customerId, String email) {
        logger.info("[ JwtUtil#generateToken ] customerId: {}, email: {}", customerId, email);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());

        return Jwts.builder()
                .subject(String.valueOf(customerId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // JWTからClaimsを取得（検証込み）
    public Claims getClaims(String token) {
        logger.debug("[ JwtUtil#getClaims ]");

        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // JWTから顧客IDを取得
    public Integer getCustomerIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Integer.valueOf(claims.getSubject());
    }

    // JWTからメールアドレスを取得
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    // JWT検証
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("[ JwtUtil#validateToken ] Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // HttpServletRequestからJWTを抽出
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

    // Cookie名を取得
    public String getCookieName() {
        return cookieName;
    }

    // 有効期限（秒）を取得
    public int getExpirationSeconds() {
        return (int) (expirationMs / 1000);
    }
}

