package org.folio.edgecommonspring.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class EdgeUrlRequestInterceptorTest {

  @InjectMocks private EdgeUrlRequestInterceptor edgeUrlRequestInterceptor;
  @Mock private EdgeClientProperties properties;
  @Mock private HttpRequest httpRequest;
  @Mock private ClientHttpResponse clientHttpResponse;
  @Mock private ClientHttpRequestExecution nextExecution;
  @Captor private ArgumentCaptor<HttpRequest> requestCaptor;

  @ParameterizedTest
  @CsvSource(value = {
    "http://entities, /entities",
    "https://entities, /entities",
    "/entities, /entities",
    "entities, /entities",
    "http://test/entities/92393, /test/entities/92393",
    "https://test/entities/92393, /test/entities/92393",
    "/test/entities/92393, /test/entities/92393",
    "test/entities/92393, /test/entities/92393"
  })
  void shouldInterceptWithValidOkapiUrl(String uri, String expectedPath) throws IOException {
    var okapiUrl = "https://test.okapi.sample.org";
    when(httpRequest.getURI()).thenReturn(URI.create(uri));
    when(properties.getOkapiUrl()).thenReturn(okapiUrl);
    when(nextExecution.execute(requestCaptor.capture(), any())).thenReturn(clientHttpResponse);
    try (var response = edgeUrlRequestInterceptor.intercept(httpRequest, new byte[0], nextExecution)) {
      assertThat(response).isNotNull();
      var capturedRequest = requestCaptor.getValue();
      assertThat(capturedRequest.getURI().toURL())
        .hasProtocol("https")
        .hasHost("test.okapi.sample.org")
        .hasPath(expectedPath);
    }
  }

  @ParameterizedTest
  @CsvSource(value = {
    "http://entities, /entities",
    "https://entities, /entities",
    "/entities, /entities",
    "entities, /entities",
    "http://test/entities/92393, /test/entities/92393",
    "https://test/entities/92393, /test/entities/92393",
    "/test/entities/92393, /test/entities/92393",
    "test/entities/92393, /test/entities/92393"
  })
  void shouldInterceptWithValidDeprecatedUrlSetting(String uri, String expectedPath) throws Exception {
    var okapiUrl = "https://depreacated.okapi.sample.org";
    FieldUtils.writeDeclaredField(edgeUrlRequestInterceptor, "okapiUrl", okapiUrl, true);

    when(httpRequest.getURI()).thenReturn(URI.create(uri));
    when(nextExecution.execute(requestCaptor.capture(), any())).thenReturn(clientHttpResponse);
    try (var response = edgeUrlRequestInterceptor.intercept(httpRequest, new byte[0], nextExecution)) {
      assertThat(response).isNotNull();
      var capturedRequest = requestCaptor.getValue();
      assertThat(capturedRequest.getURI().toURL())
        .hasProtocol("https")
        .hasHost("depreacated.okapi.sample.org")
        .hasPath(expectedPath);
    }

    verify(properties, never()).getOkapiUrl();
  }
}
