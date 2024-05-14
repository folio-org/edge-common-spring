package org.folio.edgecommonspring.client;

import lombok.Data;
import org.folio.common.configuration.properties.TlsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "folio.client")
public class EdgeFeignClientProperties {
  private String okapiUrl;

  @NestedConfigurationProperty
  private TlsProperties tls;
}
