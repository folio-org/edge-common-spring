package org.folio.edgecommonspring.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.folio.edge.api.utils.cache.TokenCache;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.spring.model.UserToken;
import org.folio.spring.service.SystemUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SecurityManagerServiceTest {

  private static final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9eyJzdWIiOiJ0ZXN0X2FkbWluIiwidXNlcl9pZCI6ImQyNjUwOGJlLTJmMGItNTUyMC1iZTNkLWQwYjRkOWNkNmY2ZSIsImlhdCI6MTYxNjQ4NDc5NCwidGVuYW50IjoidGVzdCJ9VRYeA0s1O14hAXoTG34EAl80";
  private static final String LOGIN_RESPONSE_BODY = "{\r\n    \"username\": \"diku_admin\",\r\n    \"password\": \"admin\"\r\n}";
  private static final String API_KEY = "eyJzIjoidGVzdF9hZG1pbiIsInQiOiJ0ZXN0IiwidSI6InRlc3QifQ==";
  private static final String WRONG_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ3cm9uZ191c2VyIn0=";
  private static final Instant TOKEN_EXPIRATION = Instant.now().plus(1, ChronoUnit.DAYS);

  @InjectMocks
  private SecurityManagerService securityManagerService;

  @Mock
  private SystemUserService systemUserService;

  @BeforeEach
  void before() {
    ReflectionTestUtils
      .setField(securityManagerService, "secureStorePropsFile", "src/test/resources/ephemeral.properties");
    ReflectionTestUtils
        .setField(securityManagerService, "cacheTtlMs", 360000);
    ReflectionTestUtils
        .setField(securityManagerService, "cacheCapacity", 100);
  }

  /*@Test
  void getConnectionParams_success() {
    securityManagerService.init();
//    when(authnClient.getApiKey(any(ConnectionSystemParameters.class), anyString())).thenReturn(getLoginResponse());
    ConnectionSystemParameters connectionSystemParameters = securityManagerService.getParamsWithToken(API_KEY);

    Assertions.assertNotNull(connectionSystemParameters);
    Assertions.assertEquals("test_admin", connectionSystemParameters.getUsername());
    Assertions.assertEquals("test", connectionSystemParameters.getPassword());
    Assertions.assertEquals("test", connectionSystemParameters.getTenantId());
    Assertions.assertEquals(MOCK_TOKEN, connectionSystemParameters.getOkapiToken());
  }*/

  @Test
  void getConnectionParams_success() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("test_admin", "test", "test", new UserToken(MOCK_TOKEN, TOKEN_EXPIRATION));
    ConnectionSystemParameters connectionSystemParameters = securityManagerService.getParamsWithToken(API_KEY);

    Assertions.assertNotNull(connectionSystemParameters);
    Assertions.assertEquals("test", connectionSystemParameters.getTenantId());
    Assertions.assertEquals(MOCK_TOKEN, connectionSystemParameters.getOkapiToken().accessToken());
  }

  @Test
  void getConnectionParams_success_with_expired_cached_token() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("test_admin", "test", "test", new UserToken(MOCK_TOKEN,
        Instant.now().minus(1, ChronoUnit.DAYS)));
    ConnectionSystemParameters csp = ConnectionSystemParameters.builder()
        .tenantId("test")
        .username("test")
        .password("test")
        .build();
    when(systemUserService.authSystemUser(any(), any(), any()))
        .thenReturn(new UserToken(MOCK_TOKEN, TOKEN_EXPIRATION));
    ConnectionSystemParameters connectionSystemParameters = securityManagerService.getParamsWithToken(API_KEY);

    Assertions.assertNotNull(connectionSystemParameters);
    Assertions.assertEquals("test", connectionSystemParameters.getTenantId());
    Assertions.assertEquals(MOCK_TOKEN, connectionSystemParameters.getOkapiToken().accessToken());
  }

  @Test
  void getConnectionParams_passNotFound() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("BPaofNFncK6477DubxDh", "test", "wrong_user", new UserToken(MOCK_TOKEN,
        Instant.now().minus(1, ChronoUnit.DAYS)));
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
