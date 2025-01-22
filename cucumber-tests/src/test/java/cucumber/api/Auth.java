package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Auth extends Base {

    public Map<String, Cookie> loginPost() {
        RestAssured.baseURI = camundaUrl;
        Response respCamundaApp = RestAssured
            .given()
            .redirects()
            .follow(false)
            .when()
            .get("/camunda/app/")
            .then()
            .statusCode(302)
            .extract()
            .response();
        Cookie JSESSIONID1 = respCamundaApp.getDetailedCookies().get("JSESSIONID");

        Response respAuthKC = RestAssured
            .given()
            .redirects()
            .follow(false)
            .cookie(JSESSIONID1)
            .when()
            .get("/oauth2/authorization/keycloak")
            .then()
            .statusCode(302)
            .extract()
            .response();
        String nextLocation = respAuthKC.getHeader("location");

        Map<String, String> params0 = new HashMap<>();
        params0.put("response_type", nextLocation.split("response_type=")[1].split("&")[0]);
        params0.put("client_id", nextLocation.split("client_id=")[1].split("&")[0]);
        params0.put("scope", nextLocation.split("scope=")[1].split("&")[0]);
        params0.put("state", URLDecoder.decode(nextLocation.split("state=")[1].split("&")[0], StandardCharsets.UTF_8));
        params0.put("redirect_uri", nextLocation.split("redirect_uri=")[1].split("&")[0]);
        params0.put("nonce", nextLocation.split("nonce=")[1].split("&")[0]);

        RestAssured.baseURI = dockerKeycloak;
        Response kcAuth = RestAssured
            .given()
            .redirects()
            .follow(false)
            .queryParams(params0)
            .when()
            .get("auth/realms/camunda/protocol/openid-connect/auth")
            .then()
            .statusCode(200)
            .extract()
            .response();

        String respoBody = kcAuth.getBody().asString();
        String postUrl = respoBody
            .split("onsubmit=\"login.disabled = true; return true;\" action=\"")[1].split(
                "\" method=\"post\" novalidate=\"novalidate\">"
            )[0];

        RestAssured.baseURI = postUrl.split("/auth/realms")[0];
        Map<String, String> headersPost = new HashMap<>();
        headersPost.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> params = new HashMap<>();
        params.put("session_code", postUrl.split("session_code=")[1].split("&amp;")[0]);
        params.put("execution", postUrl.split("execution=")[1].split("&amp;")[0]);
        params.put("client_id", postUrl.split("client_id=")[1].split("&amp;")[0]);
        params.put("tab_id", postUrl.split("tab_id=")[1].split("&amp;")[0]);
        params.put("client_data", postUrl.split("client_data=")[1].split("&amp;")[0]);

        String uri = postUrl.split(dockerKeycloak + "/")[1].split("\\?")[0];
        RestAssured.baseURI = dockerKeycloak;
        Response kcAuthPost = RestAssured
            .given()
            .redirects()
            .follow(false)
            .headers(headersPost)
            .queryParams(params)
            .cookies(kcAuth.getDetailedCookies())
            .body("username=" + username + "&password=" + password + "&credentialId=")
            .post(uri)
            .then()
            .statusCode(302)
            .extract()
            .response();
        nextLocation = kcAuthPost.getHeader("location");

        Map<String, String> params2 = new HashMap<>();
        params2.put("state", URLDecoder.decode(nextLocation.split("state=")[1].split("&")[0], StandardCharsets.UTF_8));
        params2.put("session_state", nextLocation.split("session_state=")[1].split("&")[0]);
        params2.put("iss", URLDecoder.decode(nextLocation.split("iss=")[1].split("&")[0], StandardCharsets.UTF_8));
        params2.put("code", nextLocation.split("code=")[1].split("&")[0]);
        RestAssured.baseURI = camundaUrl;
        Response respJSID = RestAssured
            .given()
            .redirects()
            .follow(false)
            .queryParams(params2)
            .cookie(JSESSIONID1)
            .when()
            .get("login/oauth2/code/keycloak")
            .then()
            .statusCode(302)
            .extract()
            .response();
        Cookie JSESSIONID = respJSID.getDetailedCookies().get("JSESSIONID");

        RestAssured.baseURI = camundaUrl;
        Response respXSRF = RestAssured
            .given()
            .redirects()
            .follow(false)
            .cookie(JSESSIONID)
            .when()
            .get("camunda/app/?continue")
            .then()
            .statusCode(302)
            .extract()
            .response();
        Cookie XSRF = respXSRF.getDetailedCookies().get("XSRF-TOKEN");

        Map<String, Cookie> cookiesMap = new HashMap<>();
        cookiesMap.put("JSESSIONID", JSESSIONID);
        cookiesMap.put("XSRF", XSRF);
        return cookiesMap;
    }
}
