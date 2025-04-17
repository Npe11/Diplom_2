package api;

import clients.IngredientClient;
import clients.OrderClient;
import clients.UserClient;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.CourierModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderCreationTest {

    private Faker faker;
    private String uniqueEmail;
    private String password;
    private String name;
    private String accessToken;
    private UserClient userClient;
    private OrderClient orderClient;
    private IngredientClient ingredientClient;

    @Before
    public void setUp() {
        faker = new Faker();
        name = faker.name().fullName();
        password = faker.internet().password();
        uniqueEmail = faker.internet().emailAddress();

        userClient = new UserClient();
        orderClient = new OrderClient();
        ingredientClient = new IngredientClient();

        CourierModel user = new CourierModel(uniqueEmail, password, name);
        Response regResponse = userClient.createUser(user);
        accessToken = regResponse.jsonPath().getString("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
        accessToken = null;
        userClient = null;
        orderClient = null;
        ingredientClient = null;
    }

    @Step("Получить валидные идентификаторы ингредиентов")
    private List<String> getValidIngredientIds() {
        Response ingredientResponse = ingredientClient.getIngredients();
        List<String> ingredientIds = ingredientResponse.jsonPath().getList("data._id");
        return new ArrayList<>(ingredientIds.subList(0, 2));
    }

    @Test
    @Step("Успешное создание заказа с авторизацией")
    public void testCreateOrderWithAuthorizationAndValidIngredients() {
        List<String> validIngredients = getValidIngredientIds();

        Response response = orderClient.createOrder(validIngredients, accessToken);
        assertEquals("Заказ должен быть создан успешно (код 200)", 200, response.statusCode());
        assertTrue("Параметр success должен быть true", response.jsonPath().getBoolean("success"));
        assertNotNull("Номер заказа не найден", response.jsonPath().get("order.number"));
    }

    @Test
    @Step("Успешное создание заказа без авторизации")
    public void testCreateOrderWithoutAuthorizationAndValidIngredients() {
        List<String> validIngredients = getValidIngredientIds();

        Response response = orderClient.createOrder(validIngredients, "");
        assertEquals("Заказ должен быть создан успешно (код 200)", 200, response.statusCode());
        assertTrue("Параметр success должен быть true", response.jsonPath().getBoolean("success"));
        assertNotNull("Номер заказа не найден", response.jsonPath().get("order.number"));
    }

    @Test
    @Step("Попытка создания заказа без ингридиентов")
    public void testCreateOrderWithAuthorizationButNoIngredients() {
        List<String> emptyIngredients = Collections.emptyList();

        Response response = orderClient.createOrder(emptyIngredients, accessToken);
        assertEquals("При отсутствии ингредиентов ожидается код 400", 400, response.statusCode());
        String message = response.jsonPath().getString("message");
        assertEquals("Ingredient ids must be provided", message);
    }

    @Test
    @Step("Попытка создания заказа с неправильным хэшом ингридиента")
    public void testCreateOrderWithAuthorizationAndInvalidIngredient() {
        List<String> invalidIngredients = Arrays.asList("invalidIngredientHash");

        Response response = orderClient.createOrder(invalidIngredients, accessToken);
        assertEquals("При неверном хеше ингредиента ожидается код 500", 500, response.statusCode());
    }
}
