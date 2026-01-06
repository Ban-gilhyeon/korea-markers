package ban.koreamarkers.controller;

import ban.koreamarkers.auth.util.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 RefreshToken 가져오기
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "RefreshToken이 없습니다."));
        }

        try {
            // RefreshToken 검증
            if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "유효하지 않은 RefreshToken입니다."));
            }

            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "RefreshToken이 만료되었습니다."));
            }

            // 새로운 AccessToken 발급
            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);

            // 새로운 AccessToken을 쿠키에 저장
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(false);
            accessTokenCookie.setSecure(false); // 개발 환경용, 프로덕션에서는 true로 설정
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (jwtTokenUtil.getAccessTokenExpiration() / 1000));
            response.addCookie(accessTokenCookie);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("accessToken", newAccessToken);
            responseBody.put("message", "AccessToken이 갱신되었습니다.");

            // 응답 헤더에도 추가
            response.setHeader("Authorization", "Bearer " + newAccessToken);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰 갱신 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
