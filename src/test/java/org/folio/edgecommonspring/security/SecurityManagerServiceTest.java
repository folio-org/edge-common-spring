package org.folio.edgecommonspring.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.client.AuthnClient;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class SecurityManagerServiceTest {

  private static final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9eyJzdWIiOiJ0ZXN0X2FkbWluIiwidXNlcl9pZCI6ImQyNjUwOGJlLTJmMGItNTUyMC1iZTNkLWQwYjRkOWNkNmY2ZSIsImlhdCI6MTYxNjQ4NDc5NCwidGVuYW50IjoidGVzdCJ9VRYeA0s1O14hAXoTG34EAl80";
  private static final String LOGIN_RESPONSE_BODY = "{\r\n    \"username\": \"diku_admin\",\r\n    \"password\": \"admin\"\r\n}";
  private static final String API_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ0ZXN0X2FkbWluIn0=";
  private static final String WRONG_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ3cm9uZ191c2VyIn0=";

  @InjectMocks
  private SecurityManagerService securityManagerService;
  @Mock
  private AuthnClient authnClient;

  @BeforeEach
  void before() {
    ReflectionTestUtils
      .setField(securityManagerService, "secureStorePropsFile", "src/test/resources/ephemeral.properties");
  }

  @Test
  void getConnectionParams_success() {
    securityManagerService.init();
    when(authnClient.getApiKey(any(ConnectionSystemParameters.class), anyString())).thenReturn(getLoginResponse());
    ConnectionSystemParameters connectionSystemParameters = securityManagerService.getParamsWithToken(API_KEY);

    Assertions.assertNotNull(connectionSystemParameters);
    Assertions.assertEquals("test_admin", connectionSystemParameters.getUsername());
    Assertions.assertEquals("test", connectionSystemParameters.getPassword());
    Assertions.assertEquals("test", connectionSystemParameters.getTenantId());
    Assertions.assertEquals(MOCK_TOKEN, connectionSystemParameters.getOkapiToken());
  }

  @Test
  void getConnectionParams_passNotFound() {
    securityManagerService.init();
    AuthorizationException exception = Assertions.assertThrows(AuthorizationException.class, () ->
      securityManagerService.getParamsWithToken(WRONG_KEY));
    Assertions.assertEquals("Cannot get system connection properties for user with name: wrong_user, for tenant: test",
      exception.getMessage());
  }

  private ResponseEntity<String> getLoginResponse() {
    URI uri = null;
    try {
      uri = new URI("http://localhost:9130/login");
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return ResponseEntity.created(Objects.requireNonNull(uri))
      .header("x-okapi-token", MOCK_TOKEN)
      .body(LOGIN_RESPONSE_BODY);
  }


}
