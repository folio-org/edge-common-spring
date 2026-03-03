package org.folio.edgecommonspring.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

@Log4j2
@RequiredArgsConstructor
public class EdgeUrlRequestInterceptor implements ClientHttpRequestInterceptor {

  private static final String HTTP_PROTOCOL_PREFIX = "http://";
  private static final String HTTPS_PROTOCOL_PREFIX = "https://";
  private final EdgeClientProperties properties;

  /**
   * Okapi URL from properties. This is used for backward compatibility with existing clients that use this property.
   *
   * @deprecated okapi_url is deprecated.
   *   Please use folio.client.okapiUrl instead: {@link EdgeClientProperties#getOkapiUrl()}
   */
  @Deprecated
  @Value("${okapi_url:#{null}}")
  private String okapiUrl;

  @Override
  public @NonNull ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body,
    @NonNull ClientHttpRequestExecution execution) throws IOException {

    var okapiUrlToUse = getUrlToUse();

    // If the request already starts with the Okapi URL, modification can be skipped to invalid URIs.
    if (Strings.CI.startsWith(request.getURI().toString(), okapiUrlToUse)) {
      return execution.execute(request, body);
    }

    var uri = prepareUrl(request.getURI().toString(), okapiUrlToUse);
    var modifiedRequest = new EdgeHttpRequestWrapper(request, URI.create(uri), request.getHeaders());
    return execution.execute(modifiedRequest, body);
  }

  private String getUrlToUse() {
    String okapiUrlToUse = okapiUrl;
    if (isNotBlank(okapiUrlToUse)) {
      log.warn("deprecated property okapi_url is used. Please use folio.client.okapiUrl instead.");
    } else {
      okapiUrlToUse = properties.getOkapiUrl();
    }
    return okapiUrlToUse;
  }

  static String prepareUrl(String requestUrl, String okapiUrl) {
    var modifiedOkapiUrl = Strings.CS.appendIfMissing(okapiUrl, "/");

    if (requestUrl.startsWith(HTTP_PROTOCOL_PREFIX)) {
      return Strings.CS.replace(requestUrl, HTTP_PROTOCOL_PREFIX, modifiedOkapiUrl);
    }

    if (requestUrl.startsWith(HTTPS_PROTOCOL_PREFIX)) {
      return Strings.CS.replace(requestUrl, HTTPS_PROTOCOL_PREFIX, modifiedOkapiUrl);
    }

    return modifiedOkapiUrl + Strings.CI.removeStart(requestUrl, "/");
  }

  private static class EdgeHttpRequestWrapper extends HttpRequestWrapper {

    private final URI uri;
    private final HttpHeaders headers;

    EdgeHttpRequestWrapper(HttpRequest request, URI uri, HttpHeaders headers) {
      super(request);
      this.uri = uri;
      this.headers = headers;
    }

    @Override
    public @NonNull URI getURI() {
      return uri;
    }

    @Override
    public @NonNull HttpHeaders getHeaders() {
      return headers;
    }
  }
}
