package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class PizzaProcess extends Base {

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, Cookie> cookiesMap;

    public PizzaProcess(Map<String, Cookie> cookiesMap) {
        this.headers.put("Accept", "application/hal+json, application/json; q=0.5");
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Origin", camundaUrl);
        this.headers.put("Referer", camundaUrl + "/camunda/app/tasklist/default/");
        this.headers.put("X-XSRF-TOKEN", cookiesMap.get("XSRF").getValue());
        this.cookiesMap = cookiesMap;
    }

    public String submitForm(String processDefinitionId) {
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .body("{\"businessKey\":\"1\",\"variables\":{}}")
            .urlEncodingEnabled(false)
            .post("camunda/api/engine/engine/default/process-definition/" + processDefinitionId + "/submit-form");
        String body = res.getBody().asString();
        Assertions.assertEquals(200, res.statusCode(), "Submit Process: response code is not 200");
        JsonPath jpath = new JsonPath(body);
        return jpath.getString("id");
    }

    public void completeForm(String taskId) {
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(headers)
            .cookie(cookiesMap.get("XSRF"))
            .cookie(cookiesMap.get("JSESSIONID"))
            .body("{\"variables\":{}}")
            .urlEncodingEnabled(false)
            .post("camunda/api/engine/engine/default/task/" + taskId + "/submit-form");
        Assertions.assertEquals(204, res.statusCode(), "Complete Process: response code is not 204");
    }
}
