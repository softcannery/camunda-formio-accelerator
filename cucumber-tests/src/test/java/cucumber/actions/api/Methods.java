package cucumber.actions.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class Methods extends Base {

    private final Map<String, String> headers = new HashMap<>();
    private Map<String, Cookie> cookiesMap;

    public Methods(Map<String, Cookie> cookiesMap) {
        this.headers.put("Accept", "application/hal+json, application/json; q=0.5");
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Origin", this.camundaUrl);
        this.headers.put("Referer", this.camundaUrl + "/camunda/app/tasklist/default/");
        this.headers.put("X-XSRF-TOKEN", cookiesMap.get("XSRF").getValue());
        this.cookiesMap = cookiesMap;
    }

    public String getProcessDefinitionId(String processName) {
        Map<String, String> params = new HashMap<>();
        params.put("latest", "true");
        params.put("active", "true");
        params.put("startableInTasklist", "true");
        params.put("startablePermissionCheck", "true");
        params.put("firstResult", "0");
        params.put("maxResults", "15");

        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .queryParams(params)
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .get("camunda/api/engine/engine/default/process-definition");
        Assertions.assertEquals(200, res.statusCode(), "Get deploymentId: response code not 200");

        ResponseBody body = res.body();
        String rbdy = body.asString();
        JsonPath jpath = new JsonPath(rbdy);

        return jpath.getString("find{it.name == '" + processName + "'}.id");
    }

    public String getTaskId(String processInstanceId) {
        Map<String, String> params = new HashMap<>();
        params.put("processInstanceId", processInstanceId);

        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .queryParams(params)
            .headers(headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .get("camunda/api/engine/engine/default/task");
        Assertions.assertEquals(200, res.statusCode(), "Get taskId: response code not 200");

        ResponseBody body = res.body();
        String rbdy = body.asString();
        JsonPath jpath = new JsonPath(rbdy);

        return jpath.getString("_embedded.task.id[0]");
    }

    private String getFilterId() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, String> params = new HashMap<>();
        params.put("firstResult", "0");
        params.put("maxResults", "2000");
        params.put("itemCount", "false");
        params.put("resourceType", "Task");

        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest.queryParams(params).headers(headers).get("/engine-rest/filter");
        Assertions.assertEquals(200, res.statusCode(), "Get filterId: response code not 200");

        ResponseBody body = res.body();
        String rbdy = body.asString();
        JsonPath jpath = new JsonPath(rbdy);

        return jpath.getString("id[0]");
    }

    public int getProcessInstance(String processInstanceId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest.headers(headers).get("/engine-rest/process-instance/" + processInstanceId);
        return res.statusCode();
    }

    public void claim(String taskId) {
        String payload = "{\"userId\":\"kermit\"}";
        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(headers)
            .cookie(cookiesMap.get("XSRF"))
            .cookie(cookiesMap.get("JSESSIONID"))
            .body(payload)
            .urlEncodingEnabled(false)
            .post("camunda/api/engine/engine/default/task/" + taskId + "/claim");
        Assertions.assertEquals(204, res.statusCode(), "Claim Process: response code is not 200");
    }

    public Map<String, String> getFormVariables(String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put("deserializeValues", "false");

        RestAssured.baseURI = this.camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .queryParams(params)
            .get("camunda/api/engine/engine/default/task/" + taskId + "/form-variables");
        Assertions.assertEquals(200, res.statusCode(), "Get form variables: response code not 200");

        String rbdy = res.body().asString();
        Map<String, String> submitAndActivityIds = new HashMap<>();
        //extract submissionId
        String submissionId = rbdy.split("submissionId\\\\\":\\\\\"")[1].split(":")[0];
        //extract ActivityId
        String activityInstanceId = rbdy.split("\"activityInstanceId\\\\\":\\\\\"")[1].split("\\\\\"")[0];

        submitAndActivityIds.put("submissionId", submissionId);
        submitAndActivityIds.put("activityInstanceId", activityInstanceId);
        submitAndActivityIds.put("body", rbdy);

        return submitAndActivityIds;
    }
}
