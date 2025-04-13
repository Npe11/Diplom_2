package clients;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static utils.BaseClient.spec;

import java.util.HashMap;
import java.util.Map;

public class UserClient {

    @Step("Создание пользователя с данными: email = {email}, password = {password}, name = {name}")
    public Response createUser(String email, String password, String name) {
        Map<String, String> requestBody = new HashMap<>();

        if (email != null) {
            requestBody.put("email", email);
        }

        if (password != null) {
            requestBody.put("password", password);
        }

        if (name != null) {
            requestBody.put("name", name);
        }

        return RestAssured.given().spec(spec)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then().extract().response();
    }

    @Step("Логин пользователя с email: {email} и password: {password}")
    public Response loginUser(String email, String password) {
        Map<String, String> requestBody = new HashMap<>();

        if (email != null) {
            requestBody.put("email", email);
        }

        if (password != null) {
            requestBody.put("password", password);
        }

        return RestAssured.given().spec(spec)
                .body(requestBody)
                .when()
                .post("/api/auth/login")
                .then().extract().response();
    }

    @Step("Обновление данных пользователя: поля для обновления: {updates}, с токеном: {token}")
    public Response updateUser(Map<String, String> updates, String token) {
        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .body(updates)
                .when()
                .patch("/api/auth/user")
                .then().extract().response();
    }

    @Step("Удаление пользователя с токеном: {token}")
    public Response deleteUser(String token) {
        return RestAssured.given().spec(spec)
                .header("Authorization", token)
                .when()
                .delete("/api/auth/user")
                .then().extract().response();
    }
}
