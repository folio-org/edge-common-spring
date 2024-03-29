package org.folio.edgecommonspring.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApiKeyHelperImplTest {

  @InjectMocks
  private ApiKeyHelperImpl apiKeyHelperImpl;

  @Test
  void testHeaderOnly() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "HEADER");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "testKeyHeader");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertEquals("testKeyHeader", apiKey);

  }

  @Test
  void testPathOnly() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "PATH");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("apiKeyPath", "testKeyPath");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertEquals("testKeyPath", apiKey);

  }

  @Test
  void shouldReturnApiKey_byNewApiKeyParamName() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "PARAM");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("apiKey", "testKeyParam");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertEquals("testKeyParam", apiKey);

  }

  @Test
  void shouldReturnApiKey_byLegacyApiKeyParamName() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "PARAM");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("apikey", "testKeyParam");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertEquals("testKeyParam", apiKey);

  }

  @Test
  void shouldReturnNull_whenApiKeyNotPresent() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "PARAM");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertNull(apiKey);
  }

  @Test
  void shouldReturnNull_whenHeaderApiKeyEmpty() {
    ReflectionTestUtils
      .setField(apiKeyHelperImpl, "apiKeySources", "HEADER");
    apiKeyHelperImpl.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelperImpl.getEdgeApiKey(servletRequest, apiKeyHelperImpl.getSources());

    assertNull(apiKey);
  }
}
