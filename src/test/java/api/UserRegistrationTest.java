package api;

import clients.UserClient;
import models.CourierModel;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserRegistrationTest {
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
    @DisplayName("Регистрация уникального пользователя")
    @Description("Проверяем, что пользователь с уникальными email, password и name может успешно зарегистрироваться.")
    public void testRegisterUniqueUser() {
        CourierModel user = new CourierModel(uniqueEmail, password, name);
        Response response = userClient.createUser(user);

        assertEquals(200, response.statusCode());
        assertTrue(response.jsonPath().getBoolean("success"));
        assertEquals(uniqueEmail, response.jsonPath().getString("user.email"));
        assertEquals(name, response.jsonPath().getString("user.name"));

        accessToken = response.jsonPath().getString("accessToken");
        assertNotNull("AccessToken не должен быть null", accessToken);
    }

    @Test
    @DisplayName("Повторная регистрация того же пользователя")
    @Description("Проверяем, что при попытке зарегистрировать уже существующего пользователя API возвращает 403 и сообщение об ошибке.")
    public void testRegisterAlreadyRegisteredUser() {
        CourierModel user = new CourierModel(uniqueEmail, password, name);
        Response firstResponse = userClient.createUser(user);
        assertEquals(200, firstResponse.statusCode());
        accessToken = firstResponse.jsonPath().getString("accessToken");

        Response secondResponse = userClient.createUser(user);
        assertEquals(403, secondResponse.statusCode());
        assertEquals("User already exists", secondResponse.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Регистрация без имени")
    @Description("Проверяем, что при отсутствии поля name API возвращает 403 и сообщение об обязательных полях.")
    public void testRegisterUserMissingNameField() {
        CourierModel userMissingName = new CourierModel(uniqueEmail, password, null);
        Response response = userClient.createUser(userMissingName);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Регистрация без пароля")
    @Description("Проверяем, что при отсутствии поля password API возвращает 403 и сообщение об обязательных полях.")
    public void testRegisterUserMissingPasswordField() {
        CourierModel userMissingPassword = new CourierModel(uniqueEmail, null, name);
        Response response = userClient.createUser(userMissingPassword);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Регистрация без email")
    @Description("Проверяем, что при отсутствии поля email API возвращает 403 и сообщение об обязательных полях.")
    public void testRegisterUserMissingEmailField() {
        CourierModel userMissingEmail = new CourierModel(null, password, name);
        Response response = userClient.createUser(userMissingEmail);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }
}
