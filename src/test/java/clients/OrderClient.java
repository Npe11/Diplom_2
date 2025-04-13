package clients;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static utils.BaseClient.spec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderClient {

    @Step("Создание заказа с ингредиентами: {ingredients} и токеном: {token}")
    public Response createOrder(List<String> ingredients, String token) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ingredients", ingredients);

        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then().extract().response();
    }

    @Step("Получение заказов конкретного пользователя с токеном: {token}")
    public Response getUserOrders(String token) {
        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .get("/api/orders")
                .then().extract().response();
    }
}
