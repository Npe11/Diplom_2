package api;

import clients.UserClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserRegistrationTest {
    private String uniqueEmail;
    private final String password = "password";
    private final String name = "TestUser";
    private String accessToken;
    private UserClient userClient;

    @Before
    public void setUp() {
        uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
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
        Response response = userClient.createUser(uniqueEmail, password, name);

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
        Response firstResponse = userClient.createUser(uniqueEmail, password, name);
        assertEquals(200, firstResponse.statusCode());
        accessToken = firstResponse.jsonPath().getString("accessToken");

        Response secondResponse = userClient.createUser(uniqueEmail, password, name);
        assertEquals(403, secondResponse.statusCode());

        String errorMessage = secondResponse.jsonPath().getString("message");
        assertEquals("User already exists", errorMessage);
    }

    @Test
    @Step("Попытка зарегистрировать пользователя без одного из обязательных полей")
    public void testRegisterUserMissingField() {
        Response response = userClient.createUser(uniqueEmail, password, null);

        assertEquals(403, response.statusCode());

        String errorMessage = response.jsonPath().getString("message");
        assertEquals("Email, password and name are required fields", errorMessage);
    }
}
