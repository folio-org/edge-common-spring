package org.folio.edgecommonspring.security;

import static org.folio.edge.api.utils.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.api.utils.Constants.PROP_SECURE_STORE_TYPE;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.api.utils.cache.TokenCache;
import org.folio.edge.api.utils.cache.TokenCache.NotInitializedException;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edge.api.utils.model.ClientInfo;
import org.folio.edge.api.utils.security.SecureStore;
import org.folio.edge.api.utils.security.SecureStore.NotFoundException;
import org.folio.edge.api.utils.security.SecureStoreFactory;
import org.folio.edge.api.utils.util.ApiKeyParser;
import org.folio.edge.api.utils.util.PropertiesUtil;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.spring.model.UserToken;
import org.folio.spring.service.SystemUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SecurityManagerService {

  private final SystemUserService systemUserService;
  private SecureStore secureStore;
  private TokenCache tokenCache;
  @Value("${secure_store:Ephemeral}")
  private String secureStoreType;
  @Value("${secure_store_props:src/main/resources/ephemeral.properties}")
  private String secureStorePropsFile;
  @Value("${token_cache_ttl_ms:3636000}")
  private long cacheTtlMs;
  @Value("${null_token_cache_ttl_ms:30000}")
  private long failureCacheTtlMs;
  @Value("${token_cache_capacity:100}")
  private int cacheCapacity;

  @PostConstruct
  public void init() {
    if (null == tokenCache) {
      log.info("Using token cache TTL (ms): {}", cacheTtlMs);
      log.info("Using failure token cache TTL (ms): {}", failureCacheTtlMs);
      log.info("Using token cache capacity: {}", cacheCapacity);
      tokenCache = TokenCache.initialize(cacheTtlMs, failureCacheTtlMs, cacheCapacity);
    }
    Properties secureStoreProps = PropertiesUtil.getProperties(secureStorePropsFile);
    String type = secureStoreProps.getProperty(PROP_SECURE_STORE_TYPE, DEFAULT_SECURE_STORE_TYPE);
    secureStore = SecureStoreFactory.getSecureStore(type, secureStoreProps);
  }

  public ConnectionSystemParameters getParamsWithToken(String edgeApiKey) {
    String tenantId;
    String username;
    String salt;
    try {
      ClientInfo clientInfo = ApiKeyParser.parseApiKey(edgeApiKey);
      tenantId = clientInfo.tenantId;
      username = clientInfo.username;
      salt = clientInfo.salt;

    } catch (ApiKeyParser.MalformedApiKeyException e) {
      throw new AuthorizationException("Malformed edge api key: " + edgeApiKey);
    }
    return getParamsDependingOnCachePresent(salt, tenantId, username);
  }

  public ConnectionSystemParameters getParamsDependingOnCachePresent(String salt, String tenantId,
    String username) {
    try {
      TokenCache cache = TokenCache.getInstance();
      UserToken token = cache.get(salt, tenantId, username);
      if (isValidUserToken(token) && token.accessTokenExpiration().isAfter(Instant.now().minusSeconds(30L))) {
        log.info("Using cached token");
        return new ConnectionSystemParameters().withOkapiToken(token)
          .withTenantId(tenantId);
      }
      log.debug("No token in cache, started process of fetching token");
      cache.invalidate(salt, tenantId, username);
      return buildRequiredOkapiHeadersWithToken(salt, tenantId, username);
    } catch (NotInitializedException e) {
      log.warn("Failed to access TokenCache", e);
    }
    return null;
  }

  private boolean isValidUserToken(UserToken token) {
    return token != null && token.accessToken() != null && token.accessTokenExpiration() != null;
  }

  private ConnectionSystemParameters buildRequiredOkapiHeadersWithToken(String salt, String tenantId,
    String username) {
    ConnectionSystemParameters connectionSystemParameters = buildLoginRequest(salt, tenantId, username);
    UserToken token = loginAndGetToken(connectionSystemParameters, tenantId);
    connectionSystemParameters.setOkapiToken(token);
    tokenCache.put(salt, tenantId, username, token);
    log.debug("Successfully fetched token and put in cache");
    return connectionSystemParameters;
  }

  UserToken loginAndGetToken(ConnectionSystemParameters connectionSystemParameters, String tenantId) {
    return systemUserService.authSystemUser(tenantId, connectionSystemParameters.getUsername(), connectionSystemParameters.getPassword());
  }

  private ConnectionSystemParameters buildLoginRequest(String salt, String tenantId,
    String username) {
    try {
      return ConnectionSystemParameters.builder()
        .tenantId(tenantId)
        .username(username)
        .password(secureStore.get(salt, tenantId, username))
        .build();
    } catch (NotFoundException e) {
      log.error("Exception retrieving password", e);
      throw new AuthorizationException(String
        .format("Cannot get system connection properties for user with name: %s, for tenant: %s", username, tenantId));
    }
  }
}
