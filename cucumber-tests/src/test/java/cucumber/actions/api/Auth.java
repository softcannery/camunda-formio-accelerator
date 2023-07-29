package cucumber.actions.api;

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
        RestAssured.baseURI = this.camundaUrl;
        Map<String, String> headers = new HashMap<>();
        headers.put("referer", this.camundaUrl + "/camunda/app/tasklist/default/");
        Cookies cookies = RestAssured
            .given()
            .headers(headers)
            .when()
            .get("/camunda/api/engine/engine/")
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
        headers.put("origin", this.camundaUrl);
        headers.put("referer", this.camundaUrl + "/camunda/app/tasklist/default/");
        headers.put("X-Xsrf-Token", this.XSRF.getValue());
        headers.put("Accept", "application/json, text/plain, */*");
        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .headers(headers)
            .cookie(this.XSRF)
            .cookie(this.JSESSIONID)
            .body("username=" + this.username + "&password=" + this.password)
            .post("/camunda/api/admin/auth/user/default/login/tasklist");
        Assertions.assertEquals(200, res.statusCode(), "Authenticate: response code is not 200");
        this.JSESSIONID = res.getDetailedCookie("JSESSIONID");
        this.XSRF =
            RestAssured
                .given()
                .headers(headers)
                .cookie(this.XSRF)
                .cookie(this.JSESSIONID)
                .when()
                .get("/camunda/api/engine/engine/")
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
