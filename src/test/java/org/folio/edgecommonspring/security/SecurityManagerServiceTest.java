package org.folio.edgecommonspring.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SecurityManagerServiceTest {

  private static final String MOCK_TOKEN = "test-token";
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
  void getConnectionParams_success_withExpiredCachedTokenWithNullExpirationValue() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("test_admin", "test", "test", new UserToken(MOCK_TOKEN,
        null));
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
  void getConnectionParams_success_withExpiredCachedTokenWithNullAccessTokenValue() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("test_admin", "test", "test", new UserToken(null,
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
  void getConnectionParams_success_withExpiredCachedTokenWithNullToken() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("test_admin", "test", "test", null);
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

  @Test
  void getConnectionParams_withTemperedEdgeApiKey() {
    securityManagerService.init();
    TokenCache tokenCache = TokenCache.getInstance();
    tokenCache.put("BPaofNFncK6477DubxDh", "test", "wrong_user", new UserToken(MOCK_TOKEN,
        Instant.now().minus(1, ChronoUnit.DAYS)));
    var temperedEdgeApiKey = API_KEY + "tempered";
    AuthorizationException exception = Assertions.assertThrows(AuthorizationException.class, () ->
        securityManagerService.getParamsWithToken(temperedEdgeApiKey));
    Assertions.assertEquals("Malformed edge api key: " + temperedEdgeApiKey,
        exception.getMessage());
  }
}
