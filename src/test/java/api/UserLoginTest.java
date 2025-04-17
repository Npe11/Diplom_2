package api;

import clients.UserClient;
import models.CourierModel;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserLoginTest {

    private Faker faker;
    private String uniqueEmail;
    private String password;
    private String name;
    private String accessToken;
    private UserClient userClient;

    @Before
    public void setUp() {
        faker = new Faker();
        uniqueEmail = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().fullName();
        userClient = new UserClient();
        Response regResponse = userClient.createUser(new CourierModel(uniqueEmail, password, name));
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
        CourierModel login = new CourierModel(uniqueEmail, password, null);
        Response loginResponse = userClient.loginUser(login);

        assertEquals("Логин завершился с ошибочным статусом", 200, loginResponse.statusCode());
        assertTrue("Поле success должно быть true", loginResponse.jsonPath().getBoolean("success"));

        String newAccessToken = loginResponse.jsonPath().getString("accessToken");
        assertNotNull("AccessToken не должен быть null при успешном логине", newAccessToken);
        assertEquals("Email в ответе не соответствует", uniqueEmail, loginResponse.jsonPath().getString("user.email"));
    }

    @Test
    @Step("Логин с не правильными данными: email")
    public void testLoginWithInvalidEmail() {
        CourierModel wrongLogin = new CourierModel("wrongEmail@test.com", password, null);
        Response loginResponse = userClient.loginUser(wrongLogin);

        assertEquals("Ожидается статус 401 Unauthorized при неверном email", 401, loginResponse.statusCode());
        assertEquals("email or password are incorrect", loginResponse.jsonPath().getString("message"));
    }

    @Test
    @Step("Логин с не правильными данными: password")
    public void testLoginWithInvalidPassword() {
        CourierModel wrongLogin = new CourierModel(uniqueEmail, "wrongPassword", null);
        Response loginResponse = userClient.loginUser(wrongLogin);

        assertEquals("Ожидается статус 401 Unauthorized при неверном пароле", 401, loginResponse.statusCode());
        assertEquals("email or password are incorrect", loginResponse.jsonPath().getString("message"));
    }
}
