package api;

import clients.IngredientClient;
import clients.OrderClient;
import clients.UserClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserOrdersTest {

    private String uniqueEmail;
    private final String password = "password";
    private final String name = "OrderUserTest";
    private String accessToken;
    private UserClient userClient;
    private OrderClient orderClient;
    private IngredientClient ingredientClient;

    @Before
    public void setUp() {
        uniqueEmail = "orderuser" + System.currentTimeMillis() + "@example.com";
        userClient = new UserClient();
        orderClient = new OrderClient();
        ingredientClient = new IngredientClient();

        Response regResponse = userClient.createUser(uniqueEmail, password, name);
        accessToken = regResponse.jsonPath().getString("accessToken");

        List<String> validIngredients = getValidIngredientIds();
        orderClient.createOrder(validIngredients, accessToken);
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
    @Step("Получить список ордеров с авторизацией")
    public void testGetUserOrdersWithAuthorization() {
        Response response = orderClient.getUserOrders(accessToken);

        assertEquals("Ожидается статус 200 для авторизованного запроса", 200, response.statusCode());
        assertTrue("Параметр success должен быть true", response.jsonPath().getBoolean("success"));
        assertNotNull("Поле orders не должно быть null", response.jsonPath().getList("orders"));
    }

    @Test
    @Step("Получить список ордеров без авторизации")
    public void testGetUserOrdersWithoutAuthorization() {
        Response response = orderClient.getUserOrders("");

        assertEquals("Без авторизации ожидается код 401", 401, response.statusCode());
        assertFalse("Success должен быть false при отсутствии авторизации", response.jsonPath().getBoolean("success"));
        assertEquals("Сообщение об ошибке не соответствует", "You should be authorised", response.jsonPath().getString("message"));
    }
}
