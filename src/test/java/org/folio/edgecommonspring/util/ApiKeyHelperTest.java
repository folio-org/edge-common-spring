package org.folio.edgecommonspring.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class ApiKeyHelperTest {

  @InjectMocks
  private ApiKeyHelper apiKeyHelper;

  @Test
  void testHeaderOnly() {
    ReflectionTestUtils
      .setField(apiKeyHelper, "apiKeySources", "HEADER");
    apiKeyHelper.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "testKeyHeader");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelper.getEdgeApiKey(servletRequest);

    Assertions.assertEquals("testKeyHeader", apiKey);

  }

  @Test
  void testPathOnly() {
    ReflectionTestUtils
      .setField(apiKeyHelper, "apiKeySources", "PATH");
    apiKeyHelper.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("apiKeyPath", "testKeyPath");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelper.getEdgeApiKey(servletRequest);

    Assertions.assertEquals("testKeyPath", apiKey);

  }

  @Test
  void testParamOnly() {
    ReflectionTestUtils
      .setField(apiKeyHelper, "apiKeySources", "PARAM");
    apiKeyHelper.init();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("apikey", "testKeyParam");
    ServletRequest servletRequest = new HttpServletRequestWrapper(request);

    String apiKey = apiKeyHelper.getEdgeApiKey(servletRequest);

    Assertions.assertEquals("testKeyParam", apiKey);

  }
}
