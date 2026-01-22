package at.htlle.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionAccountResolver {

    public Long getAccountId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("accountId");
        if (value instanceof Long accountId) {
            return accountId;
        }
        if (value instanceof String accountIdText) {
            try {
                return Long.parseLong(accountIdText);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public void setAccountId(HttpServletRequest request, Long accountId) {
        request.getSession(true).setAttribute("accountId", accountId);
    }

    public void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
