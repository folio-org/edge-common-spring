package org.folio.edgecommonspring.config;

import feign.Client;
import org.folio.edgecommonspring.client.EnrichUrlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.clientconfig.OkHttpFeignConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableFeignClients(basePackages = {"org.folio.edgecommonspring.client"})
@ConditionalOnMissingBean(value = Client.class)
@Import(OkHttpFeignConfiguration.class)
public class FeignClientConfiguration {

  public Client enrichHeadersClient(@Autowired okhttp3.OkHttpClient okHttpClient) {
    return new EnrichUrlClient(okHttpClient);
  }
}
