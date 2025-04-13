package utils;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class BaseClient {
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site";

    public static RequestSpecification spec = RestAssured.given()
            .baseUri(BASE_URL)
            .header("Content-Type", "application/json");
}
