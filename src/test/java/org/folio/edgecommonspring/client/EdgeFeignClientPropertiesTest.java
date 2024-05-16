package org.folio.edgecommonspring.client;

import org.folio.common.configuration.properties.TlsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EdgeFeignClientPropertiesTest {

  private EdgeFeignClientProperties edgeFeignClientProperties;

  @BeforeEach
  void setUp() {
    edgeFeignClientProperties = new EdgeFeignClientProperties();
  }

  @Test
  void testGetAndSetOkapiUrl() {
    String okapiUrl = "https://okapi-url";
    edgeFeignClientProperties.setOkapiUrl(okapiUrl);
    assertEquals(okapiUrl, edgeFeignClientProperties.getOkapiUrl());
  }

  @Test
  void testGetAndSetTlsProperties() {
    TlsProperties tlsProperties = new TlsProperties();
    tlsProperties.setTrustStorePassword("TrustStorePassword");
    tlsProperties.setTrustStorePath("TrustStorePath");
    tlsProperties.setTrustStoreType("TrustStoreType");

    edgeFeignClientProperties.setTls(tlsProperties);
    assertEquals(tlsProperties, edgeFeignClientProperties.getTls());
  }

  @Test
  void testDefaultConstructor() {
    assertNotNull(edgeFeignClientProperties);
    assertNull(edgeFeignClientProperties.getOkapiUrl());
    assertNull(edgeFeignClientProperties.getTls());
  }

  @Test
  void testConfigurationPropertiesAnnotation() {
    ConfigurationProperties configurationProperties = EdgeFeignClientProperties.class.getAnnotation(ConfigurationProperties.class);
    assertNotNull(configurationProperties);
    assertEquals("folio.client", configurationProperties.prefix());
  }
}
