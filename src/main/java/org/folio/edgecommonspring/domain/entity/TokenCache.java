package org.folio.edgecommonspring.domain.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.cache.Cache;
import org.folio.edge.api.utils.cache.Cache.Builder;
import org.folio.edge.api.utils.cache.Cache.CacheValue;

public class TokenCache {
  private static final Logger logger = LogManager.getLogger(org.folio.edgecommonspring.domain.entity.TokenCache.class);
  private static org.folio.edgecommonspring.domain.entity.TokenCache instance = null;
  private final Cache<UserToken> cache;

  private TokenCache(long ttl, long nullTokenTtl, int capacity) {
    logger.info("Using TTL: {}", ttl);
    logger.info("Using null token TTL: {}", nullTokenTtl);
    logger.info("Using capacity: {}", capacity);
    this.cache = (new Builder()).withTTL(ttl).withNullValueTTL(nullTokenTtl).withCapacity(capacity).build();
  }

  public static synchronized org.folio.edgecommonspring.domain.entity.TokenCache getInstance() {
    if (instance == null) {
      throw new org.folio.edgecommonspring.domain.entity.TokenCache.NotInitializedException("You must call TokenCache.initialize(ttl, capacity) before you can get the singleton instance");
    } else {
      return instance;
    }
  }

  public static synchronized org.folio.edgecommonspring.domain.entity.TokenCache initialize(long ttl, long nullValueTtl, int capacity) {
    if (instance != null) {
      logger.warn("Reinitializing cache.  All cached entries will be lost");
    }

    instance = new org.folio.edgecommonspring.domain.entity.TokenCache(ttl, nullValueTtl, capacity);
    return instance;
  }

  public UserToken get(String clientId, String tenant, String username) {
    return (UserToken)this.cache.get(this.computeKey(clientId, tenant, username));
  }

  public CacheValue<UserToken> put(String clientId, String tenant, String username, UserToken token) {
    return this.cache.put(this.computeKey(clientId, tenant, username), token);
  }

  private String computeKey(String clientId, String tenant, String username) {
    return String.format("%s:%s:%s", clientId, tenant, username);
  }

  public static class NotInitializedException extends RuntimeException {
    private static final long serialVersionUID = -1660691531387000897L;

    public NotInitializedException(String msg) {
      super(msg);
    }
  }
}
