package ban.koreamarkers.domain.service;

import ban.koreamarkers.common.DuplicateEmailException;
import ban.koreamarkers.common.DuplicateUserNameException;
import ban.koreamarkers.controller.dto.SignupRequest;
import ban.koreamarkers.domain.User;
import ban.koreamarkers.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("signup 성공: ban / bbgiloo@gmail.com 회원가입 성공")
    void signup_ShouldBeSuccess() {
        // given
        SignupRequest req = new SignupRequest(
                "ban",
                "1q2w3e",
                "bbgiloo@gmail.com",
                "반길현"
        );

        when(userRepository.existsByUsername("ban")).thenReturn(false);
        when(userRepository.existsByEmail("bbgiloo@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("1q2w3e")).thenReturn("ENC(1q2w3e)");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User saved = userService.signup(req);

        // then
        assertThat(saved.getUsername()).isEqualTo("ban");
        assertThat(saved.getEmail()).isEqualTo("bbgiloo@gmail.com");
        assertThat(saved.getName()).isEqualTo("반길현");
        assertThat(saved.getPassword()).isEqualTo("ENC(1q2w3e)");
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);
        assertThat(saved.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("signup 실패: 아이디 ban 중복")
    void signup_ShouldDuplicateUsername_throwException() {
        // given
        SignupRequest req = new SignupRequest(
                "ban",
                "1q2w3e",
                "bbgiloo@gmail.com",
                "반길현"
        );

        when(userRepository.existsByUsername("ban")).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(DuplicateUserNameException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");

        verify(userRepository).existsByUsername("ban");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("signup 실패: 이메일 bbgiloo@gmail.com 중복")
    void signup_ShouldDuplicateEmail_throwException() {
        // given
        SignupRequest req = new SignupRequest(
                "ban",
                "1q2w3e",
                "bbgiloo@gmail.com",
                "반길현"
        );

        when(userRepository.existsByUsername("ban")).thenReturn(false);
        when(userRepository.existsByEmail("bbgiloo@gmail.com")).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository).existsByUsername("ban");
        verify(userRepository).existsByEmail("bbgiloo@gmail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("signup 저장 User 필드 검증 (ban / 반길현)")
    void signup_capture_savedUser_fields() {
        // given
        SignupRequest req = new SignupRequest(
                "ban",
                "1q2w3e",
                "bbgiloo@gmail.com",
                "반길현"
        );

        when(userRepository.existsByUsername("ban")).thenReturn(false);
        when(userRepository.existsByEmail("bbgiloo@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("1q2w3e")).thenReturn("ENC(1q2w3e)");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.signup(req);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User user = captor.getValue();
        assertThat(user.getUsername()).isEqualTo("ban");
        assertThat(user.getEmail()).isEqualTo("bbgiloo@gmail.com");
        assertThat(user.getName()).isEqualTo("반길현");
        assertThat(user.getPassword()).isEqualTo("ENC(1q2w3e)");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        assertThat(user.getEnabled()).isTrue();
    }


}