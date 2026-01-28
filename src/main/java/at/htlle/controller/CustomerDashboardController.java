package at.htlle.controller;

import at.htlle.dto.AccountResponse;
import at.htlle.entity.AppUser;
import at.htlle.entity.LoyaltyAccount;
import at.htlle.service.AccountService;
import at.htlle.service.CurrentUserService;
import java.util.Comparator;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomerDashboardController {

    private static final String SELECTED_ACCOUNT_SESSION_KEY = "selectedAccountId";

    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    public CustomerDashboardController(AccountService accountService, CurrentUserService currentUserService) {
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/customer/dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String dashboard(@RequestParam(name = "accountId", required = false) Long accountId,
                            HttpSession session,
                            Model model) {
        AppUser user = currentUserService.getCurrentUser();
        if (user.getCustomer() == null) {
            model.addAttribute("errorMessage", "No customer profile linked to this account.");
            return "dashboard";
        }
        List<LoyaltyAccount> accounts = accountService.getAccountsForCustomer(user.getCustomer().getId());
        if (accounts.isEmpty()) {
            model.addAttribute("errorMessage", "No loyalty accounts found.");
            return "dashboard";
        }

        LoyaltyAccount selected = resolveSelectedAccount(accounts, accountId, session);
        AccountResponse response = accountService.buildAccountResponse(selected, true);
        model.addAttribute("accounts", accounts);
        model.addAttribute("account", response);
        model.addAttribute("accountId", selected.getId());
        return "dashboard";
    }

    private LoyaltyAccount resolveSelectedAccount(List<LoyaltyAccount> accounts, Long accountId, HttpSession session) {
        // Persist selection across page switches via session.
        Long resolvedId = accountId;
        if (resolvedId == null && session != null) {
            Object stored = session.getAttribute(SELECTED_ACCOUNT_SESSION_KEY);
            if (stored instanceof Long) {
                resolvedId = (Long) stored;
            } else if (stored instanceof String storedValue) {
                try {
                    resolvedId = Long.valueOf(storedValue);
                } catch (NumberFormatException ignored) {
                    resolvedId = null;
                }
            }
        }
        LoyaltyAccount selected = selectAccount(accounts, resolvedId);
        if (session != null) {
            session.setAttribute(SELECTED_ACCOUNT_SESSION_KEY, selected.getId());
        }
        return selected;
    }

    private LoyaltyAccount selectAccount(List<LoyaltyAccount> accounts, Long accountId) {
        if (accountId != null) {
            for (LoyaltyAccount account : accounts) {
                if (account.getId().equals(accountId)) {
                    return account;
                }
            }
        }
        return accounts.stream()
                .min(Comparator.comparing(LoyaltyAccount::getId))
                .orElseThrow();
    }
}
