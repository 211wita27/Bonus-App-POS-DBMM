package at.htlle.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Redirects users to the correct dashboard based on their role.
 */
@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Redirects to the appropriate landing page after login.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authentication authentication result
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String target = "/customer/dashboard";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                target = "/admin/dashboard";
                break;
            }
            if ("ROLE_RESTAURANT".equals(authority.getAuthority())) {
                target = "/restaurant/dashboard";
                break;
            }
        }
        response.sendRedirect(target);
    }
}
