package org.folio.edgecommonspring.config;

import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import org.folio.common.utils.exception.SslInitializationException;
import org.folio.edgecommonspring.client.EdgeClientProperties;
import org.folio.edgecommonspring.client.EdgeUrlRequestInterceptor;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.client.EnrichUrlAndHeadersInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.folio.common.utils.tls.FeignClientTlsUtils.buildSslContext;
import static org.folio.common.utils.tls.Utils.IS_HOSTNAME_VERIFICATION_DISABLED;

@Log4j2
@Configuration
@EnableConfigurationProperties(EdgeClientProperties.class)
public class EdgeServiceClientConfiguration {

  private static final DefaultHostnameVerifier DEFAULT_HOSTNAME_VERIFIER = new DefaultHostnameVerifier();

  /**
   * Provides interceptor to enrich outgoing requests with edge service URL from configuration properties.
   *
   * @param properties - edge client properties to get edge service URL from
   * @return {@link EdgeUrlRequestInterceptor} instance
   */
  @Bean
  public EdgeUrlRequestInterceptor edgeUrlRequestInterceptor(EdgeClientProperties properties) {
    return new EdgeUrlRequestInterceptor(properties);
  }

  /**
   * This bean is required to enrich outgoing requests with okapi headers.
   * It's conditional to avoid conflict if {@code folio.exchange.enabled} is set to {@code true}.
   *
   * @param context - folio execution context to get okapi headers values from
   * @return {@link EnrichUrlAndHeadersInterceptor} instance
   */
  @Bean
  @ConditionalOnMissingBean(EnrichUrlAndHeadersInterceptor.class)
  public EnrichUrlAndHeadersInterceptor enrichUrlAndHeadersInterceptor(FolioExecutionContext context) {
    return new EnrichUrlAndHeadersInterceptor(context);
  }

  @Bean
  public RestClient.Builder edgeExchangeRestClientBuilder(JsonMapper jsonMapper,
    EdgeClientProperties edgeClientProperties,
    @Qualifier("exchangeJsonMapper") @Autowired(required = false) JsonMapper exchangeJsonMapper,
    @Qualifier("edgeUrlRequestInterceptor") EdgeUrlRequestInterceptor edgeUrlRequestInterceptor,
    @Qualifier("enrichUrlAndHeadersInterceptor") EnrichUrlAndHeadersInterceptor enrichUrlAndHeadersInterceptor) {

    var mapper = getIfNull(exchangeJsonMapper, jsonMapper);
    return RestClient.builder()
      .requestFactory(buildRequestFactory(edgeClientProperties))
      .requestInterceptor(enrichUrlAndHeadersInterceptor)
      .requestInterceptor(edgeUrlRequestInterceptor)
      .configureMessageConverters(configurer ->
        configurer.addCustomConverter(new JacksonJsonHttpMessageConverter(mapper)));
  }

  @Bean
  public HttpServiceProxyFactory edgeHttpServiceProxyFactory(
    @Qualifier("edgeExchangeRestClientBuilder") RestClient.Builder exchangeRestClient) {
    return HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(exchangeRestClient.build()))
      .build();
  }

  private static ClientHttpRequestFactory buildRequestFactory(EdgeClientProperties edgeClientProperties) {
    var tls = edgeClientProperties.getTls();
    if (tls == null || !tls.isEnabled() || !StringUtils.hasText(tls.getTrustStorePath())) {
      log.info("RestClient without TLS will be created. TLS config: {}", tls);
      return new HttpComponentsClientHttpRequestFactory();
    }

    try {
      var hostnameVerifier = IS_HOSTNAME_VERIFICATION_DISABLED
        ? NoopHostnameVerifier.INSTANCE
        : DEFAULT_HOSTNAME_VERIFIER;

      var tlsSocketStrategy = ClientTlsStrategyBuilder.create()
        .setSslContext(buildSslContext(tls))
        .setHostnameVerifier(hostnameVerifier)
        .buildClassic();

      var httpClient = HttpClients.custom()
        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
          .setTlsSocketStrategy(tlsSocketStrategy)
          .build())
        .build();

      log.info("RestClient with TLS will be created. TLS config: {}", tls);
      return new HttpComponentsClientHttpRequestFactory(httpClient);

    } catch (Exception e) {
      throw new SslInitializationException("Error creating RestClient with SSL context", e);
    }
  }
}
