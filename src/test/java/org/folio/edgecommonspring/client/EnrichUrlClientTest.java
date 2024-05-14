package org.folio.edgecommonspring.client;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

class EnrichUrlClientTest {
  @Mock
  private EdgeFeignClientProperties properties;

  @Mock
  private SSLSocketFactory sslSocketFactory;

  @Mock
  private HostnameVerifier hostnameVerifier;

  private EnrichUrlClient enrichUrlClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    enrichUrlClient = new EnrichUrlClient(properties, sslSocketFactory, hostnameVerifier);
  }

  @Test
  void testConstructor() {
    assertNotNull(enrichUrlClient);
    // Use reflection to access the private 'properties' field
    try {
      EdgeFeignClientProperties propertiesField = (EdgeFeignClientProperties) FieldUtils.readDeclaredField(enrichUrlClient, "properties", true);
      assertEquals(properties, propertiesField);
    } catch (IllegalAccessException e) {
      fail("Failed to access the 'properties' field using reflection.");
    }
  }

  @Test
  void testGetUrlToUseWithOkapiUrl() throws Exception {
    String okapiUrl = "https://okapi-url";
    when(properties.getOkapiUrl()).thenReturn(okapiUrl);

    Method method = EnrichUrlClient.class.getDeclaredMethod("getUrlToUse");
    method.setAccessible(true);

    String result = (String) method.invoke(enrichUrlClient);
    assertEquals(okapiUrl, result);
  }

  @Test
  void testGetUrlToUseWithDeprecatedOkapiUrl() throws Exception {
    String deprecatedOkapiUrl = "https://deprecated-okapi-url";
    when(properties.getOkapiUrl()).thenReturn(null);
    FieldUtils.writeDeclaredField(enrichUrlClient, "okapiUrl", deprecatedOkapiUrl, true);

    Method method = EnrichUrlClient.class.getDeclaredMethod("getUrlToUse");
    method.setAccessible(true);

    String result = (String) method.invoke(enrichUrlClient);
    assertEquals(deprecatedOkapiUrl, result);
  }


}
