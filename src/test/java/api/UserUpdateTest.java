package api;

import clients.UserClient;
import com.github.javafaker.Faker;
import models.CourierModel;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UserUpdateTest {

    private Faker faker;
    private String uniqueEmail;
    private String password;
    private String name;
    private String newEmail;
    private String newName;
    private String accessToken;
    private UserClient userClient;

    @Before
    public void setUp() {
        faker = new Faker();
        name = faker.name().fullName();
        password = faker.internet().password();
        uniqueEmail = faker.internet().emailAddress();
        userClient = new UserClient();

        Response regResponse = userClient.createUser(new CourierModel(uniqueEmail, password, name));
        accessToken = regResponse.jsonPath().getString("accessToken");
        newEmail = faker.internet().emailAddress();
        newName = faker.name().fullName();
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
    @DisplayName("Обновление имени пользователя с авторизацией")
    @Description("Проверяем, что авторизованный пользователь может обновить своё имя.")
    public void testUpdateUserNameWithAuthorization() {
        Map<String, String> updates = new HashMap<>();
        updates.put("name", newName);

        Response updateResponse = userClient.updateUser(updates, accessToken);
        assertEquals("Ожидается, что обновление будет успешным", 200, updateResponse.statusCode());
        assertEquals("Имя не обновлено", newName, updateResponse.jsonPath().getString("user.name"));
    }

    @Test
    @DisplayName("Обновление email пользователя с авторизацией")
    @Description("Проверяем, что авторизованный пользователь может обновить свой email.")
    public void testUpdateUserEmailWithAuthorization() {
        Map<String, String> updates = new HashMap<>();
        updates.put("email", newEmail);

        Response updateResponse = userClient.updateUser(updates, accessToken);
        assertEquals("Ожидается, что обновление будет успешным", 200, updateResponse.statusCode());
        assertEquals("Email не обновлён", newEmail, updateResponse.jsonPath().getString("user.email"));
    }

    @Test
    @DisplayName("Обновление пользователя без авторизации")
    @Description("Проверяем, что при попытке обновить данные без токена возвращается 401 и сообщение об ошибке.")
    public void testUpdateUserWithoutAuthorization() {
        Map<String, String> updates = new HashMap<>();
        updates.put("email", newEmail);
        updates.put("name", newName);

        Response updateResponse = userClient.updateUser(updates, "");
        assertEquals("Ожидается код 401 при обновлении без токена", 401, updateResponse.statusCode());
        assertEquals("Сообщение об ошибке не соответствует", "You should be authorised", updateResponse.jsonPath().getString("message"));
    }
}
