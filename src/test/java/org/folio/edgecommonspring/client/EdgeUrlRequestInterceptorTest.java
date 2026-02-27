package org.folio.edgecommonspring.client;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.common.utils.OkapiHeaders;
import org.folio.spring.FolioExecutionContext;

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
