package org.folio.edgecommonspring.config;

import tools.jackson.databind.json.JsonMapper;

import org.folio.common.configuration.properties.TlsProperties;
import org.folio.common.utils.exception.SslInitializationException;
import org.folio.edgecommonspring.client.EdgeClientProperties;
import org.folio.edgecommonspring.client.EdgeUrlRequestInterceptor;
import org.folio.edgecommonspring.client.LoginRequestInterceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class EdgeServiceClientConfigurationTest {

  @InjectMocks private EdgeServiceClientConfiguration configuration;
  @Mock private EdgeClientProperties properties;
  @Mock private TlsProperties tlsProperties;
  @Mock private JsonMapper jsonMapper;
  @Mock private LoginRequestInterceptor loginRequestInterceptor;
  @Mock private EdgeUrlRequestInterceptor edgeUrlRequestInterceptor;

  @Test
  void edgeExchangeRestClientBuilder_noTls() {
    when(properties.getTls()).thenReturn(null);

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(
      jsonMapper, properties, null, loginRequestInterceptor, edgeUrlRequestInterceptor);

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

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(
      jsonMapper, properties, null, loginRequestInterceptor, edgeUrlRequestInterceptor);

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
    assertThatThrownBy(() -> configuration.edgeExchangeRestClientBuilder(
      jsonMapper, properties, null, loginRequestInterceptor, edgeUrlRequestInterceptor))
      .isInstanceOf(SslInitializationException.class)
      .hasMessageContaining("Error creating RestClient with SSL context");
  }

  @Test
  void enrichHeadersClient_withTlsAndTrustStorePath() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("classpath:test.truststore.jks");
    when(tlsProperties.getTrustStorePassword()).thenReturn("SecretPassword");

    var restClientBuilder = configuration.edgeExchangeRestClientBuilder(
      jsonMapper, properties, null, loginRequestInterceptor, edgeUrlRequestInterceptor);
    var client = restClientBuilder.build();

    assertThat(client).isInstanceOf(RestClient.class);
    verify(properties, atLeastOnce()).getTls();
    verify(tlsProperties, atLeastOnce()).getTrustStorePath();
    verify(tlsProperties, atLeastOnce()).getTrustStorePassword();
  }
}
