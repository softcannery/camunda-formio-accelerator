package cucumber.api;

public class Base {

    public static final String camundaUrl = "http://localhost:80";
    public static final String username = "kermit";
    public static final String password = "password";
    public static final String fileuploadUrl = "http://localhost:80";
    public static final String mailApiUrl = "http://localhost:8088/";

    public static final String tasks = "/camunda/app/tasklist/default/";
    public static final String engine = "/camunda/api/engine/engine/";
    public static final String loginTasks = "/camunda/api/admin/auth/user/default/login/tasklist";
    public static final String upload = "/content/upload";

    public static final String taskApi = "camunda/api/engine/engine/default/task";
    public static final String processDefinition = "camunda/api/engine/engine/default/process-definition";
    public static final String deploymentApi = "camunda/api/engine/engine/default/deployment/";
    public static final String processInstance = "/engine-rest/process-instance/";
}
