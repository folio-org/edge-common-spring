package org.folio.edgecommonspring.config;

import feign.Client;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.folio.common.utils.exception.SslInitializationException;
import org.folio.edgecommonspring.client.EdgeFeignClientProperties;
import org.folio.edgecommonspring.client.EnrichUrlClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.common.utils.FeignClientTlsUtils.buildSslContext;

@Configuration
@EnableFeignClients(basePackages = {"org.folio.edgecommonspring.client"})
@EnableConfigurationProperties(EdgeFeignClientProperties.class)
@ConditionalOnMissingBean(value = Client.class)
@ComponentScan({"org.folio.edgecommonspring.client",
  "org.folio.edgecommonspring.security",
  "org.folio.edgecommonspring.domain.entity",
  "org.folio.edgecommonspring.util",
  "org.folio.edgecommonspring.filter"})
@Log4j2
@AllArgsConstructor
public class FeignClientConfiguration {
  private final EdgeFeignClientProperties properties;

  @Bean
  public Client enrichHeadersClient() {
    var tls = properties.getTls();
    if (tls == null || Boolean.FALSE.equals(tls.isEnabled()) || isBlank(tls.getTrustStorePath())) {
      log.info("EnrichUrlClient without TLS will be created. tls configuration used: {}", tls);
      return new EnrichUrlClient(properties, null, null);
    }

    try {
      var sslSocketFactory = buildSslContext(tls).getSocketFactory();
      return new EnrichUrlClient(properties, sslSocketFactory, NoopHostnameVerifier.INSTANCE);
    } catch (Exception e) {
      throw new SslInitializationException("Error creating EnrichUrlClient with SSL context", e);
    }
  }
}
