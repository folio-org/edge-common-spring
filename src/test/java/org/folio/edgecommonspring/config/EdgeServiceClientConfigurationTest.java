package org.folio.edgecommonspring.config;

import tools.jackson.databind.json.JsonMapper;

import com.sun.net.httpserver.HttpServer;
import org.folio.common.configuration.properties.TlsProperties;
import org.folio.common.utils.exception.SslInitializationException;
import org.folio.edgecommonspring.client.EdgeClientProperties;
import org.folio.edgecommonspring.client.EdgeUrlRequestInterceptor;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.client.EnrichUrlAndHeadersInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
class EdgeServiceClientConfigurationTest {

  @InjectMocks private EdgeServiceClientConfiguration configuration;
  @Mock private EdgeClientProperties properties;
  @Mock private TlsProperties tlsProperties;
  @Mock private JsonMapper jsonMapper;
  @Mock private EdgeUrlRequestInterceptor edgeUrlRequestInterceptor;
  @Mock private EnrichUrlAndHeadersInterceptor enrichUrlAndHeadersInterceptor;

  @Test
  void edgeExchangeRestClientBuilder_noTls() {
    when(properties.getTls()).thenReturn(null);

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor);

    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verifyNoMoreInteractions(properties);
  }

  @Test
  void edgeExchangeRestClientBuilder_tlsIsDisabled() {
    when(tlsProperties.isEnabled()).thenReturn(false);
    when(properties.getTls()).thenReturn(tlsProperties);

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor);

    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verifyNoMoreInteractions(properties);
  }

  @Test
  void edgeExchangeRestClientBuilder_trustStorePathIsEmpty() {
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("");
    when(properties.getTls()).thenReturn(tlsProperties);

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor);

    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verifyNoMoreInteractions(properties);
  }

  @Test
  void enrichHeadersClient_withTlsNoTrustStorePath() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn(null);

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor);

    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verify(tlsProperties, atLeastOnce()).getTrustStorePath();
  }

  @Test
  void enrichHeadersClient_withTlsAndTrustStorePathButBuildSslContextThrowsException() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("classpath:test.truststore1.jks");
    assertThatThrownBy(() -> configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor))
      .isInstanceOf(SslInitializationException.class)
      .hasMessageContaining("Error creating RestClient with SSL context");
  }

  @Test
  void enrichHeadersClient_withTlsAndTrustStorePath() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("classpath:test.truststore.jks");
    when(tlsProperties.getTrustStorePassword()).thenReturn("SecretPassword");

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, properties,
      null, edgeUrlRequestInterceptor, enrichUrlAndHeadersInterceptor);
    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verify(tlsProperties, atLeastOnce()).getTrustStorePath();
    verify(tlsProperties, atLeastOnce()).getTrustStorePassword();
  }

  @Test
  void shouldCreateEdgeHttpServiceProxyFactory() {
    var restClientBuilder = RestClient.builder();
    var factory = configuration.edgeHttpServiceProxyFactory(restClientBuilder, Optional.empty());

    assertThat(factory).isNotNull();
  }

  @Test
  void shouldCreateEdgeHttpServiceProxyFactoryWithCustomizer() {
    var restClientBuilder = RestClient.builder();
    UnaryOperator<RestClient.Builder> customizer = builder ->
      builder.baseUrl("http://custom-url");

    var factory = configuration.edgeHttpServiceProxyFactory(
      restClientBuilder,
      Optional.of(customizer)
    );

    assertThat(factory).isNotNull();
  }

  @Test
  void edgeExchangeRestClient_doesNotReuseCookiesAcrossConcurrentRequests() throws Exception {
    var observedCookieHeaders = new CopyOnWriteArrayList<String>();
    var server = buildTestHttpServer(observedCookieHeaders);

    try {
      server.start();
      var baseUrl = "http://localhost:" + server.getAddress().getPort();
      var edgeClientProperties = new EdgeClientProperties();
      edgeClientProperties.setOkapiUrl(baseUrl);

      var folioExecutionContext = mock(FolioExecutionContext.class);

      var restClientBuilder = configuration.edgeExchangeRestClientBuilder(jsonMapper, edgeClientProperties,
        null, new EdgeUrlRequestInterceptor(edgeClientProperties),
        new EnrichUrlAndHeadersInterceptor(folioExecutionContext));
      var restClient = restClientBuilder.baseUrl(baseUrl).build();

      // Simulate several concurrent "system user login" responses, each setting a different
      // folioAccessToken cookie on the shared HttpClient, as would happen when concurrent
      // requests race to refresh a near-expiry token (see SecurityManagerService).
      runConcurrently(restClient, i -> "/login/token-" + i);

      observedCookieHeaders.clear();
      runConcurrently(restClient, i -> "/sample-folio-path");

      assertThat(observedCookieHeaders)
        .as("no outgoing request should carry a Cookie header reused from another request")
        .allSatisfy(cookie -> assertThat(cookie).isNull());

    } finally {
      server.stop(0);
    }
  }

  @Test
  void shouldCreateEnrichUrlAndHeadersInterceptor() {
    var context = mock(FolioExecutionContext.class);
    var result = configuration.enrichUrlAndHeadersInterceptor(context);
    assertThat(result).isNotNull();
  }

  @Test
  void shouldCreateEdgeUrlRequestInterceptor() {
    var result = configuration.edgeUrlRequestInterceptor(properties);
    assertThat(result).isNotNull();
  }

  private static HttpServer buildTestHttpServer(CopyOnWriteArrayList<String> observedCookieHeaders) throws IOException {
    var server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", exchange -> {
      try (exchange) {
        observedCookieHeaders.add(exchange.getRequestHeaders().getFirst("Cookie"));
        if (exchange.getRequestURI().getPath().startsWith("/login/")) {
          var token = exchange.getRequestURI().getPath().substring("/login/".length());
          exchange.getResponseHeaders().add("Set-Cookie", "folioAccessToken=" + token + "; Path=/");
        }

        var body = "{}".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
      }
    });
    server.setExecutor(Executors.newFixedThreadPool(16));
    return server;
  }

  private static void runConcurrently(RestClient restClient, IntFunction<String> pathForIndex) throws Exception {
    try (var executor = Executors.newFixedThreadPool(10)) {
      var tasks = IntStream.range(0, 10)
        .mapToObj(i -> getVoidCallableForRequest(restClient, pathForIndex, i))
        .toList();

      var futures = executor.invokeAll(tasks);
      for (var future : futures) {
        future.get();
      }
    }
  }

  private static Callable<Void> getVoidCallableForRequest(
    RestClient restClient, IntFunction<String> pathForIndex, int index) {
    return () -> {
      restClient.get().uri(pathForIndex.apply(index)).retrieve().toBodilessEntity();
      return null;
    };
  }
}
