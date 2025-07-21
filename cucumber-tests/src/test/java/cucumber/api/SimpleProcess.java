package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class SimpleProcess extends Base {

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, Cookie> cookiesMap;

    public SimpleProcess(Map<String, Cookie> cookiesMap) {
        this.headers.put("Accept", "application/hal+json, application/json; q=0.5");
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Origin", camundaUrl);
        this.headers.put("Referer", camundaUrl + tasks);
        this.headers.put("X-XSRF-TOKEN", cookiesMap.get("XSRF").getValue());
        this.cookiesMap = cookiesMap;
    }

    public String submitForm(String processDefinitionId) {
        String payloadStr =
            "{\"variables\": {\"textField\": {\"value\": \"mike test\",\"type\": \"String\"},\"number\": {\"value\": 111111111,\"type\": \"Double\"}}}";

        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .body(payloadStr)
            .urlEncodingEnabled(false)
            .post(processDefinition + "/" + processDefinitionId + "/start");
        Assertions.assertEquals(200, res.statusCode(), "Submit Process: response code is not 200");
        String body = res.getBody().asString();
        JsonPath jpath = new JsonPath(body);
        return jpath.getString("id");
    }

    public void completeProcess(String taskId, Map<String, String> submitAndActivityIds, String processDefinitionId) {
        Payload payload = new Payload();
        String payloadStr = payload.readPayloadFromFile("simpleProcessComplete.txt");
        payloadStr = payloadStr.replace("<PROCESS_DEFINITION_ID>", processDefinitionId);
        payloadStr = payloadStr.replace("<ACTIVITY_INSTANCE_ID>", submitAndActivityIds.get("activityInstanceId"));
        payloadStr = payloadStr.replace("<SUBMISSION_ID>", submitAndActivityIds.get("submissionId"));
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(headers)
            .cookie(cookiesMap.get("XSRF"))
            .cookie(cookiesMap.get("JSESSIONID"))
            .body(payloadStr)
            .urlEncodingEnabled(false)
            .post(taskApi + "/" + taskId + "/complete");
        Assertions.assertEquals(204, res.statusCode(), "Complete Process: response code is not 204");
    }
}
