package cucumber.api;

public class Base {
    public static String camundaUrl = "http://localhost:80";
    public static String username = "kermit";
    public static String password = "password";
    public static String fileuploadUrl = "http://localhost:80";
    public static String mailApiUrl = "http://localhost:8088/";
    // public static String dockerKeycloak = "http://host.docker.internal";
    public static String dockerKeycloak = "http://" + System.getenv("OAUTH2_SSO_HOST");
}
