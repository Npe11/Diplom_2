package api;

import clients.UserClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserLoginTest {

    private String uniqueEmail;
    private final String password = "password";
    private final String name = "TestUser";
    private String accessToken;
    private UserClient userClient;

    @Before
    public void setUp() {
        uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        userClient = new UserClient();
        Response regResponse = userClient.createUser(uniqueEmail, password, name);
        accessToken = regResponse.jsonPath().getString("accessToken");
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
    @Step("Логин существующего пользователя")
    public void testLoginExistingUser() {
        Response loginResponse = userClient.loginUser(uniqueEmail, password);

        assertEquals("Логин завершился с ошибочным статусом", 200, loginResponse.statusCode());
        assertTrue("Поле success должно быть true", loginResponse.jsonPath().getBoolean("success"));

        String newAccessToken = loginResponse.jsonPath().getString("accessToken");
        assertNotNull("AccessToken не должен быть null при успешном логине", newAccessToken);
        assertEquals("Email в ответе не соответствует", uniqueEmail, loginResponse.jsonPath().getString("user.email"));
    }

    @Test
    @Step("Логин с не правильными данными: email")
    public void testLoginWithInvalidEmail() {
        Response loginResponse = userClient.loginUser("wrongEmail@test.com", password);

        assertEquals("Ожидается статус 401 Unauthorized при неверном пароле", 401, loginResponse.statusCode());
        String errorMessage = loginResponse.jsonPath().getString("message");
        assertEquals("Сообщение об ошибке не соответствует", "email or password are incorrect", errorMessage);
    }

    @Test
    @Step("Логин с не правильными данными: password")
    public void testLoginWithInvalidPassword() {
        Response loginResponse = userClient.loginUser(uniqueEmail, "wrongPassword");

        assertEquals("Ожидается статус 401 Unauthorized при неверном пароле", 401, loginResponse.statusCode());
        String errorMessage = loginResponse.jsonPath().getString("message");
        assertEquals("Сообщение об ошибке не соответствует", "email or password are incorrect", errorMessage);
    }
}
