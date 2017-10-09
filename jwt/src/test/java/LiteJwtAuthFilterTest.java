import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import app.TestApp;
import app.TestAppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

public class LiteJwtAuthFilterTest {

  public static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  /**
   * Decodes to:
   *
   * <blockquote><pre>
   * {
   *  "typ": "JWT",
   *  "alg": "HS256"
   * }.
   * {
   *  "iss": "Some lite service",
   *  "iat": 1507542376,
   *  "exp": 1602236776,
   *  "aud": "lite",
   *  "sub": "123456",
   *  "email": "example@example.com"
   * }
   * </pre></blockquote>
   *
   * using HMAC SHA-256 with {@link LiteJwtAuthFilterTest#JWT_SHARED_SECRET} for signing
   */
  public static final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTb21lIGxpdGUgc2VydmljZSIsImlhdCI6MTUwNzU0MjM3NiwiZXhwIjoxNjAyMjM2Nzc2LCJhdWQiOiJsaXRlIiwic3ViIjoiMTIzNDU2IiwiZW1haWwiOiJleGFtcGxlQGV4YW1wbGUuY29tIn0.wC_Jc4cOoM4UFX7UHHD3hCUcz8b9UPL_ImncY5FtAho";

  @ClassRule
  public static final DropwizardAppRule<TestAppConfig> RULE = new DropwizardAppRule<>(
      TestApp.class, resourceFilePath("test-config.yaml"),
      ConfigOverride.config("jwtSharedSecret", JWT_SHARED_SECRET));


  private static String urlTarget(String targetPath) {
    return "http://localhost:" + RULE.getLocalPort() + targetPath;
  }

  private static Map<String, String> jsonObjToMap(String jsonObj) throws IOException {
    TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(jsonObj, typeRef);
  }

  @Test
  public void authNotRequired() throws Exception {
    String target = urlTarget("/noauth");
    System.out.println(target);
    Response response = RULE.client().target(target).request().get();
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(body).isEqualTo("noauth");
  }

  @Test
  public void authRequired() throws Exception {
    String target = urlTarget("/auth");
    System.out.println(target);
    Response response = RULE.client().target(target).request().get();
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authSuccess() throws Exception {
    String target = urlTarget("/auth");
    System.out.println(target);
    Response response = RULE.client().target(target).request()
        .header("Authorization", "Bearer " + JWT)
        .get();

    String jsonObjBody = response.readEntity(String.class);
    Map<String, String> map = jsonObjToMap(jsonObjBody);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(map.get("name")).isEqualTo("123456");
    assertThat(map.get("userId")).isEqualTo("123456");
    assertThat(map.get("email")).isEqualTo("example@example.com");
  }
}
