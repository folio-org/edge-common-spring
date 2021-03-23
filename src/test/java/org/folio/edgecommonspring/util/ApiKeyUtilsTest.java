package org.folio.edgecommonspring.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.folio.edge.api.utils.model.ClientInfo;
import org.folio.edgecommonspring.util.ApiKeyUtils.MalformedApiKeyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class ApiKeyUtilsTest {

  public static final String SALT_LEN = "10";
  public static final String TENANT = "diku";
  public static final String USERNAME = "diku";
  public static final String API_KEY = "eyJzIjoiZ0szc0RWZ3labCIsInQiOiJkaWt1IiwidSI6ImRpa3UifQ==";
  public static final String BAD_API_KEY = "broken";
  ObjectMapper objectMapper = new ObjectMapper();


  @Test
  void testParseApiKeyNullSalt() throws Exception {
    ClientInfo ci = new ClientInfo(null, TENANT, USERNAME);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assertions.assertThrows(ApiKeyUtils.MalformedApiKeyException.class,
      () -> ApiKeyUtils.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Salt", exception.getMessage());
  }

  @Test
  void testParseApiKeyNullTenant() throws Exception {
    ClientInfo ci = new ClientInfo("abcdef12345", null, USERNAME);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assertions.assertThrows(ApiKeyUtils.MalformedApiKeyException.class,
      () -> ApiKeyUtils.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Tenant", exception.getMessage());
  }

  @Test
  void testParseApiKeyNullUsername() throws Exception {
    ClientInfo ci = new ClientInfo("abcdef12345", TENANT, null);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assertions.assertThrows(ApiKeyUtils.MalformedApiKeyException.class,
      () -> ApiKeyUtils.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Username", exception.getMessage());
  }

  @Test
  void testGenerateSuccess() throws Exception {

    ClientInfo info = ApiKeyUtils.parseApiKey(API_KEY);

    assertEquals(Integer.parseInt(SALT_LEN), info.salt.length());
    assertEquals(TENANT, info.tenantId);
    assertEquals(USERNAME, info.username);
  }

  @Test
  void testParseApiKeyBrokenApiKey() throws Exception {

    MalformedApiKeyException exception = Assertions.assertThrows(ApiKeyUtils.MalformedApiKeyException.class,
      () -> ApiKeyUtils.parseApiKey(BAD_API_KEY));

    assertEquals("Malformed API Key: Failed to parse", exception.getMessage());
  }


}
