package org.folio.edgecommonspring.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import org.folio.common.utils.OkapiHeaders;
import org.folio.spring.FolioExecutionContext;

@ExtendWith(MockitoExtension.class)
class LoginRequestInterceptorTest {

  @InjectMocks private LoginRequestInterceptor loginRequestInterceptor;
  @Mock private HttpRequest httpRequest;
  @Mock private ClientHttpResponse clientHttpResponse;
  @Mock private FolioExecutionContext executionContext;
  @Mock private ClientHttpRequestExecution nextExecution;

  @ParameterizedTest
  @ValueSource(strings = {
    "http:///login",
    "/login",
    "/authn/login",
    "login",
    "authn/login",
    "http:///login-with-expiry",
    "http:///authn/login-with-expiry",
    "/login-with-expiry",
    "/authn/login-with-expiry",
    "login-with-expiry",
    "authn/login-with-expiry",
  })
  void testFeignInterceptorWithValidInterceptorUrl(String uri) throws IOException {
    var headers = new HttpHeaders();
    when(httpRequest.getURI()).thenReturn(URI.create(uri));
    when(executionContext.getTenantId()).thenReturn("test_tenant");
    when(httpRequest.getHeaders()).thenReturn(headers);
    when(nextExecution.execute(any(), any())).thenReturn(clientHttpResponse);
    try (var response = loginRequestInterceptor.intercept(httpRequest, new byte[0], nextExecution)) {
     assertThat(response).isNotNull();
     assertThat(headers.get(OkapiHeaders.TENANT)).isEqualTo(List.of("test_tenant"));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "http://test",
    "test",
    "/test",
    "http://login-with-Expiry",
    "/login-with-Expiry",
    "login-with-Expiry",
    "http://Login",
    "/Login",
    "Login/test/123",
    "http://unknown",
  })
  void testFeignInterceptorWithInvalidInterceptorUrl(String uri) throws IOException {
    when(httpRequest.getURI()).thenReturn(URI.create(uri));
    when(nextExecution.execute(any(), any())).thenReturn(clientHttpResponse);
    try (var response = loginRequestInterceptor.intercept(httpRequest, new byte[0], nextExecution)) {
      assertThat(response).isNotNull();
    }

    verify(executionContext, never()).getTenantId();
    verify(httpRequest, never()).getHeaders();
  }
}
