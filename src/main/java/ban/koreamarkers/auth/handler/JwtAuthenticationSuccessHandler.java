package ban.koreamarkers.auth.handler;

import ban.koreamarkers.auth.util.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.cookie.max-age:604800}") // 기본 7일 (초)
    private int cookieMaxAge;

    @Value("${jwt.cookie.secure:false}") // 개발 환경에서는 false, 프로덕션에서는 true
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // AccessToken과 RefreshToken 생성
        String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        // AccessToken을 쿠키에 저장 (HttpOnly=false, 클라이언트에서 접근 가능)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(false); // JavaScript에서 접근 가능 (필요시)
        accessTokenCookie.setSecure(cookieSecure); // 설정에 따라 HTTPS에서만 전송
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenUtil.getAccessTokenExpiration() / 1000)); // 초 단위
        response.addCookie(accessTokenCookie);

        // RefreshToken을 HttpOnly 쿠키에 저장 (XSS 공격 방지)
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // JavaScript에서 접근 불가 (보안 강화)
        refreshTokenCookie.setSecure(cookieSecure); // 설정에 따라 HTTPS에서만 전송
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenUtil.getRefreshTokenExpiration() / 1000)); // 초 단위
        response.addCookie(refreshTokenCookie);

        // 응답 헤더에도 AccessToken 추가 (API 호출용)
        response.setHeader("Authorization", "Bearer " + accessToken);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
