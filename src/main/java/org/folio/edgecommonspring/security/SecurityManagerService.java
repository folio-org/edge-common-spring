package org.folio.edgecommonspring.security;

import static java.util.Optional.ofNullable;
import static org.folio.edge.api.utils.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.api.utils.Constants.PROP_SECURE_STORE_TYPE;
import static org.folio.edge.api.utils.Constants.X_OKAPI_TOKEN;

import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.api.utils.cache.TokenCache;
import org.folio.edge.api.utils.cache.TokenCache.NotInitializedException;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edge.api.utils.model.ClientInfo;
import org.folio.edge.api.utils.security.SecureStore;
import org.folio.edge.api.utils.security.SecureStore.NotFoundException;
import org.folio.edge.api.utils.security.SecureStoreFactory;
import org.folio.edge.api.utils.util.ApiKeyParser;
import org.folio.edge.api.utils.util.PropertiesUtil;
import org.folio.edgecommonspring.client.AuthnClient;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SecurityManagerService {

  private final AuthnClient authnClient;
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

  private ConnectionSystemParameters getParamsDependingOnCachePresent(String salt, String tenantId,
    String username) {
    try {
      TokenCache cache = TokenCache.getInstance();
      String token = cache.get(salt, tenantId, username);
      if (StringUtils.isNotEmpty(token)) {
        log.info("Using cached token");
        return new ConnectionSystemParameters().withOkapiToken(token)
          .withTenantId(tenantId);
      }
    } catch (NotInitializedException e) {
      log.warn("Failed to access TokenCache", e);
    }
    log.debug("No token in cache, started process of fetching token");
    return buildRequiredOkapiHeadersWithToken(salt, tenantId, username);
  }

  private ConnectionSystemParameters buildRequiredOkapiHeadersWithToken(String salt, String tenantId,
    String username) {
    ConnectionSystemParameters connectionSystemParameters = buildLoginRequest(salt, tenantId, username);
    String token = loginAndGetToken(connectionSystemParameters, tenantId);
    connectionSystemParameters.setOkapiToken(token);
    tokenCache.put(salt, tenantId, username, token);
    log.debug("Successfully fetched token and put in cache");
    return connectionSystemParameters;
  }

  private String loginAndGetToken(ConnectionSystemParameters connectionSystemParameters, String tenantId) {
    return ofNullable(
      authnClient.getApiKey(connectionSystemParameters, tenantId)
        .getHeaders()
        .get(X_OKAPI_TOKEN))
      .orElseThrow(() -> new AuthorizationException("Cannot retrieve okapi token for tenant: " + tenantId))
      .get(0);
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
