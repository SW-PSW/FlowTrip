package capstone.hallym.xx.flowtrip.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.dto.SignupRequestDto;
import capstone.hallym.xx.flowtrip.entity.AppUser;
import capstone.hallym.xx.flowtrip.repository.UserRepository;
import capstone.hallym.xx.flowtrip.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public Map<String, Object> signupApi(@RequestBody SignupRequestDto dto) {
        try {
            userService.signup(dto);

            return Map.of(
                    "success", true,
                    "message", "회원가입이 완료되었습니다."
            );

        } catch (IllegalArgumentException e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "회원가입 중 오류가 발생했습니다."
            );
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return Map.of(
                    "authenticated", false
            );
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElse(null);
        String nickname = user == null || user.getNickname() == null || user.getNickname().isBlank()
                ? authentication.getName()
                : user.getNickname();

        return Map.of(
                "authenticated", true,
                "username", authentication.getName(),
                "nickname", nickname
        );
    }
}
