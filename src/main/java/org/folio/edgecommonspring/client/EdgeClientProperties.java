package org.folio.edgecommonspring.client;

import lombok.Data;
import org.folio.common.configuration.properties.TlsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "folio.client")
public class EdgeClientProperties {

  private static final int DEFAULT_MAX_CONNECTIONS = 200;
  private static final int DEFAULT_MAX_PER_ROUTE = 50;

  private String okapiUrl;

  /**
   * Maximum total number of connections in the edge client connection pool.
   * Configurable via {@code folio.client.max-connections} property or {@code FOLIO_CLIENT_MAX_CONNECTIONS}
   * environment variable.
   */
  private int maxConnections = DEFAULT_MAX_CONNECTIONS;

  /**
   * Maximum number of connections per route in the edge client connection pool.
   * Configurable via {@code folio.client.max-per-route} property or {@code FOLIO_CLIENT_MAX_PER_ROUTE}
   * environment variable.
   */
  private int maxPerRoute = DEFAULT_MAX_PER_ROUTE;

  @NestedConfigurationProperty
  private TlsProperties tls;
}
