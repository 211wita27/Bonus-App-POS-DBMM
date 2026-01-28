package at.htlle.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndToEndUiFlowTest {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setRedirectEnabled(true);
    }

    @AfterEach
    void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    void fullUiFlowCoversAdminRestaurantAndCustomer() {
        login("admin@admin", "admin");
        HtmlPage dashboard = waitForPath("/admin/dashboard");
        assertThat(dashboard.asNormalizedText()).contains("Operations Dashboard");

        verifyRestaurantSearchOnDashboard(dashboard);
        HtmlPage restaurantsPage = clickFirstRestaurantCard(dashboard);
        assertThat(restaurantsPage.getUrl().toString()).contains("/admin/restaurants#restaurant-");

        HtmlPage ledgerPage = getPage("/admin/ledger");
        verifyPointRateSearch(ledgerPage);
        logout();

        login("restaurant@restaurant", "restaurant");
        HtmlPage restaurantPage = waitForPath("/restaurant/dashboard");
        assertThat(restaurantPage.asNormalizedText()).contains("Restaurant Portal");
        restaurantPage = createRestaurantPurchase(restaurantPage);
        assertThat(restaurantPage.asNormalizedText()).contains("Purchase recorded");
        logout();

        login("user@user", "user");
        HtmlPage customerDashboard = waitForPath("/customer/dashboard");
        assertThat(customerDashboard.asNormalizedText()).contains("Customer Dashboard");
        HtmlPage redemption = redeemAvailableReward(customerDashboard);
        assertThat(redemption.asNormalizedText()).contains("Reward Redeemed");
    }

    private void login(String username, String password) {
        HtmlPage loginPage = getPage("/login");
        HtmlForm form = loginPage.getForms().get(0);
        HtmlInput userInput = form.getInputByName("username");
        HtmlInput passInput = form.getInputByName("password");
        userInput.setValueAttribute(username);
        passInput.setValueAttribute(password);
        HtmlButton submit = form.getFirstByXPath(".//button[@type='submit']");
        click(submit);
        waitForJs();
    }

    private void logout() {
        HtmlPage currentPage = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
        if (currentPage == null) {
            currentPage = getPage("/");
        }
        HtmlForm form = currentPage.getFirstByXPath("//form[@action='/logout']");
        if (form == null) {
            currentPage = getPage("/customer/dashboard");
            form = currentPage.getFirstByXPath("//form[@action='/logout']");
        }
        HtmlButton button = form.getFirstByXPath(".//button[@type='submit']");
        click(button);
        waitForPath("/login");
    }

    private void verifyRestaurantSearchOnDashboard(HtmlPage dashboard) {
        HtmlInput search = dashboard.getFirstByXPath("//input[@data-search-input and @placeholder='Search restaurants']");
        List<DomElement> rows = dashboard.getByXPath("//*[contains(@class,'restaurant-row')]");
        int total = rows.size();
        search.type("Sushi");
        waitForJs();
        long visible = rows.stream().filter(this::isVisible).count();
        assertThat(visible).isGreaterThan(0);
        assertThat(visible).isLessThanOrEqualTo(total);
    }

    private HtmlPage clickFirstRestaurantCard(HtmlPage dashboard) {
        HtmlAnchor row = dashboard.getFirstByXPath("//*[contains(@class,'restaurant-row')]");
        HtmlPage next = click(row);
        waitForJs();
        return next;
    }

    private void verifyPointRateSearch(HtmlPage ledgerPage) {
        HtmlInput search = ledgerPage.getFirstByXPath("//input[@data-search-input and @placeholder='Search restaurants']");
        List<DomElement> cards = ledgerPage.getByXPath("//*[contains(@class,'rate-card')]");
        int total = cards.size();
        search.type("Bistro");
        waitForJs();
        long visible = cards.stream().filter(this::isVisible).count();
        assertThat(visible).isGreaterThan(0);
        assertThat(visible).isLessThanOrEqualTo(total);
    }

    private HtmlPage createRestaurantPurchase(HtmlPage restaurantPage) {
        HtmlForm form = restaurantPage.getFirstByXPath("//form[@action='/restaurant/purchases']");
        HtmlSelect account = form.getSelectByName("accountId");
        account.setSelectedIndex(1);
        HtmlInput amount = form.getInputByName("totalAmount");
        amount.setValueAttribute("19.90");
        HtmlInput currency = form.getInputByName("currency");
        currency.setValueAttribute("EUR");
        HtmlButton submit = form.getFirstByXPath(".//button[@type='submit']");
        HtmlPage next = click(submit);
        waitForJs();
        return next;
    }

    private HtmlPage redeemAvailableReward(HtmlPage customerDashboard) {
        HtmlAnchor rewardsLink = customerDashboard.getFirstByXPath("//a[@href='/customer/rewards']");
        HtmlPage rewardsPage = click(rewardsLink);
        waitForJs();
        List<DomElement> rewardCards = rewardsPage.getByXPath("//*[@data-reward-card]");
        HtmlButton redeemButton = null;
        for (DomElement card : rewardCards) {
            HtmlButton button = card.getFirstByXPath(".//button[@type='submit']");
            if (button.getAttribute("disabled") == DomElement.ATTRIBUTE_NOT_DEFINED) {
                redeemButton = button;
                HtmlInput notes = card.getFirstByXPath(".//input[@name='notes']");
                notes.setValueAttribute("Demo redemption");
                break;
            }
        }
        assertThat(redeemButton).isNotNull();
        HtmlPage redemption = click(redeemButton);
        waitForJs();
        return redemption;
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private HtmlPage waitForPath(String path) {
        HtmlPage page = webClient.getCurrentWindow().getEnclosedPage();
        if (page == null || !page.getUrl().toString().contains(path)) {
            page = getPage(path);
        }
        return page;
    }

    private HtmlPage getPage(String path) {
        try {
            return webClient.getPage(baseUrl() + path);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load page " + path, ex);
        }
    }

    private void waitForJs() {
        webClient.waitForBackgroundJavaScript(500);
    }

    private boolean isVisible(DomElement element) {
        String style = element.getAttribute("style");
        if (style == null || DomElement.ATTRIBUTE_NOT_DEFINED.equals(style)) {
            return true;
        }
        return !style.contains("display: none");
    }

    private HtmlPage click(HtmlElement element) {
        try {
            return element.click();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to click element", ex);
        }
    }
}
