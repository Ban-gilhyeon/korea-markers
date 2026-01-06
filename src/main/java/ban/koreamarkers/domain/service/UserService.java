package ban.koreamarkers.domain.service;

import ban.koreamarkers.common.DuplicateEmailException;
import ban.koreamarkers.common.DuplicateUserNameException;
import ban.koreamarkers.controller.dto.SignupRequest;
import ban.koreamarkers.domain.User;
import ban.koreamarkers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(SignupRequest signupRequest) {
        // 아이디 중복 확인
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateUserNameException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .email(signupRequest.getEmail())
                .name(signupRequest.getName())
                .role(User.Role.USER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }
}
