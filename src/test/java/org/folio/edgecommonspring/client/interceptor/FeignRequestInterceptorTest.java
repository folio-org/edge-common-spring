package org.folio.edgecommonspring.client.interceptor;

import feign.RequestTemplate;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignRequestInterceptorTest {

  private final String TEST_TENANT = "test_tenant";
  @InjectMocks
  private FeignRequestInterceptor feignRequestInterceptor;
  @Mock
  private FolioExecutionContext executionContext;
  @Mock
  private RequestTemplate requestTemplate;

  @Test
  void testFeignInterceptorWithValidInterceptorUrl1() {
    when(requestTemplate.path()).thenReturn("/login");
    when(executionContext.getTenantId()).thenReturn("test_tenant");
    feignRequestInterceptor.apply(requestTemplate);
    verify(requestTemplate).header(TENANT, Collections.singletonList(TEST_TENANT));
  }

  @Test
  void testFeignInterceptorWithValidInterceptorUrl2() {
    when(requestTemplate.path()).thenReturn("/login-with-expiry");
    when(executionContext.getTenantId()).thenReturn("test_tenant");
    feignRequestInterceptor.apply(requestTemplate);
    verify(requestTemplate).header(TENANT, Collections.singletonList(TEST_TENANT));
  }

  @Test
  void testFeignInterceptorWithInValidInterceptorUrl() {
    when(requestTemplate.path()).thenReturn("/login-with-Expiry");
    feignRequestInterceptor.apply(requestTemplate);
    verify(requestTemplate, never()).header(any(), any(Iterable.class));
  }

}
