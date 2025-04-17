package clients;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.CourierModel;

import static utils.BaseClient.spec;

public class UserClient {

    @Step("Создание пользователя: {courierModel}")
    public Response createUser(CourierModel courierModel) {
        return RestAssured.given().spec(spec)
                .body(courierModel)
                .when()
                .post("/api/auth/register")
                .then().extract().response();
    }

    @Step("Логин пользователя: {courierModel}")
    public Response loginUser(CourierModel courierModel) {
        return RestAssured.given().spec(spec)
                .body(courierModel)
                .when()
                .post("/api/auth/login")
                .then().extract().response();
    }

    @Step("Обновление данных пользователя: {updates}, token={token}")
    public Response updateUser(Object updates, String token) {
        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .body(updates)
                .when()
                .patch("/api/auth/user")
                .then().extract().response();
    }

    @Step("Удаление пользователя, token={token}")
    public Response deleteUser(String token) {
        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .when()
                .delete("/api/auth/user")
                .then().extract().response();
    }
}
