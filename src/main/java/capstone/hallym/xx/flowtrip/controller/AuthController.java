package capstone.hallym.xx.flowtrip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.dto.SignupRequestDto;
import capstone.hallym.xx.flowtrip.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupRequestDto", new SignupRequestDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupRequestDto dto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userService.signup(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("signupError", e.getMessage());
            return "signup";
        }

        return "redirect:/login?signupSuccess=true";
    }
}