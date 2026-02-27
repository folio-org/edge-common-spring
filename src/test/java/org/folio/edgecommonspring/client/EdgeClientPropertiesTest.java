package org.folio.edgecommonspring.client;

import org.folio.common.configuration.properties.TlsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EdgeClientPropertiesTest {

  private EdgeClientProperties edgeClientProperties;

  @BeforeEach
  void setUp() {
    edgeClientProperties = new EdgeClientProperties();
  }

  @Test
  void testGetAndSetOkapiUrl() {
    String okapiUrl = "https://okapi-url";
    edgeClientProperties.setOkapiUrl(okapiUrl);
    assertEquals(okapiUrl, edgeClientProperties.getOkapiUrl());
  }

  @Test
  void testGetAndSetTlsProperties() {
    TlsProperties tlsProperties = new TlsProperties();
    tlsProperties.setTrustStorePassword("TrustStorePassword");
    tlsProperties.setTrustStorePath("TrustStorePath");
    tlsProperties.setTrustStoreType("TrustStoreType");

    edgeClientProperties.setTls(tlsProperties);
    assertEquals(tlsProperties, edgeClientProperties.getTls());
  }

  @Test
  void testDefaultConstructor() {
    assertNotNull(edgeClientProperties);
    assertNull(edgeClientProperties.getOkapiUrl());
    assertNull(edgeClientProperties.getTls());
  }

  @Test
  void testConfigurationPropertiesAnnotation() {
    ConfigurationProperties configurationProperties = EdgeClientProperties.class.getAnnotation(ConfigurationProperties.class);
    assertNotNull(configurationProperties);
    assertEquals("folio.client", configurationProperties.prefix());
  }
}
