package org.folio.edgecommonspring.security;

import static java.util.Optional.ofNullable;
import static org.folio.edge.api.utils.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.api.utils.Constants.PROP_SECURE_STORE_TYPE;
import static org.folio.edge.api.utils.Constants.X_OKAPI_TOKEN;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.api.utils.cache.TokenCache;
import org.folio.edge.api.utils.cache.TokenCache.NotInitializedException;
import org.folio.edge.api.utils.model.ClientInfo;
import org.folio.edge.api.utils.security.SecureStore;
import org.folio.edge.api.utils.security.SecureStore.NotFoundException;
import org.folio.edge.api.utils.security.SecureStoreFactory;
import org.folio.edgecommonspring.client.AuthnClient;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.edgecommonspring.exception.AuthorizationException;
import org.folio.edgecommonspring.util.ApiKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SecurityManagerService {

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");
  @Autowired
  private final AuthnClient authnClient;
  private SecureStore secureStore;
  private TokenCache tokenCache;
  @Value("${secure_store}")
  private String secureStoreType;
  @Value("${secure_store_props}")
  private String secureStorePropsFile;
  @Value("${token_cache_ttl_ms}")
  private long cacheTtlMs;
  @Value("${null_token_cache_ttl_ms}")
  private long failureCacheTtlMs;
  @Value("${token_cache_capacity}")
  private int cacheCapacity;

  private static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    log.info("Attempt to load properties from: " + secureStorePropFile);

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
          secureStoreProps.load(in);
          log.info("Successfully loaded properties from: " + secureStorePropFile);
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      log.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }

  @PostConstruct
  public void init() {
    if (null == tokenCache) {
      log.info("Using token cache TTL (ms): {}", cacheTtlMs);
      log.info("Using failure token cache TTL (ms): {}", failureCacheTtlMs);
      log.info("Using token cache capacity: {}", cacheCapacity);
      tokenCache = TokenCache.initialize(cacheTtlMs, failureCacheTtlMs, cacheCapacity);
    }
    Properties secureStoreProps = getProperties(secureStorePropsFile);
    String type = secureStoreProps.getProperty(PROP_SECURE_STORE_TYPE, DEFAULT_SECURE_STORE_TYPE);
    secureStore = SecureStoreFactory.getSecureStore(type, secureStoreProps);
  }

  public ConnectionSystemParameters getParamsWithToken(String edgeApiKey) {
    String tenantId;
    String username;
    String salt;
    try {
      ClientInfo clientInfo = ApiKeyUtils.parseApiKey(edgeApiKey);
      tenantId = clientInfo.tenantId;
      username = clientInfo.username;
      salt = clientInfo.salt;

    } catch (ApiKeyUtils.MalformedApiKeyException e) {
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
    return buildRequiredOkapiHeadersWithToken(salt, tenantId, username);
  }

  private ConnectionSystemParameters buildRequiredOkapiHeadersWithToken(String salt, String tenantId,
    String username) {
    ConnectionSystemParameters connectionSystemParameters = buildLoginRequest(salt, tenantId, username);
    String token = loginAndGetToken(connectionSystemParameters, tenantId);
    connectionSystemParameters.setOkapiToken(token);
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
      throw new AuthorizationException("Cannot get system connection properties for: " + tenantId);
    }
  }

}
