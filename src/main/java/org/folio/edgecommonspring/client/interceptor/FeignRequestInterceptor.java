package org.folio.edgecommonspring.client.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;

@Component
@RequiredArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public void apply(RequestTemplate template) {
    if ("/login".equals(template.path()) || "/login-with-expiry".equals(template.path())) {
      template.header(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
    }
  }
}
