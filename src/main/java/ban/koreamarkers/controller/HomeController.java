package ban.koreamarkers.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        // 쿠키에서 토큰 정보 가져오기
        String accessToken = null;
        String refreshToken = null;
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 현재 인증된 사용자 정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null && authentication.isAuthenticated() 
            ? authentication.getName() : null;

        model.addAttribute("hasAccessToken", accessToken != null);
        model.addAttribute("hasRefreshToken", refreshToken != null);
        model.addAttribute("username", username);
        
        // 토큰 미리보기 (보안상 일부만 표시)
        if (accessToken != null) {
            model.addAttribute("accessTokenPreview", 
                accessToken.length() > 50 ? accessToken.substring(0, 50) + "..." : accessToken);
        }
        if (refreshToken != null) {
            model.addAttribute("refreshTokenPreview", 
                refreshToken.length() > 50 ? refreshToken.substring(0, 50) + "..." : refreshToken);
        }

        return "home";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }
        return "login";
    }
}
