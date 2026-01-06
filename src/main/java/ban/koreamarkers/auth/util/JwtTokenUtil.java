package ban.koreamarkers.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationMustBeAtLeast256Bits}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}") // 15분 (밀리초)
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7일 (밀리초)
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // AccessToken 생성
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ACCESS");
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    // RefreshToken 생성
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    // 토큰 생성
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 토큰에서 사용자명 추출
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 토큰에서 만료일 추출
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 토큰에서 클레임 추출
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 토큰에서 모든 클레임 추출
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰 만료 확인
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 토큰 검증
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // AccessToken인지 확인
    public Boolean isAccessToken(String token) {
        try {
            String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
            return "ACCESS".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // RefreshToken인지 확인
    public Boolean isRefreshToken(String token) {
        try {
            String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
            return "REFRESH".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // AccessToken 만료 시간 반환 (밀리초)
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // RefreshToken 만료 시간 반환 (밀리초)
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
