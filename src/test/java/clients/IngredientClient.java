package clients;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static utils.BaseClient.spec;

public class IngredientClient {

    @Step("Получение данных об ингредиентах")
    public Response getIngredients() {
        return RestAssured.given().spec(spec)
                .when()
                .get("/api/ingredients")
                .then().extract().response();
    }
}
