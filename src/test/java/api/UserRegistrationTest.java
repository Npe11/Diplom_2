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
    @Step("Регистрация уникального пользователя")
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
    @Step("Попытка дважды зарегистрировать пользователя с одинаковыми данными")
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
    @Step("Попытка зарегистрировать пользователя без одного из обязательных полей: name")
    public void testRegisterUserMissingNameField() {
        CourierModel userMissingName = new CourierModel(uniqueEmail, password, null);
        Response response = userClient.createUser(userMissingName);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Step("Попытка зарегистрировать пользователя без одного из обязательных полей: password")
    public void testRegisterUserMissingPasswordField() {
        CourierModel userMissingName = new CourierModel(uniqueEmail, null, name);
        Response response = userClient.createUser(userMissingName);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Step("Попытка зарегистрировать пользователя без одного из обязательных полей: email")
    public void testRegisterUserMissingEmailField() {
        CourierModel userMissingName = new CourierModel(null, password, name);
        Response response = userClient.createUser(userMissingName);

        assertEquals(403, response.statusCode());
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }
}
