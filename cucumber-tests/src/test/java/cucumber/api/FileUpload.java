package cucumber.api;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public class FileUpload extends Base {

    public String uploadForm(String filePath, Map<String, Cookie> cookiesMap) {
        File testFile = null;
        URL resource = getClass().getClassLoader().getResource(filePath);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            try {
                testFile = new File(resource.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        RestAssured.baseURI = fileuploadUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Map<String, String> params = new HashMap<>();
        params.put("baseUrl", "https%3A%2F%2Fapi.form.io");
        params.put("project", "");
        params.put("form", "");

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "multipart/form-data");
        headers.put("origin", camundaUrl);
        headers.put("referer", camundaUrl + "/camunda/app/tasklist/default/");
        headers.put("Accept", "*/*");
        //RestAssured.baseURI = this.camundaUrl;

        Response res = httpRequest
            .queryParams(params)
            .headers(headers)
            .cookie(cookiesMap.get("JSESSIONID"))
            .multiPart("file", testFile)
            .multiPart("name", "bot.png")
            .multiPart("dir", "")
            .when()
            .post("/content/upload");
        Assertions.assertEquals(200, res.statusCode(), "Upload file: response code is not 200");
        JsonPath jpath = new JsonPath(res.getBody().asString());
        return jpath.getString("url");
    }
}
