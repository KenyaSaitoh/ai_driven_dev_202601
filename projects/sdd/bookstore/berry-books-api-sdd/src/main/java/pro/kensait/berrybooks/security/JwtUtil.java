package pro.kensait.berrybooks.security;

import io.jsonwebtoken.Claims;
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
 * JwtUtil - JWT生成・検証ユーティリティ
 * 
 * 責務:
 * - JWT生成・検証
 * - Cookie管理
 * - ライブラリ: jjwt 0.12.6
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 */
@ApplicationScoped
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Inject
    @ConfigProperty(name = "jwt.secret-key")
    private String secretKey;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration-ms")
    private Long expirationMs;
    
    @Inject
    @ConfigProperty(name = "jwt.cookie-name")
    private String cookieName;
    
    private SecretKey key;
    
    /**
     * JWT秘密鍵の初期化
     * 
     * 処理:
     * 1. secretKeyがnullまたは空の場合、デフォルト値を設定
     * 2. expirationMsがnullの場合、デフォルト値（86400000L = 24時間）を設定
     * 3. Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)) - 秘密鍵生成
     * 4. 初期化完了ログ出力
     */
    @PostConstruct
    public void init() {
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
     * JWTトークン生成
     * 
     * 処理:
     * 1. 現在時刻（now）と有効期限（expiration）を計算
     * 2. Jwts.builder()でJWTを構築:
     *   - subject: customerIdの文字列表現
     *   - claim("email", email): メールアドレスをクレームに追加
     *   - issuedAt(now): 発行時刻
     *   - expiration(expiration): 有効期限
     *   - signWith(key): HMAC-SHA256で署名
     * 3. JWT文字列を返却
     * 
     * @param customerId 顧客ID
     * @param email メールアドレス
     * @return JWT文字列
     */
    public String generateToken(Long customerId, String email) {
        logger.debug("[ JwtUtil#generateToken ] Generating token for customerId={}, email={}", customerId, email);
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        
        String token = Jwts.builder()
                .subject(String.valueOf(customerId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
        
        logger.debug("[ JwtUtil#generateToken ] Token generated successfully");
        return token;
    }
    
    /**
     * JWTトークンの検証
     * 
     * 処理:
     * 1. Jwts.parser().verifyWith(key).build() - パーサー構築
     * 2. parseSignedClaims(token) - 署名検証とクレーム解析
     * 3. 成功時: true を返却
     * 4. 例外発生時（署名不正、期限切れ等）: false を返却
     * 
     * @param token JWTトークン
     * @return 検証成功=true、失敗=false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("[ JwtUtil#validateToken ] Invalid token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWTからユーザーIDを取得
     * 
     * 処理:
     * 1. Jwts.parser().verifyWith(key).build() - パーサー構築
     * 2. parseSignedClaims(token).getPayload().getSubject() - subject取得
     * 3. Long.parseLong(subject) - 顧客IDに変換
     * 4. 顧客IDを返却
     * 
     * @param token JWTトークン
     * @return 顧客ID（Long）
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * JWTからメールアドレスを取得
     * 
     * 処理:
     * 1. Jwts.parser().verifyWith(key).build() - パーサー構築
     * 2. parseSignedClaims(token).getPayload().get("email", String.class) - emailクレーム取得
     * 3. メールアドレスを返却
     * 
     * @param token JWTトークン
     * @return メールアドレス（String）
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("email", String.class);
    }
    
    /**
     * HTTPリクエストからJWT Cookieを抽出
     * 
     * 処理:
     * 1. request.getCookies() - Cookieの配列を取得
     * 2. Cookieがnullの場合、nullを返却
     * 3. Cookie配列をループし、Cookie名がcookieNameと一致するものを検索
     * 4. 一致するCookieが見つかった場合、その値を返却
     * 5. 見つからない場合、nullを返却
     * 
     * @param request HTTPリクエスト
     * @return JWT文字列（Cookie未設定の場合はnull）
     */
    public String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Cookie名を取得
     * 
     * @return Cookie名
     */
    public String getCookieName() {
        return cookieName;
    }
}
