package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class Auth extends Base {

    private Cookie XSRF;
    private Cookie JSESSIONID;

    private void getCsrf() {
        RestAssured.baseURI = camundaUrl;
        Map<String, String> headers = new HashMap<>();
        headers.put("referer", camundaUrl + tasks);
        Cookies cookies = RestAssured
            .given()
            .headers(headers)
            .when()
            .get(engine)
            .then()
            .statusCode(200)
            .extract()
            .response()
            .getDetailedCookies();
        this.XSRF = cookies.get("XSRF-TOKEN");
        this.JSESSIONID = cookies.get("JSESSIONID");
    }

    public Map<String, Cookie> loginPost() {
        getCsrf();

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.put("origin", camundaUrl);
        headers.put("referer", camundaUrl + tasks);
        headers.put("X-Xsrf-Token", this.XSRF.getValue());
        headers.put("Accept", "application/json, text/plain, */*");
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .headers(headers)
            .cookie(this.XSRF)
            .cookie(this.JSESSIONID)
            .body("username=" + username + "&password=" + password)
            .post(loginTasks);
        Assertions.assertEquals(200, res.statusCode(), "Authenticate: response code is not 200");
        this.JSESSIONID = res.getDetailedCookie("JSESSIONID");
        this.XSRF =
            RestAssured
                .given()
                .headers(headers)
                .cookie(this.XSRF)
                .cookie(this.JSESSIONID)
                .when()
                .get(engine)
                .then()
                .statusCode(200)
                .extract()
                .response()
                .getDetailedCookies()
                .get("XSRF-TOKEN");
        Map<String, Cookie> cookiesMap = new HashMap<>();
        cookiesMap.put("JSESSIONID", this.JSESSIONID);
        cookiesMap.put("XSRF", this.XSRF);
        return cookiesMap;
    }
}
