package api;

import clients.UserClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserUpdateTest {

    private String uniqueEmail;
    private final String password = "password";
    private String name = "TestUser";
    private String newEmail;
    private String newName;
    private String accessToken;
    private UserClient userClient;

    @Before
    public void setUp() {
        uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        userClient = new UserClient();

        Response regResponse = userClient.createUser(uniqueEmail, password, name);
        accessToken = regResponse.jsonPath().getString("accessToken");
        newEmail = "updated" + System.currentTimeMillis() + "@example.com";
        newName = "UpdatedUser";
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }

        accessToken = null;
        userClient = null;
    }

    @Test
    @Step("Обновление пользователя с авторизацией")
    public void testUpdateUserWithAuthorization() {
        java.util.Map<String, String> updates = new java.util.HashMap<>();
        updates.put("email", newEmail);
        updates.put("name", newName);

        Response updateResponse = userClient.updateUser(updates, accessToken);
        assertEquals("Ожидается, что обновление будет успешным", 200, updateResponse.statusCode());
        assertEquals("Email не обновлён", newEmail, updateResponse.jsonPath().getString("user.email"));
        assertEquals("Имя не обновлено", newName, updateResponse.jsonPath().getString("user.name"));
    }

    @Test
    @Step("Попытка обновления пользователя без авторизации")
    public void testUpdateUserWithoutAuthorization() {
        java.util.Map<String, String> updates = new java.util.HashMap<>();
        updates.put("email", newEmail);
        updates.put("name", newName);

        Response updateResponse = userClient.updateUser(updates, "");
        assertEquals("Ожидается код 401 при обновлении без токена", 401, updateResponse.statusCode());
        String errorMessage = updateResponse.jsonPath().getString("message");
        assertEquals("Сообщение об ошибке не соответствует", "You should be authorised", errorMessage);
    }
}
