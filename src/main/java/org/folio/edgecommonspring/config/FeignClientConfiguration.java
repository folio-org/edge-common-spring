package org.folio.edgecommonspring.config;

import feign.Client;
import org.folio.edgecommonspring.client.EnrichUrlClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {"org.folio.edgecommonspring.client"})
@ConditionalOnMissingBean(value = Client.class)
@ComponentScan({"org.folio.edgecommonspring.client", "org.folio.edgecommonspring.security",
  "org.folio.edgecommonspring.domain.entity", "org.folio.edgecommonspring.util", "org.folio.edgecommonspring.filter"})
public class FeignClientConfiguration {

  @Bean
  public Client enrichHeadersClient() {
    return new EnrichUrlClient();
  }
}
