package org.folio.edgecommonspring.config;

import feign.Client;
import org.folio.common.configuration.properties.TlsProperties;
import org.folio.common.utils.exception.SslInitializationException;
import org.folio.edgecommonspring.client.EdgeFeignClientProperties;
import org.folio.edgecommonspring.client.EnrichUrlClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FeignClientConfigurationTest {

  @Mock
  private EdgeFeignClientProperties properties;

  @Mock
  private TlsProperties tlsProperties;

  @InjectMocks
  private FeignClientConfiguration configuration;

  @Test
  void enrichHeadersClient_noTls() {
    when(properties.getTls()).thenReturn(null);

    Client client = configuration.enrichHeadersClient();

    assertThat(client).isInstanceOf(EnrichUrlClient.class);
    verify(properties, atLeastOnce()).getTls();
    verifyNoMoreInteractions(properties);
  }

  @Test
  void enrichHeadersClient_withTlsNoTrustStorePath() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn(null);

    Client client = configuration.enrichHeadersClient();

    assertThat(client).isInstanceOf(EnrichUrlClient.class);
    verify(properties, atLeastOnce()).getTls();
    verify(tlsProperties, atLeastOnce()).getTrustStorePath();
  }

  @Test
  void enrichHeadersClient_withTlsAndTrustStorePathButBuildSslContextThrowsException() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("classpath:test.truststore1.jks");
    when(tlsProperties.getTrustStorePassword()).thenReturn("SecretPassword");
    assertThatThrownBy(() -> configuration.enrichHeadersClient())
      .isInstanceOf(SslInitializationException.class)
      .hasMessageContaining("Error creating EnrichUrlClient with SSL context");
  }

  @Test
  void enrichHeadersClient_withTlsAndTrustStorePath() {
    when(properties.getTls()).thenReturn(tlsProperties);
    when(tlsProperties.isEnabled()).thenReturn(true);
    when(tlsProperties.getTrustStorePath()).thenReturn("classpath:test.truststore.jks");
    when(tlsProperties.getTrustStorePassword()).thenReturn("SecretPassword");

    Client client = configuration.enrichHeadersClient();
    assertThat(client).isInstanceOf(EnrichUrlClient.class);

    verify(properties, atLeastOnce()).getTls();
    verify(tlsProperties, atLeastOnce()).getTrustStorePath();
    verify(tlsProperties, atLeastOnce()).getTrustStorePassword();
  }
}
