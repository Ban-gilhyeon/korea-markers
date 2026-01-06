package ban.koreamarkers.controller;

import ban.koreamarkers.controller.dto.SignupRequest;
import ban.koreamarkers.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SignupController {

    private final UserService userService;

    /**
     * 폼 바인딩용 기본 객체 제공
     * - record에는 기본 생성자가 없으므로 canonical constructor 사용
     * - GET /signup, POST validation 실패 시 모두 재사용됨
     */
    @ModelAttribute("signupRequest")
    public SignupRequest signupRequest() {
        return new SignupRequest();
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupRequest") SignupRequest signupRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        log.info("userSignUp Request={}", signupRequest);
        if (bindingResult.hasErrors()) {
            // validation 에러 시 forward → 에러 메시지 유지
            return "signup";
        }

        try {
            userService.signup(signupRequest);
            redirectAttributes.addFlashAttribute(
                    "message", "회원가입이 완료되었습니다. 로그인해주세요."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "회원가입 중 오류가 발생했습니다."
            );
            return "redirect:/signup";
        }
    }
}