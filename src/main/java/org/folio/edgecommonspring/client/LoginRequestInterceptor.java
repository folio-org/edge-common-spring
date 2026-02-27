package org.folio.edgecommonspring.client;

import lombok.RequiredArgsConstructor;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import org.folio.spring.FolioExecutionContext;

@RequiredArgsConstructor
public class LoginRequestInterceptor implements ClientHttpRequestInterceptor {

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body,
    @NonNull ClientHttpRequestExecution execution) throws IOException {

    var uriPath = request.getURI().getPath();
    var path = uriPath.startsWith("/") ? uriPath : "/" + uriPath;
    if (Strings.CS.endsWithAny(path, "/login", "/login-with-expiry")) {
      request.getHeaders().put(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
    }
    return execution.execute(request, body);
  }
}
