package pro.kensait.berrybooks.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * JWT（JSON Web Token）生成・検証ユーティリティ
 * 
 * トークン生成、検証、クレーム抽出機能を提供する。
 */
@ApplicationScoped
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @ConfigProperty(name = "jwt.secret-key", defaultValue = "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore")
    private String secretKey;

    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;

    private SecretKey key;

    /**
     * 初期化処理（@PostConstructで実行）
     * 
     * 設定値のnullチェックとフォールバック処理を行う。
     */
    @PostConstruct
    public void init() {
        // @ConfigPropertyが失敗した場合のフォールバック
        if (secretKey == null || secretKey.isEmpty()) {
            logger.warn("jwt.secret-key is not configured, using default value.");
            secretKey = "BerryBooksSecretKeyForJWT2024MustBe32CharactersOrMore";
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
     * @param email メールアドレス
     * @return JWT トークン文字列
     */
    public String generateToken(Integer customerId, String email) {
        logger.info("[ JwtUtil#generateToken ] customerId={}, email={}", customerId, email);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(String.valueOf(customerId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        logger.info("[ JwtUtil#generateToken ] Token generated, expires at: {}", expiryDate);
        return token;
    }

    /**
     * JWTトークンを検証する
     * 
     * @param token JWTトークン
     * @return 有効な場合true、無効な場合false
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
     * JWTトークンから顧客IDを取得する
     * 
     * @param token JWTトークン
     * @return 顧客ID
     */
    public Integer getCustomerIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }

    /**
     * JWTトークンからメールアドレスを取得する
     * 
     * @param token JWTトークン
     * @return メールアドレス
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
