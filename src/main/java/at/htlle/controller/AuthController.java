package at.htlle.controller;

import at.htlle.dto.RegistrationRequest;
import at.htlle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Renders login and registration pages.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the login page and status messages.
     *
     * @param error optional error flag
     * @param logout optional logout flag
     * @param registered optional registration flag
     * @param model view model
     * @return view name
     */
    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) String error,
                        @RequestParam(name = "logout", required = false) String logout,
                        @RequestParam(name = "registered", required = false) String registered,
                        Model model) {
        model.addAttribute("loginError", error != null);
        model.addAttribute("loggedOut", logout != null);
        model.addAttribute("registered", registered != null);
        return "login";
    }

    /**
     * Displays the registration form.
     *
     * @param model view model
     * @return view name
     */
    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new RegistrationRequest("", "", "", "", "", "", ""));
        }
        return "register";
    }

    /**
     * Handles registration form submission.
     *
     * @param request registration payload
     * @param bindingResult validation result
     * @param model view model
     * @return view or redirect
     */
    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registration") RegistrationRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (!request.password().equals(request.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.registerCustomer(request);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registration.failed", ex.getMessage());
            return "register";
        } catch (Exception ex) {
            model.addAttribute("registrationError", "Registration failed. Please try again.");
            return "register";
        }
        return "redirect:/login?registered";
    }
}
