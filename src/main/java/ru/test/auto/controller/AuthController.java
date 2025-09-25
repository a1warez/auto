package ru.test.auto.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.test.auto.model.User;
import ru.test.auto.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Thymeleaf шаблон для входа
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Thymeleaf шаблон для регистрации
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerNewUser(user, "ROLE_USER");
            redirectAttributes.addFlashAttribute("message", "Регистрация прошла успешно! Войдите в систему.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
    // Если вам нужно что-то обрабатывать при успешном входе (редко нужно, обычно перенаправление работает)
    // @GetMapping("/login-success")
    // public String loginSuccess() {
    //     return "redirect:/"; // Перенаправление на главную после успешного входа
    // }
//}