package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
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
        this.headers.put("Origin", camundaUrl);
        this.headers.put("Referer", camundaUrl + tasks);
        this.headers.put("X-XSRF-TOKEN", cookiesMap.get("XSRF").getValue());
        this.cookiesMap = cookiesMap;
    }

    public Methods() {}

    public String getProcessDefinitionId(String processName) {
        Map<String, String> params = new HashMap<>();
        params.put("latest", "true");
        params.put("active", "true");
        params.put("startableInTasklist", "true");
        params.put("startablePermissionCheck", "true");
        params.put("firstResult", "0");
        params.put("maxResults", "15");

        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .queryParams(params)
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .get(processDefinition);
        Assertions.assertEquals(200, res.statusCode(), "Get deploymentId: response code not 200");

        String rbdy = res.body().asString();
        JsonPath jpath = new JsonPath(rbdy);

        return jpath.getString("find{it.name == '" + processName + "'}.id");
    }

    public JsonPath checkFormioFiles(String processName) {
        Map<String, String> params = new HashMap<>();
        params.put("latest", "true");
        params.put("active", "true");
        params.put("startableInTasklist", "true");
        params.put("startablePermissionCheck", "true");
        params.put("firstResult", "0");
        params.put("maxResults", "15");

        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .queryParams(params)
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .get(processDefinition);
        Assertions.assertEquals(200, res.statusCode(), "Get deploymentId: response code not 200");

        JsonPath jpath = new JsonPath(res.body().asString());
        String processDefinitionId = jpath.getString("find{it.name == '" + processName + "'}.deploymentId");

        res =
            httpRequest
                .queryParams(params)
                .headers(this.headers)
                .cookie(this.cookiesMap.get("XSRF"))
                .cookie(this.cookiesMap.get("JSESSIONID"))
                .get(deploymentApi + processDefinitionId + "/resources");
        Assertions.assertEquals(200, res.statusCode(), "Get deploymentId: response code not 200");

        return new JsonPath(res.body().asString());
    }

    public String getTaskId(String processInstanceId) {
        Map<String, String> params = new HashMap<>();
        params.put("processInstanceId", processInstanceId);

        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .queryParams(params)
            .headers(headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .get(taskApi);
        Assertions.assertEquals(200, res.statusCode(), "Get taskId: response code not 200");

        String rbdy = res.body().asString();
        JsonPath jpath = new JsonPath(rbdy);

        return jpath.getString("_embedded.task.id[0]");
    }

    public int getProcessInstance(String processInstanceId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest.headers(headers).get(processInstance + processInstanceId);
        return res.statusCode();
    }

    public int claim(String taskId) {
        String payload = "{\"userId\":\"kermit\"}";
        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest
            .headers(headers)
            .cookie(cookiesMap.get("XSRF"))
            .cookie(cookiesMap.get("JSESSIONID"))
            .body(payload)
            .urlEncodingEnabled(false)
            .post(taskApi + "/" + taskId + "/claim");
        return res.statusCode();
    }

    public Map<String, String> getFormVariables(String taskId) {
        Map<String, String> params = new HashMap<>();
        params.put("deserializeValues", "false");

        RestAssured.baseURI = camundaUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest
            .headers(this.headers)
            .cookie(this.cookiesMap.get("XSRF"))
            .cookie(this.cookiesMap.get("JSESSIONID"))
            .queryParams(params)
            .get(taskApi + "/" + taskId + "/form-variables");
        Assertions.assertEquals(200, res.statusCode(), "Get form variables: response code not 200");

        String rbdy = res.body().asString();
        Map<String, String> submitAndActivityIds = new HashMap<>();
        String submissionId = rbdy.split("submissionId\\\\\":\\\\\"")[1].split(":")[0];
        String activityInstanceId = rbdy.split("\"activityInstanceId\\\\\":\\\\\"")[1].split("\\\\\"")[0];

        submitAndActivityIds.put("submissionId", submissionId);
        submitAndActivityIds.put("activityInstanceId", activityInstanceId);
        submitAndActivityIds.put("body", rbdy);

        return submitAndActivityIds;
    }

    public JsonPath getMailFromInbox(String emailAddress) {
        RestAssured.baseURI = mailApiUrl;
        RequestSpecification httpRequest = RestAssured.given();
        Response res = httpRequest.get("api/user/" + emailAddress + "/messages");
        String rbdy = res.body().asString();
        return new JsonPath(rbdy);
    }

    public void getDeleteAllMails(String emailAddress) {
        RestAssured.baseURI = mailApiUrl;
        RequestSpecification httpRequest = RestAssured.given();
        httpRequest.delete("api/user/" + emailAddress);
    }
}
